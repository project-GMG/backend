package eusyaeusya.gmg.api.place.response;

import eusyaeusya.gmg.common.api.response.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = lombok.AccessLevel.PACKAGE)
@Getter
public enum PlaceErrorCode implements ErrorCode {
    //조회 관련 (PL-E0001 ~ PL-E0009)
    PLACE_TYPE_NOT_FOUND("PL-E0001", "장소 타입을 찾을 수 없습니다"),
    ;
    private final String value;
    private final String message;
}
