package eusyaeusya.gmg.api.place.response;

import eusyaeusya.gmg.common.api.response.code.SuccessCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum PlaceSuccessCode implements SuccessCode {
    PLACE_TYPES_RETRIEVED("PL-S0001", "장소 타입 목록을 조회했습니다"),
    ;
    private final String value;
    private final String message;
}
