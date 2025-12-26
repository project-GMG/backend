package eusyaeusya.gmg.domain.event.util;

import eusyaeusya.gmg.config.sse.SseService;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.place.service.PlaceRecommendationService;
import eusyaeusya.gmg.domain.place.vo.CategoryRecommendations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceRecommendationEventListener {

    private final PlaceRecommendationService recommendationService;
    private final SseService sseService;
    private final EventRepository eventRepository;

    @EventListener
    public void handleRecommendationUpdate(PlaceRecommendationUpdateEvent event) {
        Long eventId = event.eventId();
        log.info("추천 업데이트 이벤트 처리 시작: eventId={}", eventId);

        try {
            Event eventEntity = eventRepository.findById(eventId)
                    .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
            String hashUrl = eventEntity.getHashUrl();

            List<CategoryRecommendations> recommendations =
                    recommendationService.generateRecommendations(eventId);

            sseService.broadcast(hashUrl, "place-recommendations", recommendations);

            log.info("추천 업데이트 완료: eventId={}, hashUrl={}, 카테고리 수={}",
                    eventId, hashUrl, recommendations.size());
        } catch (Exception e) {
            log.error("추천 업데이트 실패: eventId={}", eventId, e);
        }
    }
}
