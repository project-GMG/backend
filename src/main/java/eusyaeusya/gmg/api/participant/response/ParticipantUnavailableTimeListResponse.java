package eusyaeusya.gmg.api.participant.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import eusyaeusya.gmg.domain.participant.entity.ParticipantUnavailableTime;

import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;
public record ParticipantUnavailableTimeListResponse(
        Long participantId,
        List<UnavailableTimeSlot> unavailableTimes
) {
    public record UnavailableTimeSlot(
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate date,

            @JsonFormat(pattern = "HH:mm")
            LocalTime startTime,

            @JsonFormat(pattern = "HH:mm")
            LocalTime endTime
    ){
        public static UnavailableTimeSlot from(ParticipantUnavailableTime time){
            return new UnavailableTimeSlot(
                    time.getUnavailableDate(),
                    time.getUnavailableTimeStart(),
                    time.getUnavailableTimeEnd()
            );
        }
    }

    public static ParticipantUnavailableTimeListResponse of(
            Long participantId,
            List<ParticipantUnavailableTime> times
    ){
        List<UnavailableTimeSlot> slots = times.stream()
                .map(UnavailableTimeSlot::from)
                .toList();
        return new ParticipantUnavailableTimeListResponse(participantId, slots);
    }
}
