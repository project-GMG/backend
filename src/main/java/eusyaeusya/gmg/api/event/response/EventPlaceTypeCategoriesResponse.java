package eusyaeusya.gmg.api.event.response;

import eusyaeusya.gmg.domain.place.entity.PlaceCategory;
import eusyaeusya.gmg.domain.place.entity.PlaceType;

import java.util.List;

public record EventPlaceTypeCategoriesResponse(
        List<PlaceTypeWithCategories> placeTypes
) {
    public record PlaceTypeWithCategories(
            Long id,
            String code,
            String label,
            List<CategoryInfo> categories
    ) {
        public static PlaceTypeWithCategories of(PlaceType placeType, List<PlaceCategory> placeCategories) {
            return new PlaceTypeWithCategories(
                    placeType.getId(),
                    placeType.getCode(),
                    placeType.getLabel(),
                    placeCategories.stream().map(CategoryInfo::from).toList()
            );
        }
    }

    public record CategoryInfo(
            Long id,
            String name,
            String code) {
        public static CategoryInfo from(PlaceCategory placeCategory) {
            return new CategoryInfo(placeCategory.getId(), placeCategory.getName(), placeCategory.getCode());
        }
    }
}
