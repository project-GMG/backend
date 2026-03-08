package eusyaeusya.gmg.api.feedback;

import eusyaeusya.gmg.common.api.response.code.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = lombok.AccessLevel.PACKAGE)
@Getter
public enum FeedbackSuccessCode implements SuccessCode {
    FEEDBACK_SUBMITTED("FB-S0001", "피드백이 성공적으로 접수되었습니다."),
    ;

    private final String value;
    private final String message;
}
