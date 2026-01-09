package eusyaeusya.gmg.domain.place.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceSearchOrchestrator {

    private final PlaceSearchTransactionService transactionService;

    @Async("placeSearchExecutor")
    public void fetchAndSaveAsync(Long eventId) {
        log.info("비동기 장소 검색 시작: eventId={}, thread={}", eventId, Thread.currentThread().getName());

        try {
            // 1. 장소 검색 및 저장
            transactionService.fetchAndSavePlaces(eventId);

            // 2. 성공 시 상태 업데이트
            transactionService.updateStatusToCompleted(eventId);

            log.info("비동기 장소 검색 완료: eventId={}", eventId);

        } catch (Exception e) {
            log.error("비동기 장소 검색 실패: eventId={}", eventId, e);

            // 3. 실패 시 상태 업데이트
            updateStatusToFailedSafely(eventId);
        }
    }

    /**
     * 검색 실패 상태로 업데이트
     */
    private void updateStatusToFailedSafely(Long eventId) {
        try {
            transactionService.updateStatusToFailed(eventId);
        } catch (Exception e) {
            log.error("장소 검색 실패 상태 업데이트 실패: eventId={}", eventId, e);
        }
    }
}
