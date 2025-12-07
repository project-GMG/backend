package eusyaeusya.gmg.common.api.response.code;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum CommonErrorCode implements ErrorCode {
    INTERNAL_SERVER_ERROR("CM-E0001"),
    INVALID_REQUEST("CM-E0002"),
    RESOURCE_NOT_FOUND("CM-E0003");

    private final String value;
}
