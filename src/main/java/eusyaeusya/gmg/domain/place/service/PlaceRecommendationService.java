package eusyaeusya.gmg.domain.place.service;

import eusyaeusya.gmg.api.event.response.EventErrorCode;
import eusyaeusya.gmg.common.api.exception.NotFoundException;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.repository.EventPlaceTypeRepository;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.participant.entity.ParticipantStatus;
import eusyaeusya.gmg.domain.participant.repository.ParticipantDislikedCategoryRepository;
import eusyaeusya.gmg.domain.place.entity.Place;
import eusyaeusya.gmg.domain.place.repository.PlaceRepository;
import eusyaeusya.gmg.domain.place.util.RecommendationScoreCalculator;
import eusyaeusya.gmg.domain.place.vo.CategoryRecommendations;
import eusyaeusya.gmg.domain.place.vo.PlaceRecommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceRecommendationService {

    private static final int MAX_RECOMMENDATIONS_PER_PLACE_TYPE = 3;

    private final EventRepository eventRepository;
    private final PlaceRepository placeRepository;
    private final EventPlaceTypeRepository eventPlaceTypeRepository;
    private final ParticipantDislikedCategoryRepository dislikedCategoryRepository;

    public List<CategoryRecommendations> generateRecommendations(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        EventErrorCode.EVENT_NOT_FOUND,
                        String.format("이벤트를 찾을 수 없습니다: eventId=%d", eventId)
                ));

        return generateRecommendations(event);
    }

    public EventRecommendationResult generateRecommendationsWithEventId(String hashUrl) {
        Event event = eventRepository.findByHashUrl(hashUrl)
                .orElseThrow(() -> new NotFoundException(
                        EventErrorCode.EVENT_NOT_FOUND,
                        String.format("이벤트를 찾을 수 없습니다: %s", hashUrl)
                ));

        List<CategoryRecommendations> recommendations = generateRecommendations(event);
        return new EventRecommendationResult(event.getId(), recommendations);
    }

    private List<CategoryRecommendations> generateRecommendations(Event event) {
        log.info("추천 장소 생성 시작: eventId={}", event.getId());
        // 1. 이벤트의 PlaceType 조회
        List<Long> eventPlaceTypeIds = eventPlaceTypeRepository.findByEventWithPlaceType(event).stream()
                .map(ept -> ept.getPlaceType().getId())
                .toList();

        // 2. 이벤트에 해당하는 PlaceType을 가진 active한 장소 조회
        List<Place> allPlaces = placeRepository.findAllByPlaceTypeIdInAndIsActiveTrue(eventPlaceTypeIds);

        // 3. 영업시간 필터링 - 최소 하루라도 영업하는 곳만
        List<Place> operatingPlaces = filterByOperatingHours(allPlaces, event);

        // 4. PlaceType별 비선호도 집계 (완료된 참여자만)
        Map<Long, Integer> placeTypeDislikes = countPlaceTypeDislikes(event.getId());

        // 5. PlaceType별로 그룹핑하고 점수 계산
        Map<Long, List<PlaceRecommendation>> recommendationsByPlaceType =
                groupAndScorePlaces(operatingPlaces, event, placeTypeDislikes);

        log.info("추천 장소 생성 완료: eventId={}", event.getId());
        // 6. PlaceType별 상위 3개씩 선택
        return selectTopRecommendations(recommendationsByPlaceType);
    }

    private List<Place> filterByOperatingHours(List<Place> places, Event event) {
        return places.stream()
                .filter(place -> {
                    int matchingDays = event.countDaysMatching(place::isOpenOn);
                    return matchingDays > 0; // 최소 하루라도 영업해야 함
                })
                .toList();
    }

    private Map<Long, Integer> countPlaceTypeDislikes(Long eventId) {
        // 완료된 참여자들의 카테고리 비선호를 PlaceType별로 집계
        List<Object[]> results = dislikedCategoryRepository
                .countDislikesByPlaceType(eventId, ParticipantStatus.COMPLETED);

        Map<Long, Integer> dislikes = new HashMap<>();
        for (Object[] result : results) {
            Long placeTypeId = (Long) result[0];
            Long count = (Long) result[1];
            dislikes.put(placeTypeId, count.intValue());
        }

        return dislikes;
    }

    private Map<Long, List<PlaceRecommendation>> groupAndScorePlaces(
            List<Place> places,
            Event event,
            Map<Long, Integer> placeTypeDislikes) {

        int totalDays = event.getTotalDays();

        return places.stream()
                .map(place -> {
                    Long placeTypeId = place.getPlaceType().getId();
                    int dislikeCount = placeTypeDislikes.getOrDefault(placeTypeId, 0);
                    int matchingDays = event.countDaysMatching(place::isOpenOn);
                    double score = RecommendationScoreCalculator.calculateScore(
                            place, event, dislikeCount
                    );

                    return new PlaceRecommendation(
                            place.getId(),
                            place.getName(),
                            place.getPlaceType().getLabel(),
                            score,
                            matchingDays,
                            totalDays,
                            placeTypeId
                    );
                })
                .collect(Collectors.groupingBy(PlaceRecommendation::placeTypeId));
    }

    private List<CategoryRecommendations> selectTopRecommendations(
            Map<Long, List<PlaceRecommendation>> recommendationsByPlaceType) {

        return recommendationsByPlaceType.values().stream()
                .map(recommendations -> {

                    // 점수 내림차순 정렬 후 상위 3개
                    List<PlaceRecommendation> topRecommendations = recommendations.stream()
                            .sorted(Comparator.comparingDouble(PlaceRecommendation::score).reversed())
                            .limit(MAX_RECOMMENDATIONS_PER_PLACE_TYPE)
                            .toList();

                    // PlaceTypeName은 첫 번째 추천에서 가져옴
                    String placeTypeName = topRecommendations.isEmpty()
                            ? "Unknown"
                            : topRecommendations.getFirst().placeTypeName();

                    return new CategoryRecommendations(placeTypeName, topRecommendations);
                })
                .toList();
    }

    public record EventRecommendationResult(
            Long eventId,
            List<CategoryRecommendations> recommendations
    ) {
    }
}