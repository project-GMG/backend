package eusyaeusya.gmg.api.place.response;

import eusyaeusya.gmg.common.api.response.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = lombok.AccessLevel.PACKAGE)
@Getter
public enum PlaceErrorCode implements ErrorCode {
    //조회 관련 (PL-E0001 ~ PL-E0009)
    PLACE_TYPE_NOT_FOUND("PL-E0001", "장소 타입을 찾을 수 없습니다"),
    PLACE_CATEGORY_NOT_FOUND("PL-E0002", "장소 카테고리를 찾을 수 없습니다"),

    //상태 관련 (PL-E0100 ~ PL-E0199)
    CATEGORY_NOT_IN_EVENT_PLACE_TYPES("PL-E0100", "장소 카테고리는 이벤트 장소 타입에 속하지 않습니다");

    private final String value;
    private final String message;
}
