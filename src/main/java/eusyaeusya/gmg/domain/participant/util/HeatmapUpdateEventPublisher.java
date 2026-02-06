package eusyaeusya.gmg.domain.participant.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeatmapUpdateEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public void publishUpdate(Long eventId, String hashUrl) {
        log.info("히트맵 업데이트 이벤트 발행: eventId={}, hashUrl={}", eventId, hashUrl);
        eventPublisher.publishEvent(new HeatmapUpdateEvent(eventId, hashUrl));
    }
}
