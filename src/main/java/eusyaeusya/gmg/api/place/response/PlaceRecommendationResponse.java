package eusyaeusya.gmg.api.place.response;

import eusyaeusya.gmg.domain.place.vo.CategoryRecommendations;
import eusyaeusya.gmg.domain.place.vo.PlaceRecommendation;

import java.util.List;

public record PlaceRecommendationResponse(
        List<CategoryRecommendation> recommendations
) {
    public static PlaceRecommendationResponse from(List<CategoryRecommendations> categoryRecommendations) {
        List<CategoryRecommendation> recommendations = categoryRecommendations.stream()
                .map(CategoryRecommendation::from)
                .toList();
        return new PlaceRecommendationResponse(recommendations);
    }

    public record CategoryRecommendation(
            String placeTypeName,
            List<PlaceInfo> places
    ) {
        public static CategoryRecommendation from(CategoryRecommendations vo) {
            List<PlaceInfo> places = vo.recommendations().stream()
                    .map(PlaceInfo::from)
                    .toList();
            return new CategoryRecommendation(vo.placeTypeName(), places);
        }
    }

    public record PlaceInfo(
            Long placeId,
            String placeName,
            double score,
            int matchingDays,
            int totalDays
    ) {
        public static PlaceInfo from(PlaceRecommendation vo) {
            return new PlaceInfo(
                    vo.placeId(),
                    vo.placeName(),
                    vo.score(),
                    vo.matchingDays(),
                    vo.totalDays()
            );
        }
    }
}
