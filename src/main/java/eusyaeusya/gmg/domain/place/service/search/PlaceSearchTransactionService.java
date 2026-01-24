package eusyaeusya.gmg.domain.place.service.search;

import eusyaeusya.gmg.api.event.response.EventErrorCode;
import eusyaeusya.gmg.common.api.exception.NotFoundException;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.place.entity.Place;
import eusyaeusya.gmg.domain.place.service.PlaceService;
import eusyaeusya.gmg.domain.place.service.enrichment.PlaceEnrichmentService;
import eusyaeusya.gmg.infra.kakao.dto.KakaoPlaceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceSearchTransactionService {

    private final PlaceFetchService placeFetchService;
    private final PlaceService placeService;
    private final PlaceEnrichmentService placeEnrichmentService;
    private final EventRepository eventRepository;

    /**
     * 장소 검색 및 저장
     */
    @Transactional
    public void fetchAndSavePlaces(Long eventId) {
        log.info("장소 검색 및 저장, 정보 보완 시작: eventId={}", eventId);

        Event event = getEvent(eventId);

        List<KakaoPlaceDto> placeDtos = placeFetchService.fetchPlacesForEvent(event);

        List<Place> savedPlaces = placeService.savePlaces(placeDtos);

        // 구글 API를 통한 정보 보완
        placeEnrichmentService.enrichPlaces(savedPlaces);

        log.info("장소 검색 및 저장, 정보 보완 완료: eventId={}, placeCount={}", eventId, savedPlaces.size());
    }

    /**
     * 검색 완료 상태로 업데이트
     */
    @Transactional
    public void updateStatusToCompleted(Long eventId) {
        Event event = getEvent(eventId);
        event.completePlaceSearch();

        log.info("장소 검색 상태 업데이트: eventId={}, status=COMPLETED", eventId);
    }

    /**
     * 검색 실패 상태로 업데이트
     */
    @Transactional
    public void updateStatusToFailed(Long eventId) {
        Event event = getEvent(eventId);
        event.failPlaceSearch();

        log.debug("장소 검색 상태 업데이트: eventId={}, status=FAILED", eventId);
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        EventErrorCode.EVENT_NOT_FOUND,
                        String.format("이벤트를 찾을 수 없습니다: %d", eventId)
                ));
    }
}
