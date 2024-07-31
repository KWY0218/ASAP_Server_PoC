package com.asap.server.service.meeting.recommend.strategy.impl;

import com.asap.server.persistence.domain.enums.Duration;
import com.asap.server.persistence.repository.timeblock.dto.TimeBlockDto;
import com.asap.server.service.meeting.recommend.strategy.ContinuousMeetingTimeStrategy;
import com.asap.server.service.vo.BestMeetingTimeVo;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ContinuousMeetingTimeStrategyImpl implements ContinuousMeetingTimeStrategy {
    @Override
    public List<BestMeetingTimeVo> find(List<TimeBlockDto> timeBlocks, Duration duration) {
        int startIdx = 0;
        int endIdx = 1;

        List<BestMeetingTimeVo> response = new ArrayList<>();
        while (endIdx < timeBlocks.size()) {
            TimeBlockDto endTimeBlock = timeBlocks.get(endIdx - 1);
            TimeBlockDto nextTimeBlock = timeBlocks.get(endIdx);
            if (endTimeBlock.availableDate().isEqual(nextTimeBlock.availableDate())
                    && endTimeBlock.timeSlot().ordinal() + 1 == nextTimeBlock.timeSlot().ordinal()) {
                endIdx++;
                continue;
            }

            TimeBlockDto startTimeBlock = timeBlocks.get(startIdx);
            if (isSatisfiedDuration(startTimeBlock, endTimeBlock, duration)) {
                int weight = sumTimeBlocksWeight(timeBlocks, startIdx, endIdx - 1);
                response.add(new BestMeetingTimeVo(startTimeBlock.availableDate(), startTimeBlock.timeSlot(),
                        endTimeBlock.timeSlot(), weight));
            }
            startIdx = endIdx++;
        }

        if (startIdx < endIdx) {
            TimeBlockDto startTimeBlock = timeBlocks.get(startIdx);
            TimeBlockDto endTimeBlock = timeBlocks.get(endIdx - 1);
            if (isSatisfiedDuration(startTimeBlock, endTimeBlock, duration)) {
                int weight = sumTimeBlocksWeight(timeBlocks, startIdx, endIdx - 1);
                response.add(new BestMeetingTimeVo(startTimeBlock.availableDate(), startTimeBlock.timeSlot(),
                        endTimeBlock.timeSlot(), weight));
            }
        }
        return response;
    }

    private boolean isSatisfiedDuration(
            TimeBlockDto startTimeBlock,
            TimeBlockDto endTimeBlock,
            Duration duration
    ) {
        int blockCnt = endTimeBlock.timeSlot().ordinal() - startTimeBlock.timeSlot().ordinal();
        return blockCnt >= duration.getNeedBlock();
    }

    private int sumTimeBlocksWeight(
            final List<TimeBlockDto> timeBlocks,
            final int startIdx,
            final int endIdx
    ) {
        return timeBlocks
                .subList(startIdx, endIdx)
                .stream()
                .map(TimeBlockDto::weight)
                .reduce(0, Integer::sum);
    }
}
