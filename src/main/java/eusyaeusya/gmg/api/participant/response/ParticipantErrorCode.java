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
    PARTICIPANT_NOT_BELONGS_TO_EVENT("PT-E0002", "이벤트에 해당하지 않는 참여자입니다"),
    PARTICIPANT_ALREADY_EXISTS("PT-E0003", "이미 존재하는 사용자입니다"),
    // 상태 관련 (PT-E0100 ~ PT-E0199)
    INVALID_UNAVAILABLE_DATE("PT-E01000", "참여할 수 없는 날입니다"),
    INVALID_UNAVAILABLE_TIME("PT-E01001", "참여할 수 없는 시간입니다");

    private final String value;
    private final String message;
}
