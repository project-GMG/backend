package eusyaeusya.gmg.api.event.response;

import eusyaeusya.gmg.common.api.response.code.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = lombok.AccessLevel.PACKAGE)
@Getter
public enum EventSuccessCode implements SuccessCode {
    EVENT_RETRIEVED("EV-S0001", "이벤트 생성 성공했습니다."),
    EVENT_PLACE_TYPES_CATEGORIES_RETRIEVED("EV-S0002", "장소 카테고리 조회 성공했습니다"),
    EVENT_MAIN_RETRIEVED("EV-S0003", "메인 페이지 정보 조회 성공했습니다"),
    ;

    private final String value;
    private final String message;
}
