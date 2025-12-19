package eusyaeusya.gmg.api.event.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Builder
public record EventMainResponse(
        Long eventId,
        String hashUrl,
        String title,
        String status,
        List<PlaceTypeInfo> placeTypes,
        LocationInfo location,
        DateRangeInfo dateRange,
        TimeRangeInfo timeRange,
        Integer participantCount,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        List<HeatmapSlot> heatmapData
) {
    @Builder
    public record PlaceTypeInfo(
            Long id,
            String code,
            String label
    ) {
    }

    @Builder
    public record LocationInfo(
            BigDecimal centerLatitude,
            BigDecimal centerLongitude,
            String locationName
    ) {
    }

    @Builder
    public record DateRangeInfo(
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate startDate,
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate endDate
    ) {
    }

    @Builder
    public record TimeRangeInfo(
            @JsonFormat(pattern = "HH:mm")
            LocalTime startTime,
            @JsonFormat(pattern = "HH:mm")
            LocalTime endTime
    ) {
    }

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
