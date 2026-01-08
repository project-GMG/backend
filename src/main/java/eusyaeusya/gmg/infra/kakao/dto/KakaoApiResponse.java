package eusyaeusya.gmg.infra.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record KakaoApiResponse<T>(
        Meta meta,
        List<T> documents
) {
    public record Meta(
            @JsonProperty("total_count") int totalCount,
            @JsonProperty("pageable_count") int pageableCount,
            @JsonProperty("is_end") boolean isEnd,
            @JsonProperty("same_name") SameName sameName) {
    }

    public record SameName(
            List<String> region,
            String keyword,
            @JsonProperty("selected_region") String selectedRegion
    ) {
    }
}
