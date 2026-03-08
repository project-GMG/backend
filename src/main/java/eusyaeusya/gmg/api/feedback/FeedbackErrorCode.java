package eusyaeusya.gmg.api.feedback;

import eusyaeusya.gmg.common.api.response.code.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum FeedbackErrorCode implements ErrorCode {
    RATE_LIMIT_EXCEEDED("FB-E0001");

    private final String value;
}
