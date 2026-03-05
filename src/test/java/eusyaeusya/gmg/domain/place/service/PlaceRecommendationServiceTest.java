package eusyaeusya.gmg.domain.place.service;

import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.entity.EventPlaceType;
import eusyaeusya.gmg.domain.event.repository.EventPlaceTypeRepository;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.event.service.HeatmapService;
import eusyaeusya.gmg.domain.participant.entity.ParticipantStatus;
import eusyaeusya.gmg.domain.participant.repository.ParticipantDislikedCategoryRepository;
import eusyaeusya.gmg.domain.participant.repository.ParticipantDislikedPlaceRepository;
import eusyaeusya.gmg.domain.participant.repository.ParticipantRepository;
import eusyaeusya.gmg.domain.place.entity.Place;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.entity.PlaceCategory;
import eusyaeusya.gmg.domain.place.repository.PlaceRepository;
import eusyaeusya.gmg.domain.place.vo.CategoryRecommendations;
import eusyaeusya.gmg.domain.place.vo.PlaceRecommendation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

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

    @Mock
    private ParticipantDislikedPlaceRepository dislikedPlaceRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private HeatmapService heatmapService;

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

        given(dislikedCategoryRepository.countDislikesByCategory(any(), any()))
                .willReturn(new ArrayList<>());
        given(dislikedPlaceRepository.countDislikesByPlace(any(), any()))
                .willReturn(new ArrayList<>());
        given(participantRepository.countByEventIdAndStatus(any(), any()))
                .willReturn(0);
        given(heatmapService.calculateIntensityMap(any())).willReturn(Collections.emptyMap());

        // when
        List<CategoryRecommendations> results = placeRecommendationService.generateRecommendations(eventId);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().placeTypeName()).isEqualTo("식당");
        assertThat(results.getFirst().recommendations()).hasSize(1);
        assertThat(results.getFirst().recommendations().getFirst().placeName()).isEqualTo("맛집1");
    }
    @Test
    @DisplayName("사용자의 카테고리 비선호도가 장소 점수에 감점으로 반영된다")
    void generateRecommendations_AppliesDislikePenalty() {
        // given
        Long eventId = 2L;
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

        PlaceCategory koreanFood = mock(PlaceCategory.class);
        given(koreanFood.getId()).willReturn(50L);

        EventPlaceType eventPlaceType = mock(EventPlaceType.class);
        given(eventPlaceType.getPlaceType()).willReturn(restaurantType);
        given(eventPlaceTypeRepository.findByEventWithPlaceType(event)).willReturn(List.of(eventPlaceType));

        Place restaurant1 = mock(Place.class);
        given(restaurant1.getId()).willReturn(101L);
        given(restaurant1.getName()).willReturn("한식집");
        given(restaurant1.getPlaceType()).willReturn(restaurantType);
        given(restaurant1.getCategory()).willReturn(koreanFood);
        given(restaurant1.getRating()).willReturn(BigDecimal.valueOf(5.0)); // 기본 50점

        given(placeRepository.findPlacesWithinRadiusByPlaceTypeIds(
                anyList(), anyDouble(), anyDouble(), anyInt(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(List.of(restaurant1));

        // 한식 카테고리를 1명이 비선호함 (5점 감점 예상)
        List<Object[]> dislikeResults = new ArrayList<>();
        dislikeResults.add(new Object[]{50L, 1L});
        given(dislikedCategoryRepository.countDislikesByCategory(any(), any()))
                .willReturn(dislikeResults);
        given(dislikedPlaceRepository.countDislikesByPlace(any(), any()))
                .willReturn(new ArrayList<>());
        given(participantRepository.countByEventIdAndStatus(any(), any()))
                .willReturn(0);
        given(heatmapService.calculateIntensityMap(any())).willReturn(Collections.emptyMap());

        // when
        List<CategoryRecommendations> results = placeRecommendationService.generateRecommendations(eventId);

        // then
        assertThat(results).hasSize(1);
        PlaceRecommendation rec = results.getFirst().recommendations().getFirst();
        // 점수 계산: (5.0 * 10) - (1 * 5) + (1.0 * 3) = 50 - 5 + 3 = 48
        assertThat(rec.score()).isEqualTo(48.0);
    }

    @Test
    @DisplayName("완료된 참여자의 과반수 이상이 비선호한 장소는 추천에서 제외된다")
    void generateRecommendations_ExcludesPlacesDislikedByMajority() {
        // given
        Long eventId = 3L;
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
        given(restaurant1.getName()).willReturn("인기식당");
        given(restaurant1.getPlaceType()).willReturn(restaurantType);
        given(restaurant1.getRating()).willReturn(BigDecimal.valueOf(4.5));

        Place restaurant2 = mock(Place.class);
        given(restaurant2.getId()).willReturn(102L);

        given(placeRepository.findPlacesWithinRadiusByPlaceTypeIds(
                anyList(), anyDouble(), anyDouble(), anyInt(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(List.of(restaurant1, restaurant2));

        // 완료된 참여자 4명
        given(participantRepository.countByEventIdAndStatus(eventId, ParticipantStatus.COMPLETED))
                .willReturn(4);

        // 102번 장소를 2명이 비선호 (4명 중 2명 = 50% → 제외)
        List<Object[]> placeDislikeResults = new ArrayList<>();
        placeDislikeResults.add(new Object[]{102L, 2L});
        given(dislikedPlaceRepository.countDislikesByPlace(any(), any()))
                .willReturn(placeDislikeResults);

        given(dislikedCategoryRepository.countDislikesByCategory(any(), any()))
                .willReturn(new ArrayList<>());
        given(heatmapService.calculateIntensityMap(any())).willReturn(Collections.emptyMap());

        // when
        List<CategoryRecommendations> results = placeRecommendationService.generateRecommendations(eventId);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().recommendations()).hasSize(1);
        assertThat(results.getFirst().recommendations().getFirst().placeName()).isEqualTo("인기식당");
    }

    @Test
    @DisplayName("과반수 미만이 비선호한 장소는 추천에서 제외되지 않는다")
    void generateRecommendations_KeepsPlacesDislikedByMinority() {
        // given
        Long eventId = 4L;
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
        given(restaurant1.getName()).willReturn("식당A");
        given(restaurant1.getPlaceType()).willReturn(restaurantType);
        given(restaurant1.getRating()).willReturn(BigDecimal.valueOf(4.5));

        Place restaurant2 = mock(Place.class);
        given(restaurant2.getId()).willReturn(102L);
        given(restaurant2.getName()).willReturn("식당B");
        given(restaurant2.getPlaceType()).willReturn(restaurantType);
        given(restaurant2.getRating()).willReturn(BigDecimal.valueOf(4.0));

        given(placeRepository.findPlacesWithinRadiusByPlaceTypeIds(
                anyList(), anyDouble(), anyDouble(), anyInt(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(List.of(restaurant1, restaurant2));

        // 완료된 참여자 4명
        given(participantRepository.countByEventIdAndStatus(eventId, ParticipantStatus.COMPLETED))
                .willReturn(4);

        // 102번 장소를 1명만 비선호 (4명 중 1명 = 25% → 유지)
        List<Object[]> placeDislikeResults = new ArrayList<>();
        placeDislikeResults.add(new Object[]{102L, 1L});
        given(dislikedPlaceRepository.countDislikesByPlace(any(), any()))
                .willReturn(placeDislikeResults);

        given(dislikedCategoryRepository.countDislikesByCategory(any(), any()))
                .willReturn(new ArrayList<>());
        given(heatmapService.calculateIntensityMap(any())).willReturn(Collections.emptyMap());

        // when
        List<CategoryRecommendations> results = placeRecommendationService.generateRecommendations(eventId);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().recommendations()).hasSize(2);
    }
}
