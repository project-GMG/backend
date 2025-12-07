package eusyaeusya.gmg.api.event.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record EventCreateRequest(
        @NotBlank(message = "모임 이름은 필수입니다")
        @Size(max = 50, message = "모임 이름은 최대 50자까지 입력 가능합니다")
        String title,

        @NotNull(message = "장소 타입은 필수입니다")
        @Size(min = 1, message = "최소 1개 이상의 장소 타입을 선택해야 합니다")
        List<String> placeTypeCodes,

        @NotNull(message = "위치 정보는 필수입니다")
        @Valid
        LocationInfo location,

        @NotNull(message = "날짜 범위는 필수입니다")
        @Valid
        DateRangeInfo dateRange,

        @NotNull(message = "시간 범위는 필수입니다")
        @Valid
        TimeRangeInfo timeRange

) {
    public record LocationInfo(
            @NotNull(message = "위도는 필수입니다")
            @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다")
            @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다")
            BigDecimal centerLatitude,

            @NotNull(message = "경도는 필수입니다")
            @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다")
            @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다")
            BigDecimal centerLongitude,

            @Size(max = 255, message = "위치 이름은 최대 255자까지 입력 가능합니다")
            String locationName
    ) {
    }

    public record DateRangeInfo(
            @NotNull(message = "시작 날짜는 필수입니다")
            @Future(message = "시작 날짜는 현재 날짜 이후여야 합니다")
            LocalDate startDate,

            @NotNull(message = "종료 날짜는 필수입니다")
            LocalDate endDate
    ) {
    }

    public record TimeRangeInfo(
            @NotNull(message = "시작 시간은 필수입니다")
            LocalTime startTime,

            @NotNull(message = "종료 시간은 필수입니다")
            LocalTime endTime
    ) {
    }
}
