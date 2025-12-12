package eusyaeusya.gmg.api.participant.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ParticipantUnavailableTimeRequest(
        @NotEmpty(message = "불가능한 시간대는 최소 1개 이상 선택해야 합니다")
        @Valid
        List<UnavailableTimeSlot> unavailableTimes
) {
    public record UnavailableTimeSlot(
            @NotNull(message = "날짜는 필수입니다")
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate date,

            @NotNull(message = "시작 시간은 필수입니다")
            @JsonFormat(pattern = "HH:mm")
            LocalTime startTime,

            @NotNull(message = "종료 시간은 필수입니다")
            @JsonFormat(pattern = "HH:mm")
            LocalTime endTime
    ) {
    }
}
