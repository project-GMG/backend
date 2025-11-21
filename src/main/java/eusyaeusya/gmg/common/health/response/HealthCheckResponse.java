package eusyaeusya.gmg.common.health.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "헬스 체크 응답 본문")
public record HealthCheckResponse(
        @Schema(description = "서버 상태", example = "UP")
        String status,

        @Schema(description = "요청 시각", example = "2025-11-21T20:30:12")
        String timestamp
) {
}
