package eusyaeusya.gmg.api.participant.response;

import eusyaeusya.gmg.common.api.response.code.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum ParticipantErrorCode implements ErrorCode {
    // 조회 관련 (PT-E0001 ~ PT-E0009)
    PARTICIPANT_NOT_FOUND("PT-E0001", "참여자를 찾을 수 없습니다"),

    // 상태 관련 (PT-E0100 ~ PT-E0199)
    ;

    private final String value;
    private final String message;
}
