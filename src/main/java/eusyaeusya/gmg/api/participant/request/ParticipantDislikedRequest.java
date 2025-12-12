package eusyaeusya.gmg.api.participant.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ParticipantDislikedRequest(
        @NotNull(message = "비선호 카테고리 목록은 필수입니다 (빈 배열 가능)")
        List<Long> dislikedCategoryIds,

        @NotNull(message = "비선호 장소 목록은 필수입니다 (빈 배열 가능)")
        List<Long> dislikedPlaceIds
) {
}
