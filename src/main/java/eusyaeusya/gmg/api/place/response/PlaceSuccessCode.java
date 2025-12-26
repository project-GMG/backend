package eusyaeusya.gmg.api.place.response;

import eusyaeusya.gmg.common.api.response.code.SuccessCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum PlaceSuccessCode implements SuccessCode {
    PLACE_TYPES_RETRIEVED("PL-S0001", "장소 타입 목록을 조회했습니다"),
    PLACES_RETRIEVED("PL-S0002", "장소 목록 조회했습니다"),
    RECOMMENDATION_PLACE("PL-S0003", "추천 장소를 조회했습니다"),
    ;

    private final String value;
    private final String message;
}
