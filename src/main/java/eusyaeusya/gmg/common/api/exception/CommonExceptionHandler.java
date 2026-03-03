package eusyaeusya.gmg.common.api.exception;

import eusyaeusya.gmg.common.api.response.ApiResponse;
import eusyaeusya.gmg.infra.kakao.exception.KakaoMapApiException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static eusyaeusya.gmg.common.api.response.code.CommonErrorCode.INTERNAL_SERVER_ERROR;
import static eusyaeusya.gmg.common.api.response.code.CommonErrorCode.INVALID_REQUEST;
import static eusyaeusya.gmg.common.api.response.code.CommonErrorCode.RESOURCE_NOT_FOUND;
import static org.springframework.http.ResponseEntity.internalServerError;

@Slf4j
@Order
@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(final NotFoundException ex,
                                                                    final HttpServletRequest request) {
        logWarn("Not found exception", ex.getErrorCode().getValue(), request, ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException(final BadRequestException ex,
                                                                      final HttpServletRequest request) {
        logWarn("Bad request exception", ex.getErrorCode().getValue(), request, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException ex,
            final HttpServletRequest request
    ) {
        logWarn("Method argument not valid exception", INVALID_REQUEST.getValue(), request, ex);
        String combinedErrorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + error.getDefaultMessage();
                    }
                    return error.getObjectName() + ": " + error.getDefaultMessage();
                })
                .collect(java.util.stream.Collectors.joining(" | "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(INVALID_REQUEST, combinedErrorMessage));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            final IllegalArgumentException ex,
            final HttpServletRequest request
    ) {
        logWarn("Illegal argument exception", INVALID_REQUEST.getValue(), request, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(INVALID_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleRequestBindingExceptions(
            final Exception ex,
            final HttpServletRequest request
    ) {
        logWarn("Request binding exception", INVALID_REQUEST.getValue(), request, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(INVALID_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
            final HttpRequestMethodNotSupportedException ex,
            final HttpServletRequest request
    ) {
        logWarn("Http method not supported", INVALID_REQUEST.getValue(), request, ex);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.fail(INVALID_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(
            final NoHandlerFoundException ex,
            final HttpServletRequest request
    ) {
        logWarn("No handler found", RESOURCE_NOT_FOUND.getValue(), request, ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(RESOURCE_NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(KakaoMapApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleKakaoMapApiException(
            final KakaoMapApiException ex,
            final HttpServletRequest request
    ) {
        logWarn("Kakao api exception", INTERNAL_SERVER_ERROR.getValue(), request, ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.fail(INTERNAL_SERVER_ERROR, ex.getMessage()));
    }

    // 모든 예외를 처리하는 기본 핸들러
    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleException(final Exception ex, final HttpServletRequest request) {
        logError("Unhandled exception", INTERNAL_SERVER_ERROR.getValue(), request, ex);
        return internalServerError().body(
                ApiResponse.fail(INTERNAL_SERVER_ERROR, "요청 처리 중 알 수 없는 오류가 발생했습니다.")
        );
    }

    private void logWarn(
            final String title,
            final String errorCode,
            final HttpServletRequest request,
            final Exception ex
    ) {
        log.warn("{}: code={}, method={}, path={}, exception={}",
                title,
                errorCode,
                request.getMethod(),
                request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex);
    }

    private void logError(
            final String title,
            final String errorCode,
            final HttpServletRequest request,
            final Exception ex
    ) {
        log.error("{}: code={}, method={}, path={}, exception={}",
                title,
                errorCode,
                request.getMethod(),
                request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex);
    }
}
