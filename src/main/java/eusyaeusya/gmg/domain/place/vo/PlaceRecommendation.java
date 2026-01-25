package eusyaeusya.gmg.domain.place.vo;

public record PlaceRecommendation(Long placeId, String placeName, String placeTypeName, String imageUrl, double score,
                                  int matchingDays,
                                  int totalDays, Long placeTypeId) {

    public double getMatchingRate() {
        return totalDays == 0 ? 0 : (double) matchingDays / totalDays;
    }
}
