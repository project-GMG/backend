package eusyaeusya.gmg.domain.event.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceRecommendationEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishUpdate(Long eventId) {
        log.info("추천 업데이트 이벤트 발행: eventId={}", eventId);
        eventPublisher.publishEvent(new PlaceRecommendationUpdateEvent(eventId));
    }
}
