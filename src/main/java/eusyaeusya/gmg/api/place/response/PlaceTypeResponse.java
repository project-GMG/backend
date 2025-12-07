package eusyaeusya.gmg.api.place.response;

import eusyaeusya.gmg.domain.place.entity.PlaceType;

public record PlaceTypeResponse(
        Long id,
        String code,
        String label
) {
    public static PlaceTypeResponse from(final PlaceType placeType) {
        return new PlaceTypeResponse(
                placeType.getId(),
                placeType.getCode(),
                placeType.getLabel()
        );
    }
}
