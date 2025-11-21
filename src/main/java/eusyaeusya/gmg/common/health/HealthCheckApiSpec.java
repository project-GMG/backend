package eusyaeusya.gmg.common.health;

import eusyaeusya.gmg.common.api.response.ApiResponse;
import eusyaeusya.gmg.common.health.response.HealthCheckResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Health Check API", description = "헬스 체크 API")
public interface HealthCheckApiSpec {
    @Operation(
            summary = "헬스 체크",
            description = "서버 상태와 타임스탬프를 포함한 응답을 반환합니다."
    )
    ApiResponse<HealthCheckResponse> healthCheck();
}
