package com.asap.server.persistence.domain.time;

import com.asap.server.persistence.domain.enums.TimeSlot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMeetingSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Long meetingId;
    @Column(nullable = false)
    private LocalDate availableDate;
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TimeSlot startTimeSlot;
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TimeSlot endTimeSlot;
    @ColumnDefault(value = "0")
    private int weight;

    @Builder
    private UserMeetingSchedule(
            final Long userId,
            final Long meetingId,
            final LocalDate availableDate,
            final TimeSlot startTimeSlot,
            final TimeSlot endTimeSlot,
            final int weight
    ) {
        this.userId = userId;
        this.meetingId = meetingId;
        this.availableDate = availableDate;
        this.startTimeSlot = startTimeSlot;
        this.endTimeSlot = endTimeSlot;
        this.weight = weight;
    }
}
