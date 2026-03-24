package eusyaeusya.gmg.api.participant.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ParticipantUnavailableTimeRequest(
//        @NotEmpty(message = "불가능한 시간대는 최소 1개 이상 선택해야 합니다")
//        @Valid
        List<UnavailableTimeSlot> unavailableTimes
) {
    public record UnavailableTimeSlot(
            @Schema(description = "날짜", type = "string", format = "date")
            @NotNull(message = "날짜는 필수입니다")
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate date,

            @Schema(description = "시작 시간", example = "12:30", type = "string", pattern = "HH:mm")
            @NotNull(message = "시작 시간은 필수입니다")
            @JsonFormat(pattern = "HH:mm")
            LocalTime startTime,

            @Schema(description = "종료 시간", example = "13:00", type = "string", pattern = "HH:mm")
            @NotNull(message = "종료 시간은 필수입니다")
            @JsonFormat(pattern = "HH:mm")
            LocalTime endTime
    ) {
    }
}
