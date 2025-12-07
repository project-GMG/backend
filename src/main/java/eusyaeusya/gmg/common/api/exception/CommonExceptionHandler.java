package eusyaeusya.gmg.common.api.exception;

import eusyaeusya.gmg.common.api.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static eusyaeusya.gmg.common.api.response.code.CommonErrorCode.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.internalServerError;

@Slf4j
@Order
@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(final NotFoundException ex) {
        log.warn("Not found exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException(final BadRequestException ex) {
        log.warn("Bad request exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(ex.getErrorCode(), ex.getMessage()));
    }


    // 모든 예외를 처리하는 기본 핸들러
    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleException(final Exception ex) {
        log.error("Exception: {}", ex.getMessage());
        return internalServerError().body(ApiResponse.fail(INTERNAL_SERVER_ERROR, ex.getMessage()));
    }
}
