package eusyaeusya.gmg.common.health.response;

import eusyaeusya.gmg.common.api.response.code.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HealthCheckSuccessCode implements SuccessCode {
    HEALTH_CHECK_SUCCESS("HC-0001", "헬스 체크 성공"),
    ;

    private final String value;
    private final String message;
}
