package eusyaeusya.gmg.api.participant.response;

import eusyaeusya.gmg.common.api.response.code.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = lombok.AccessLevel.PACKAGE)
@Getter
public enum ParticipantSuccessCode implements SuccessCode {
    PARTICIPANT_JOINED("PT-S0001", "참여자 등록이 완료되었습니다"),
    ;
    private final String value;
    private final String message;
}
