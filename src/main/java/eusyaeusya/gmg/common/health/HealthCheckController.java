package eusyaeusya.gmg.common.health;

import eusyaeusya.gmg.common.api.response.ApiResponse;
import eusyaeusya.gmg.common.health.response.HealthCheckResponse;
import eusyaeusya.gmg.common.health.response.HealthCheckSuccessCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController implements HealthCheckApiSpec {
    @Override
    @GetMapping
    public ApiResponse<HealthCheckResponse> healthCheck() {
        HealthCheckResponse data = new HealthCheckResponse(
                "UP",
                LocalDateTime.now().toString()
        );

        return ApiResponse.successWithData(
                HealthCheckSuccessCode.HEALTH_CHECK_SUCCESS,
                data
        );
    }
}
