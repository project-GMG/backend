package eusyaeusya.gmg.domain.place.service;

import eusyaeusya.gmg.domain.place.entity.PlaceCategory;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.repository.PlaceCategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PlaceCategoryServiceTest {
    @InjectMocks
    private PlaceCategoryService placeCategoryService;

    @Mock
    private PlaceCategoryRepository placeCategoryRepository;

    @Test
    @DisplayName("PlaceType별 카테고리 그룹핑 조회 성공")
    void success_getCategoriesGroupedByPlaceType() {
        // given
        PlaceType restaurant = createMockPlaceType(1L, "RESTAURANT");
        PlaceType cafe = createMockPlaceType(2L, "CAFE");

        PlaceCategory korean = createMockCategory(1L, restaurant, "한식", "KOREAN_FOOD");
        PlaceCategory chinese = createMockCategory(2L, restaurant, "중식", "CHINESE_FOOD");
        PlaceCategory franchiseCafe = createMockCategory(3L, cafe, "프랜차이즈", "FRANCHISE_CAFE");

        List<PlaceType> placeTypes = List.of(restaurant, cafe);
        List<PlaceCategory> categories = List.of(korean, chinese, franchiseCafe);

        given(placeCategoryRepository.findByPlaceTypeIn(placeTypes)).willReturn(categories);

        // when
        Map<PlaceType, List<PlaceCategory>> result = placeCategoryService.getCategoriesGroupedByPlaceType(placeTypes);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(restaurant)).hasSize(2);
        assertThat(result.get(restaurant)).containsExactly(korean, chinese);
        assertThat(result.get(cafe)).hasSize(1);
        assertThat(result.get(cafe)).containsExactly(franchiseCafe);

        verify(placeCategoryRepository).findByPlaceTypeIn(placeTypes);
    }

    @Test
    @DisplayName("특정 PlaceType의 카테고리 조회 성공")
    void success_getCategoriesByPlaceType() {
        // given
        PlaceType restaurant = createMockPlaceType(1L, "RESTAURANT");
        PlaceCategory korean = createMockCategory(1L, restaurant, "한식", "KOREAN_FOOD");
        PlaceCategory chinese = createMockCategory(2L, restaurant, "중식", "CHINESE_FOOD");

        List<PlaceCategory> categories = List.of(korean, chinese);
        given(placeCategoryRepository.findByPlaceTypeIn(List.of(restaurant))).willReturn(categories);

        // when
        Map<PlaceType, List<PlaceCategory>> result = placeCategoryService.getCategoriesGroupedByPlaceType(List.of(restaurant));

        // then
        assertThat(result.get(restaurant)).hasSize(2);
        assertThat(result).containsEntry(restaurant, categories);
        verify(placeCategoryRepository).findByPlaceTypeIn(List.of(restaurant));
    }

    @Test
    @DisplayName("카테고리가 없는 PlaceType 조회 시 빈 리스트 반환")
    void success_getCategoriesGroupedByPlaceType_emptyCategories() {
        // given
        PlaceType study = createMockPlaceType(4L, "STUDY");
        List<PlaceType> placeTypes = List.of(study);

        given(placeCategoryRepository.findByPlaceTypeIn(placeTypes)).willReturn(List.of());

        // when
        Map<PlaceType, List<PlaceCategory>> result = placeCategoryService.getCategoriesGroupedByPlaceType(placeTypes);

        // then
        assertThat(result).isEmpty();
        verify(placeCategoryRepository).findByPlaceTypeIn(placeTypes);
    }

    private PlaceType createMockPlaceType(Long id, String code) {
        return mock(PlaceType.class);
    }

    private PlaceCategory createMockCategory(Long id, PlaceType placeType, String name, String code) {
        PlaceCategory category = mock(PlaceCategory.class);
        given(category.getPlaceType()).willReturn(placeType);
        return category;
    }
}