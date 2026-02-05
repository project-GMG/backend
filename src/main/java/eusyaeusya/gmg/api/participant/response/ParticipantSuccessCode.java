package eusyaeusya.gmg.api.participant.response;

import eusyaeusya.gmg.common.api.response.code.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = lombok.AccessLevel.PACKAGE)
@Getter
public enum ParticipantSuccessCode implements SuccessCode {
    PARTICIPANT_JOINED("PT-S0001", "참여자 등록이 완료되었습니다"),
    UNAVAILABLE_TIME_REGISTERED("PT-S0002", "불가능한 시간 등록이 완료되었습니다"),
    DISLIKED_REGISTERED("PT-S0003", "비선호 카테고리 및 장소 등록이 완료되었습니다"),
    PARTICIPANT_COMPLETED("PT-S0004", "참여자 정보 입력이 완료되었습니다"),
    UNAVAILABLE_TIME_RETRIEVED("PT-S0005","참여자의 불가능한 시간을 조회했습니다")
    ;
    private final String value;
    private final String message;
}
