package eusyaeusya.gmg.domain.place.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaceSearchOrchestratorTest {

    @InjectMocks
    private PlaceSearchOrchestrator placeSearchOrchestrator;

    @Mock
    private PlaceSearchTransactionService transactionService;

    @Test
    @DisplayName("비동기 장소 검색 성공 시나리오")
    void shouldHandleFetchAndSaveSuccess() {
        // given
        Long eventId = 1L;

        // when
        placeSearchOrchestrator.fetchAndSaveAsync(eventId);

        // then
        verify(transactionService).fetchAndSavePlaces(eventId);
        verify(transactionService).updateStatusToCompleted(eventId);
    }

    @Test
    @DisplayName("비동기 장소 검색 실패 시나리오")
    void shouldHandleFetchAndSaveFailure() {
        // given
        Long eventId = 1L;
        doThrow(new RuntimeException("API Error"))
                .when(transactionService).fetchAndSavePlaces(eventId);

        // when
        placeSearchOrchestrator.fetchAndSaveAsync(eventId);

        // then
        verify(transactionService).fetchAndSavePlaces(eventId);
        verify(transactionService, never()).updateStatusToCompleted(eventId);
        verify(transactionService).updateStatusToFailed(eventId);
    }
}
