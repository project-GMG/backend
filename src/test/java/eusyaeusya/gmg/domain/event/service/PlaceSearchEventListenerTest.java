package eusyaeusya.gmg.domain.event.service;

import eusyaeusya.gmg.domain.place.service.listener.PlaceSearchEvent;
import eusyaeusya.gmg.domain.place.service.listener.PlaceSearchEventListener;
import eusyaeusya.gmg.domain.place.service.search.PlaceSearchOrchestrator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PlaceSearchEventListenerTest {

    @InjectMocks
    private PlaceSearchEventListener placeSearchEventListener;

    @Mock
    private PlaceSearchOrchestrator placeSearchOrchestrator;

    @Test
    @DisplayName("장소 검색 이벤트를 수신하면 오케스트레이터를 호출한다")
    void shouldHandlePlaceSearchEvent() {
        // given
        Long eventId = 1L;
        PlaceSearchEvent event = new PlaceSearchEvent(eventId);

        // when
        placeSearchEventListener.handlePlaceSearchEvent(event);

        // then
        verify(placeSearchOrchestrator, times(1)).fetchAndSave(eventId);
    }
}
