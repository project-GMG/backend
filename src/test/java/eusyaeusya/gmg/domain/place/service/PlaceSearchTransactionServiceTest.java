package eusyaeusya.gmg.domain.place.service;

import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.place.service.enrichment.PlaceEnrichmentService;
import eusyaeusya.gmg.domain.place.service.search.PlaceFetchService;
import eusyaeusya.gmg.domain.place.service.search.PlaceSearchTransactionService;
import eusyaeusya.gmg.infra.kakao.dto.KakaoPlaceDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PlaceSearchTransactionServiceTest {

    @InjectMocks
    private PlaceSearchTransactionService placeSearchTransactionService;

    @Mock
    private PlaceFetchService placeFetchService;

    @Mock
    private PlaceService placeService;

    @Mock
    private PlaceEnrichmentService placeEnrichmentService;

    @Mock
    private EventRepository eventRepository;

    @Test
    @DisplayName("장소를 검색하고 저장한다")
    void shouldFetchAndSavePlaces() {
        // given
        Long eventId = 1L;
        Event event = mock(Event.class);
        given(eventRepository.findById(eventId)).willReturn(Optional.of(event));

        KakaoPlaceDto dto = mock(KakaoPlaceDto.class);
        List<KakaoPlaceDto> dtos = List.of(dto);
        given(placeFetchService.fetchPlacesForEvent(event)).willReturn(dtos);
        given(placeService.savePlaces(dtos)).willReturn(new ArrayList<>());

        // when
        placeSearchTransactionService.fetchAndSavePlaces(eventId);

        // then
        verify(eventRepository).findById(eventId);
        verify(placeFetchService).fetchPlacesForEvent(event);
        verify(placeService).savePlaces(dtos);
    }

    @Test
    @DisplayName("검색 완료 상태로 업데이트한다")
    void shouldUpdateStatusToCompleted() {
        // given
        Long eventId = 1L;
        Event event = mock(Event.class);
        given(eventRepository.findById(eventId)).willReturn(Optional.of(event));

        // when
        placeSearchTransactionService.updateStatusToCompleted(eventId);

        // then
        verify(eventRepository).findById(eventId);
        verify(event).completePlaceSearch();
    }

    @Test
    @DisplayName("검색 실패 상태로 업데이트한다")
    void shouldUpdateStatusToFailed() {
        // given
        Long eventId = 1L;
        Event event = mock(Event.class);
        given(eventRepository.findById(eventId)).willReturn(Optional.of(event));

        // when
        placeSearchTransactionService.updateStatusToFailed(eventId);

        // then
        verify(eventRepository).findById(eventId);
        verify(event).failPlaceSearch();
    }
}
