package eusyaeusya.gmg.domain.place.service;

import eusyaeusya.gmg.api.event.response.EventErrorCode;
import eusyaeusya.gmg.common.api.exception.NotFoundException;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
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
    private final EventRepository eventRepository;

    /**
     * 장소 검색 및 저장
     */
    @Transactional
    public void fetchAndSavePlaces(Long eventId) {
        Event event = getEvent(eventId);

        List<KakaoPlaceDto> placeDtos = placeFetchService.fetchPlacesForEvent(event);

        placeService.savePlaces(placeDtos);

        log.info("장소 검색 및 저장 완료: eventId={}, placeCount={}", eventId, placeDtos.size());
    }

    /**
     * 검색 완료 상태로 업데이트
     */
    @Transactional
    public void updateStatusToCompleted(Long eventId) {
        Event event = getEvent(eventId);
        event.completePlaceSearch();

        log.debug("장소 검색 상태 업데이트: eventId={}, status=COMPLETED", eventId);
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
