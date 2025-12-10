package eusyaeusya.gmg.domain.event.service;

import eusyaeusya.gmg.api.event.response.EventPlaceTypeCategoriesResponse;
import eusyaeusya.gmg.common.api.exception.NotFoundException;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.entity.EventPlaceType;
import eusyaeusya.gmg.domain.event.repository.EventPlaceTypeRepository;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.place.entity.PlaceCategory;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.service.PlaceCategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPlaceTypeServiceTest {
    @InjectMocks
    private EventPlaceTypeService eventPlaceTypeService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventPlaceTypeRepository eventPlaceTypeRepository;

    @Mock
    private PlaceCategoryService placeCategoryService;

    @Test
    @DisplayName("이벤트의 선택 가능한 카테고리 조회 성공")
    void success_getAvailableCategoriesForEvent() {
        // given
        String hashUrl = "abc123";
        Event event = mock(Event.class);
        given(event.getId()).willReturn(1L);

        PlaceType restaurant = createMockPlaceType(1L, "RESTAURANT", "식당");
        PlaceType cafe = createMockPlaceType(2L, "CAFE", "카페");

        EventPlaceType ept1 = createMockEventPlaceType(event, restaurant);
        EventPlaceType ept2 = createMockEventPlaceType(event, cafe);

        PlaceCategory korean = createMockCategory(1L, restaurant, "한식", "KOREAN_FOOD");
        PlaceCategory chinese = createMockCategory(2L, restaurant, "중식", "CHINESE_FOOD");
        PlaceCategory franchiseCafe = createMockCategory(3L, cafe, "프랜차이즈", "FRANCHISE_CAFE");

        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(event));
        given(eventPlaceTypeRepository.findByEventWithPlaceType(event)).willReturn(List.of(ept1, ept2));
        given(placeCategoryService.getCategoriesGroupedByPlaceType(List.of(restaurant, cafe)))
                .willReturn(Map.of(
                        restaurant, List.of(korean, chinese),
                        cafe, List.of(franchiseCafe)
                ));

        // when
        EventPlaceTypeCategoriesResponse response = eventPlaceTypeService.getAvailableCategoriesForEvent(hashUrl);

        // then
        assertThat(response).isNotNull();
        assertThat(response.placeTypes()).hasSize(2);

        EventPlaceTypeCategoriesResponse.PlaceTypeWithCategories restaurantData = response.placeTypes().get(0);
        assertThat(restaurantData.id()).isEqualTo(1L);
        assertThat(restaurantData.code()).isEqualTo("RESTAURANT");
        assertThat(restaurantData.label()).isEqualTo("식당");
        assertThat(restaurantData.categories()).hasSize(2);
        assertThat(restaurantData.categories().getFirst().name()).isEqualTo("한식");

        EventPlaceTypeCategoriesResponse.PlaceTypeWithCategories cafeData = response.placeTypes().get(1);
        assertThat(cafeData.id()).isEqualTo(2L);
        assertThat(cafeData.categories()).hasSize(1);

        verify(eventRepository).findByHashUrl(hashUrl);
        verify(eventPlaceTypeRepository).findByEventWithPlaceType(event);
        verify(placeCategoryService).getCategoriesGroupedByPlaceType(List.of(restaurant, cafe));
    }

    @Test
    @DisplayName("존재하지 않는 이벤트로 조회 시 예외 발생")
    void fail_getAvailableCategoriesForEvent_eventNotFound() {
        // given
        String hashUrl = "invalid";
        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> eventPlaceTypeService.getAvailableCategoriesForEvent(hashUrl))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("이벤트를 찾을 수 없습니다");

        verify(eventPlaceTypeRepository, never()).findByEventWithPlaceType(any());
        verify(placeCategoryService, never()).getCategoriesGroupedByPlaceType(any());
    }

    @Test
    @DisplayName("PlaceType에 카테고리가 없는 경우 빈 리스트 반환")
    void success_getAvailableCategoriesForEvent_noCategoriesForPlaceType() {
        // given
        String hashUrl = "abc123";
        Event event = mock(Event.class);
        PlaceType study = createMockPlaceType(4L, "STUDY", "스터디");
        EventPlaceType ept = createMockEventPlaceType(event, study);

        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(event));
        given(eventPlaceTypeRepository.findByEventWithPlaceType(event)).willReturn(List.of(ept));
        given(placeCategoryService.getCategoriesGroupedByPlaceType(List.of(study)))
                .willReturn(Map.of());

        // when
        EventPlaceTypeCategoriesResponse response = eventPlaceTypeService.getAvailableCategoriesForEvent(hashUrl);

        // then
        assertThat(response.placeTypes()).hasSize(1);
        assertThat(response.placeTypes().getFirst().categories()).isEmpty();
    }

    private PlaceType createMockPlaceType(Long id, String code, String label) {
        PlaceType placeType = mock(PlaceType.class);
        given(placeType.getId()).willReturn(id);
        given(placeType.getCode()).willReturn(code);
        given(placeType.getLabel()).willReturn(label);
        return placeType;
    }

    private PlaceCategory createMockCategory(Long id, PlaceType placeType, String name, String code) {
        PlaceCategory category = mock(PlaceCategory.class);
        given(category.getId()).willReturn(id);
        given(category.getName()).willReturn(name);
        given(category.getCode()).willReturn(code);
        return category;
    }

    private EventPlaceType createMockEventPlaceType(Event event, PlaceType placeType) {
        EventPlaceType ept = mock(EventPlaceType.class);
        given(ept.getPlaceType()).willReturn(placeType);
        return ept;
    }
}