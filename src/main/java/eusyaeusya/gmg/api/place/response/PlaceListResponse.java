package eusyaeusya.gmg.api.place.response;

import eusyaeusya.gmg.domain.place.repository.PlaceSimpleProjection;

import java.util.List;

public record PlaceListResponse(
        List<PlaceInfo> places,
        boolean hasNext
) {
    public static PlaceListResponse from(List<PlaceSimpleProjection> projections, boolean hasNext) {
        return new PlaceListResponse(
                projections.stream()
                        .map(PlaceInfo::from)
                        .toList(),
                hasNext
        );
    }

    public record PlaceInfo(
            Long id,
            String name,
            String imageUrl
    ) {
        public static PlaceInfo from(PlaceSimpleProjection projection) {
            return new PlaceInfo(
                    projection.getId(),
                    projection.getName(),
                    projection.getImageUrl()
            );
        }
    }
}
