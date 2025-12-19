package eusyaeusya.gmg.api.event.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
public record EventHeatmapStreamResponse(
        Long eventId,
        List<HeatmapSlot> heatmapData
) {
    @Builder
    public record HeatmapSlot(
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate date,
            @JsonFormat(pattern = "HH:mm")
            LocalTime timeSlot,
            Integer availableCount,
            Double intensity
    ) {
    }
}
