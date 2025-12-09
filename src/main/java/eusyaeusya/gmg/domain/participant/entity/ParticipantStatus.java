package eusyaeusya.gmg.domain.participant.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum ParticipantStatus {
    DRAFT("입력 중", "참여자가 정보를 입력할 수 있는 상태"),
    COMPLETED("입력 완료", "참여자가 정보를 모두 입력한 상태");

    private final String displayName;
    private final String description;
}