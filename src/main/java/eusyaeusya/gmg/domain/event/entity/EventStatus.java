package eusyaeusya.gmg.domain.event.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventStatus {

    OPEN("진행중", "참여자가 비선호 정보를 입력할 수 있는 상태"),
    CLOSED("마감", "모임장이 최종 확정한 상태"),
    EXPIRED("만료", "생성 후 일정 시간이 지나 접근이 불가능한 상태");

    private final String displayName;
    private final String description;

    public static EventStatus from(String value) {
        try {
            return EventStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("유효하지 않은 이벤트 상태입니다: %s (허용: OPEN, CLOSED)", value)
            );
        }
    }
}
