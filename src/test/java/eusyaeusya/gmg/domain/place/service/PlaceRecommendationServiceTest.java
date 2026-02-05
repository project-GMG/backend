package eusyaeusya.gmg.domain.place.service;

import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.entity.EventPlaceType;
import eusyaeusya.gmg.domain.event.repository.EventPlaceTypeRepository;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.participant.repository.ParticipantDislikedCategoryRepository;
import eusyaeusya.gmg.domain.place.entity.Place;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.repository.PlaceRepository;
import eusyaeusya.gmg.domain.place.vo.CategoryRecommendations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PlaceRecommendationServiceTest {

    @InjectMocks
    private PlaceRecommendationService placeRecommendationService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private EventPlaceTypeRepository eventPlaceTypeRepository;

    @Mock
    private ParticipantDislikedCategoryRepository dislikedCategoryRepository;

    @Test
    @DisplayName("이벤트 위치 기준 반경 내에서 설정된 PlaceType에 해당하는 장소들만 추천한다")
    void generateRecommendations_FiltersByEventLocationAndPlaceTypes() {
        // given
        Long eventId = 1L;
        Event event = mock(Event.class);
        given(event.getId()).willReturn(eventId);
        given(event.getTotalDays()).willReturn(1);
        given(event.countDaysMatching(any())).willReturn(1);
        given(event.getCenterLatitude()).willReturn(BigDecimal.valueOf(37.5665));
        given(event.getCenterLongitude()).willReturn(BigDecimal.valueOf(126.9780));
        given(eventRepository.findById(eventId)).willReturn(Optional.of(event));

        PlaceType restaurantType = mock(PlaceType.class);
        given(restaurantType.getId()).willReturn(1L);
        given(restaurantType.getLabel()).willReturn("식당");

        EventPlaceType eventPlaceType = mock(EventPlaceType.class);
        given(eventPlaceType.getPlaceType()).willReturn(restaurantType);
        given(eventPlaceTypeRepository.findByEventWithPlaceType(event)).willReturn(List.of(eventPlaceType));

        Place restaurant1 = mock(Place.class);
        given(restaurant1.getId()).willReturn(101L);
        given(restaurant1.getName()).willReturn("맛집1");
        given(restaurant1.getPlaceType()).willReturn(restaurantType);
        given(restaurant1.getRating()).willReturn(BigDecimal.valueOf(4.5));

        given(placeRepository.findPlacesWithinRadiusByPlaceTypeIds(
                anyList(), anyDouble(), anyDouble(), anyInt(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(List.of(restaurant1));

        given(dislikedCategoryRepository.countDislikesByPlaceType(any(), any()))
                .willReturn(new ArrayList<>());

        // when
        List<CategoryRecommendations> results = placeRecommendationService.generateRecommendations(eventId);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().placeTypeName()).isEqualTo("식당");
        assertThat(results.getFirst().recommendations()).hasSize(1);
        assertThat(results.getFirst().recommendations().getFirst().placeName()).isEqualTo("맛집1");
    }
}
