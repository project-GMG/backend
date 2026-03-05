package eusyaeusya.gmg.domain.participant.util;

import eusyaeusya.gmg.api.event.response.EventHeatmapStreamResponse;
import eusyaeusya.gmg.config.sse.SseService;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.event.service.HeatmapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeatmapUpdateEventListener {
    private final HeatmapService heatmapService;
    private final SseService sseService;
    private final EventRepository eventRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleHeatmapUpdate(HeatmapUpdateEvent event) {
        String hashUrl = event.hashUrl();
        Long eventId = event.eventId();

        log.info("히트맵 업데이트 이벤트 처리 시작: eventId={}, hashUrl={}", eventId, hashUrl);

        try {
            Event eventEntity = eventRepository.findById(eventId)
                    .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

            EventHeatmapStreamResponse heatmapData =
                    heatmapService.calculateHeatmapForStream(eventEntity);

            sseService.broadcast(hashUrl, "heatmap-update", heatmapData);

            log.info("히트맵 업데이트 완료: eventId={}, hashUrl={}", eventId, hashUrl);
        } catch (Exception e) {
            log.error("히트맵 업데이트 실패: eventId={}, hashUrl={}, exception={}", eventId, hashUrl, e.getClass().getSimpleName(), e);
        }
    }
}
