package eusyaeusya.gmg.domain.participant.entity;

import eusyaeusya.gmg.api.participant.response.ParticipantErrorCode;
import eusyaeusya.gmg.common.api.exception.BadRequestException;
import eusyaeusya.gmg.domain.event.entity.Event;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "participant_unavailable_times",
        indexes = {
                @Index(name = "idx_event_date_time", columnList = "event_id, unavailable_date, unavailable_time_start"),
                @Index(name = "idx_participant_date", columnList = "participant_id, unavailable_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipantUnavailableTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @Column(name = "unavailable_date", nullable = false)
    private LocalDate unavailableDate;

    @Column(name = "unavailable_time_start", nullable = false)
    private LocalTime unavailableTimeStart;

    @Column(name = "unavailable_time_end", nullable = false)
    private LocalTime unavailableTimeEnd;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Builder
    private ParticipantUnavailableTime(
            Event event,
            Participant participant,
            LocalDate unavailableDate,
            LocalTime unavailableTimeStart,
            LocalTime unavailableTimeEnd
    ) {
        validateDate(event, unavailableDate);
        validateTime(event, unavailableTimeStart, unavailableTimeEnd);

        this.event = event;
        this.participant = participant;
        this.unavailableDate = unavailableDate;
        this.unavailableTimeStart = unavailableTimeStart;
        this.unavailableTimeEnd = unavailableTimeEnd;
        this.createdAt = java.time.LocalDateTime.now();
    }

    public static ParticipantUnavailableTime create(
            Event event,
            Participant participant,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    ) {
        return ParticipantUnavailableTime.builder()
                .event(event)
                .participant(participant)
                .unavailableDate(date)
                .unavailableTimeStart(startTime)
                .unavailableTimeEnd(endTime)
                .build();
    }

    private void validateDate(Event event, LocalDate unavailableDate) {
        if (!event.containsDate(unavailableDate)) {
            throw new BadRequestException(
                    ParticipantErrorCode.INVALID_UNAVAILABLE_DATE,
                    String.format("참여할 수 없는 날입니다: %s", unavailableDate)
            );
        }
    }

    private void validateTime(Event event, LocalTime unavailableTimeStart, LocalTime unavailableTimeEnd) {
        if (unavailableTimeStart.isBefore(event.getTimeStart())
                || unavailableTimeEnd.isAfter(event.getTimeEnd())) {
            throw new BadRequestException(
                    ParticipantErrorCode.INVALID_UNAVAILABLE_TIME,
                    String.format("참여할 수 없는 시간입니다: %s ~ %s", unavailableTimeStart, unavailableTimeEnd)
            );
        }
    }
}
