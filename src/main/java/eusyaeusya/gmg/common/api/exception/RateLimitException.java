package eusyaeusya.gmg.common.api.exception;

import eusyaeusya.gmg.common.api.response.code.ErrorCode;
import lombok.Getter;

@Getter
public class RateLimitException extends RuntimeException {
    private final ErrorCode errorCode;

    public RateLimitException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
