package eusyaeusya.gmg.common.api.exception;

import eusyaeusya.gmg.common.api.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static eusyaeusya.gmg.common.api.response.code.CommonErrorCode.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.internalServerError;

@Slf4j
@Order
@RestControllerAdvice
public class CommonExceptionHandler {
    // 리소스를 찾을 수 없는 예외 처리
    @ExceptionHandler//메서드가 특정 예외를 처리하게 만든다 -> 파라미터 타입을 기준으로 자동 매핑
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(final NoResourceFoundException ex) {
        log.warn("No resource found exception: {}", ex.getResourcePath());
        return ResponseEntity.notFound().build();
    }

    // 모든 예외를 처리하는 기본 핸들러
    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleException(final Exception ex) {
        log.error("Exception: {}", ex.getMessage());
        return internalServerError().body(ApiResponse.fail(INTERNAL_SERVER_ERROR, ex.getMessage()));
    }
}
