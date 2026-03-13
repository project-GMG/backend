package eusyaeusya.gmg.api.event.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "이벤트 생성 요청 데이터")
public record EventCreateRequest(
        @Schema(description = "모임이름", example = "전북대")
        @NotBlank(message = "모임 이름은 필수입니다")
        @Size(max = 50, message = "모임 이름은 최대 50자까지 입력 가능합니다")
        String title,

        @Schema(description = "장소 타입 (예: RESTAURANT, CAFE)", example = "[\"CAFE\"]")
        @NotNull(message = "장소 타입은 필수입니다")
        @Size(min = 1, message = "최소 1개 이상의 장소 타입을 선택해야 합니다")
        List<String> placeTypeCodes,

        @Schema(description = "위치 정보")
        @NotNull(message = "위치 정보는 필수입니다")
        @Valid
        LocationInfo location,

        @Schema(description = "선택 날짜 목록", example = "[\"2026-03-13\", \"2026-03-14\", \"2026-03-20\"]")
        @NotEmpty(message = "선택 날짜는 최소 1개 이상이어야 합니다")
        @Size(max = 35, message = "선택 날짜는 최대 35개까지 가능합니다")
        List<@NotNull(message = "날짜는 필수입니다") @FutureOrPresent(message = "선택 날짜는 현재 날짜 또는 이후여야 합니다") LocalDate> selectedDates,

        @Schema(description = "시간 범위")
        @NotNull(message = "시간 범위는 필수입니다")
        @Valid
        TimeRangeInfo timeRange

) {
    public record LocationInfo(
            @Schema(description = "위도", example = "35.8468")
            @NotNull(message = "위도는 필수입니다")
            @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다")
            @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다")
            BigDecimal centerLatitude,

            @Schema(description = "경도", example = "127.1294")
            @NotNull(message = "경도는 필수입니다")
            @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다")
            @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다")
            BigDecimal centerLongitude,

            @Schema(description = "장소명 (상세)", example = "충만치킨 전북대점")
            @Size(max = 255, message = "위치 이름은 최대 255자까지 입력 가능합니다")
            String locationName
    ) {
    }

    public record TimeRangeInfo(
            @Schema(description = "시작 시간", example = "12:30", type = "string", pattern = "HH:mm")
            @NotNull(message = "시작 시간은 필수입니다")
            LocalTime startTime,

            @Schema(description = "종료 시간", example = "22:00", type = "string", pattern = "HH:mm")
            @NotNull(message = "종료 시간은 필수입니다")
            LocalTime endTime
    ) {
    }
}
