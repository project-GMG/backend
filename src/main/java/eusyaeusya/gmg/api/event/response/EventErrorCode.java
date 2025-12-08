package eusyaeusya.gmg.api.event.response;

import eusyaeusya.gmg.common.api.response.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = lombok.AccessLevel.PACKAGE)
@Getter
public enum EventErrorCode implements ErrorCode {
    // 조회 관련 (EV-E0001 ~ EV-E0009)

    // 상태 관련 (EV-E0100 ~ EV-E0199)

    // 비즈니스 규칙 위반(EV-E0200 ~ EV-E0299)
    INVALID_DATE_RANGE("EV-E0200", "시작 날짜는 종료 날짜보다 이전이어야 합니다"),
    INVALID_TIME_RANGE("EV-E0201", "시작 시간은 종료 시간보다 이전이어야 합니다"),
    DATE_RANGE_TOO_LONG("EV-E0202", "날짜 범위는 최대 35(5주)일을 초과할 수 없습니다");

    private final String value;
    private final String message;
}
