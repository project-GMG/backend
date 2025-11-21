package eusyaeusya.gmg.common.api.exception;

import eusyaeusya.gmg.common.api.response.code.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum CommonErrorCode implements ErrorCode {
    INTERNAL_SERVER_ERROR("IA-0001"), // -> 나중에 규칙이 있는 에러코드로 변경 (IA-0001)
    ;

    private final String value;
}
