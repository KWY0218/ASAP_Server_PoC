package com.asap.server.service;

import static com.asap.server.common.exception.Error.INVALID_MEETING_HOST_EXCEPTION;
import static com.asap.server.common.exception.Error.USER_NOT_FOUND_EXCEPTION;

import com.asap.server.common.exception.Error;
import com.asap.server.common.exception.model.BadRequestException;
import com.asap.server.common.exception.model.ConflictException;
import com.asap.server.common.exception.model.NotFoundException;
import com.asap.server.common.exception.model.UnauthorizedException;
import com.asap.server.common.jwt.JwtService;
import com.asap.server.persistence.domain.AvailableDate;
import com.asap.server.persistence.domain.Meeting;
import com.asap.server.persistence.domain.user.Name;
import com.asap.server.persistence.domain.user.User;
import com.asap.server.persistence.domain.enums.Role;
import com.asap.server.persistence.domain.enums.TimeSlot;
import com.asap.server.persistence.repository.meeting.MeetingRepository;
import com.asap.server.persistence.repository.user.UserRepository;
import com.asap.server.presentation.controller.dto.request.AvailableTimeRequestDto;
import com.asap.server.presentation.controller.dto.request.UserMeetingTimeSaveRequestDto;
import com.asap.server.presentation.controller.dto.request.UserRequestDto;
import com.asap.server.presentation.controller.dto.response.UserMeetingTimeResponseDto;
import com.asap.server.presentation.controller.dto.response.UserTimeResponseDto;
import com.asap.server.service.vo.BestMeetingTimeVo;
import com.asap.server.service.vo.BestMeetingTimeWithUsersVo;
import com.asap.server.service.vo.UserVo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TimeBlockService timeBlockService;
    private final TimeBlockUserService timeBlockUserService;
    private final AvailableDateService availableDateService;
    private final MeetingRepository meetingRepository;
    private final JwtService jwtService;
    private final UserMeetingScheduleService userMeetingScheduleService;

    public User createUser(final Meeting meeting,
                           final Name userName,
                           final Role role) {
        User user = User.builder()
                .meeting(meeting)
                .name(userName)
                .role(role)
                .isFixed(false)
                .build();
        userRepository.save(user);
        return user;
    }

    @Transactional
    public UserMeetingTimeResponseDto createHostTime(final Long meetingId,
                                                     final Long userId,
                                                     final List<UserMeetingTimeSaveRequestDto> requestDtos) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException(Error.MEETING_NOT_FOUND_EXCEPTION));
        if (!meeting.authenticateHost(userId))
            throw new UnauthorizedException(INVALID_MEETING_HOST_EXCEPTION);
        if (!timeBlockUserService.isEmptyHostTimeBlock(meeting.getHost()))
            throw new ConflictException(Error.HOST_TIME_EXIST_EXCEPTION);

        isDuplicatedDate(requestDtos);
        requestDtos.forEach(requestDto -> createUserTimeBlock(meeting, meeting.getHost(), requestDto));

        String accessToken = jwtService.issuedToken(meeting.getHost().getId().toString());

        return UserMeetingTimeResponseDto.builder()
                .url(meeting.getUrl())
                .accessToken(accessToken)
                .build();
    }

    @Transactional
    public UserTimeResponseDto createUserTime(final Long meetingId,
                                              final AvailableTimeRequestDto requestDto) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException(Error.MEETING_NOT_FOUND_EXCEPTION));

        User user = createUser(meeting, new Name(requestDto.getName()), Role.MEMBER);
        isDuplicatedDate(requestDto.getAvailableTimes());
        requestDto.getAvailableTimes().forEach(availableTime -> createUserTimeBlock(meeting, user, availableTime));
        return UserTimeResponseDto.builder()
                .role(Role.MEMBER.getRole())
                .build();
    }

    public List<String> findUserNameByMeeting(final Meeting meeting) {
        List<User> users = userRepository.findByMeeting(meeting);
        if (users.isEmpty()) {
            throw new NotFoundException(USER_NOT_FOUND_EXCEPTION);
        }
        return users.stream()
                .map(User::getName)
                .collect(Collectors.toList());
    }

    private void createUserTimeBlock(final Meeting meeting,
                                     final User user,
                                     final UserMeetingTimeSaveRequestDto requestDto) {
        AvailableDate availableDate = availableDateService.findByMeetingAndDate(meeting, requestDto.month(), requestDto.day());
        TimeSlot.getTimeSlots(requestDto.startTime().ordinal(), requestDto.endTime().ordinal() - 1)
                .stream()
                .map(timeSlot -> timeBlockService.searchTimeBlock(timeSlot, availableDate, requestDto.priority())).collect(Collectors.toList())
                .forEach(timeBlock -> timeBlock.addTimeBlockUsers(timeBlockUserService.create(timeBlock, user)));
        userMeetingScheduleService.createUserMeetingSchedule(meeting.getId(), user.getId(), requestDto);
    }

    private void isDuplicatedDate(final List<UserMeetingTimeSaveRequestDto> requestDtoList) {
        Map<String, List<TimeSlot>> meetingTimeAvailable = new HashMap<>();
        for (UserMeetingTimeSaveRequestDto requestDto : requestDtoList) {
            String col = String.format("%s %s", requestDto.month(), requestDto.day());
            List<TimeSlot> timeSlots = TimeSlot.getTimeSlots(requestDto.startTime().ordinal(), requestDto.endTime().ordinal() - 1);
            if (meetingTimeAvailable.containsKey(col)) {
                if (meetingTimeAvailable.get(col).stream().anyMatch(timeSlots::contains)) {
                    throw new BadRequestException(Error.DUPLICATED_TIME_EXCEPTION);
                }
            } else {
                meetingTimeAvailable.put(col, timeSlots);
            }
        }
    }

    public List<String> getFixedUsers(final Meeting meeting) {
        return userRepository
                .findByMeetingAndIsFixed(meeting, true)
                .stream()
                .map(User::getName)
                .collect(Collectors.toList());
    }

    public void setFixedUsers(final Meeting meeting, final List<UserRequestDto> users) {
        List<Long> userIds = users.stream()
                .mapToLong(UserRequestDto::getId)
                .boxed()
                .collect(Collectors.toList());
        userRepository.updateUserIsFixedByMeeting(meeting, userIds);
    }

    public int getMeetingUserCount(final Meeting meeting) {
        return userRepository.countByMeeting(meeting);
    }

    public List<BestMeetingTimeWithUsersVo> getBestMeetingInUsers(
            final Long meetingId,
            final List<BestMeetingTimeVo> bestMeetingTimes
    ) {
        return bestMeetingTimes.stream()
                .map(bestMeetingTime -> getBestMeetingTimeInUsers(meetingId, bestMeetingTime))
                .collect(Collectors.toList());
    }

    private BestMeetingTimeWithUsersVo getBestMeetingTimeInUsers(
            final Long meetingId,
            final BestMeetingTimeVo bestMeetingTime
    ) {
        if (bestMeetingTime == null) {
            return null;
        }
        List<TimeSlot> timeSlots = TimeSlot.getTimeSlots(bestMeetingTime.startTime().ordinal(),
                bestMeetingTime.endTime().ordinal() - 1);
        List<UserVo> users = userRepository.findByAvailableDateAndTimeSlots(meetingId, bestMeetingTime.date(),
                timeSlots);
        return BestMeetingTimeWithUsersVo.of(bestMeetingTime, users);
    }
}
