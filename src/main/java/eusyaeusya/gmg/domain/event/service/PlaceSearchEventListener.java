package eusyaeusya.gmg.domain.event.service;

import eusyaeusya.gmg.domain.place.service.PlaceSearchOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceSearchEventListener {

    private final PlaceSearchOrchestrator placeSearchOrchestrator;

    @Async("placeSearchExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePlaceSearchEvent(PlaceSearchEvent event) {
        log.info("장소 검색 이벤트 수신: eventId={}", event.eventId());
        placeSearchOrchestrator.fetchAndSave(event.eventId());
    }
}
