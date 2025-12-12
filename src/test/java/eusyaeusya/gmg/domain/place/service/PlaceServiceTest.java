package eusyaeusya.gmg.domain.place.service;

import eusyaeusya.gmg.api.place.response.PlaceErrorCode;
import eusyaeusya.gmg.api.place.response.PlaceListResponse;
import eusyaeusya.gmg.common.api.exception.BadRequestException;
import eusyaeusya.gmg.common.api.exception.NotFoundException;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.entity.EventPlaceType;
import eusyaeusya.gmg.domain.event.repository.EventPlaceTypeRepository;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.place.entity.PlaceCategory;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.repository.PlaceCategoryRepository;
import eusyaeusya.gmg.domain.place.repository.PlaceRepository;
import eusyaeusya.gmg.domain.place.repository.PlaceSimpleProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @InjectMocks
    private PlaceService placeService;

    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private PlaceCategoryRepository placeCategoryRepository;
    @Mock
    private EventPlaceTypeRepository eventPlaceTypeRepository;

    @Test
    @DisplayName("유효한(반경 내) 요청시 매장 목록을 반환한다")
    void success_getPlacesWithinRadius() {
        // given
        String hashUrl = "valid-hash";
        Long categoryId = 1L;
        int page = 0;
        int size = 10;

        Event event = mock(Event.class);
        given(event.getId()).willReturn(100L);
        given(event.getCenterLatitude()).willReturn(BigDecimal.valueOf(37.5665));
        given(event.getCenterLongitude()).willReturn(BigDecimal.valueOf(126.9780));

        PlaceType placeType = mock(PlaceType.class);
        PlaceCategory category = mock(PlaceCategory.class);
        given(category.getPlaceType()).willReturn(placeType);

        EventPlaceType eventPlaceType = mock(EventPlaceType.class);
        given(eventPlaceType.getPlaceType()).willReturn(placeType);

        PlaceSimpleProjection placeProjection = mock(PlaceSimpleProjection.class);
        given(placeProjection.getId()).willReturn(1L);
        given(placeProjection.getName()).willReturn("Test Place");

        Slice<PlaceSimpleProjection> placeSlice = new SliceImpl<>(List.of(placeProjection));

        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(event));
        given(placeCategoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(eventPlaceTypeRepository.findByEventWithPlaceType(event)).willReturn(List.of(eventPlaceType));

        given(placeRepository.findPlacesWithinRadius(
                eq(categoryId),
                anyDouble(), anyDouble(), anyInt(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                any(PageRequest.class)
        )).willReturn(placeSlice);

        // when
        PlaceListResponse response = placeService.getPlacesWithinRadius(hashUrl, categoryId, page, size);

        // then
        assertThat(response).isNotNull();
        assertThat(response.places()).hasSize(1);
        assertThat(response.hasNext()).isFalse();

        verify(eventRepository).findByHashUrl(hashUrl);
        verify(placeCategoryRepository).findById(categoryId);
        verify(eventPlaceTypeRepository).findByEventWithPlaceType(event);
        verify(placeRepository).findPlacesWithinRadius(
                eq(categoryId), anyDouble(), anyDouble(), eq(500),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(), any(PageRequest.class)
        );
    }

    @Test
    @DisplayName("존재하지 않는 hashUrl이면 예외가 발생한다")
    void fail_getPlacesWithinRadius_EventNotFound() {
        // given
        String hashUrl = "invalid-hash";
        Long categoryId = 1L;

        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> placeService.getPlacesWithinRadius(hashUrl, categoryId, 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("이벤트를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 categoryId이면 예외가 발생한다")
    void fail_getPlacesWithinRadius_CategoryNotFound() {
        // given
        String hashUrl = "valid-hash";
        Long categoryId = 999L;

        Event event = mock(Event.class);
        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(event));
        given(placeCategoryRepository.findById(categoryId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> placeService.getPlacesWithinRadius(hashUrl, categoryId, 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("장소 카테고리를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("이벤트에서 선택하지 않은 장소 타입의 카테고리 요청시 예외가 발생한다")
    void fail_getPlacesWithinRadius_CategoryNotInEvent() {
        // given
        String hashUrl = "valid-hash";
        Long categoryId = 1L;

        Event event = mock(Event.class);

        // 카테고리의 PlaceType
        PlaceType categoryPlaceType = mock(PlaceType.class);
        PlaceCategory category = mock(PlaceCategory.class);
        given(category.getId()).willReturn(categoryId);
        given(category.getPlaceType()).willReturn(categoryPlaceType);

        // 이벤트에 설정된 PlaceType (카테고리의 타입과 다른 객체)
        PlaceType otherPlaceType = mock(PlaceType.class);
        EventPlaceType eventPlaceType = mock(EventPlaceType.class);
        given(eventPlaceType.getPlaceType()).willReturn(otherPlaceType);

        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(event));
        given(placeCategoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(eventPlaceTypeRepository.findByEventWithPlaceType(event)).willReturn(List.of(eventPlaceType));

        // when & then
        assertThatThrownBy(() -> placeService.getPlacesWithinRadius(hashUrl, categoryId, 0, 10))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", PlaceErrorCode.CATEGORY_NOT_IN_EVENT_PLACE_TYPES);
    }

    @Test
    @DisplayName("조건은 만족하지만 매장이 없는 경우 빈 리스트를 반환한다")
    void success_getPlacesWithinRadius_EmptyResult() {
        // given
        String hashUrl = "valid-hash";
        Long categoryId = 1L;

        Event event = mock(Event.class);
        given(event.getId()).willReturn(100L);
        given(event.getCenterLatitude()).willReturn(BigDecimal.valueOf(37.5665));
        given(event.getCenterLongitude()).willReturn(BigDecimal.valueOf(126.9780));

        PlaceType placeType = mock(PlaceType.class);
        PlaceCategory category = mock(PlaceCategory.class);
        given(category.getPlaceType()).willReturn(placeType);

        EventPlaceType eventPlaceType = mock(EventPlaceType.class);
        given(eventPlaceType.getPlaceType()).willReturn(placeType);

        Slice<PlaceSimpleProjection> emptySlice = new SliceImpl<>(Collections.emptyList());

        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(event));
        given(placeCategoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(eventPlaceTypeRepository.findByEventWithPlaceType(event)).willReturn(List.of(eventPlaceType));

        given(placeRepository.findPlacesWithinRadius(
                any(), anyDouble(), anyDouble(), anyInt(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()
        )).willReturn(emptySlice);

        // when
        PlaceListResponse response = placeService.getPlacesWithinRadius(hashUrl, categoryId, 0, 10);

        // then
        assertThat(response.places()).isEmpty();
        assertThat(response.hasNext()).isFalse();
    }
}