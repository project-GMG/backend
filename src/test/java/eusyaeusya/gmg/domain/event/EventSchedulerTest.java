package eusyaeusya.gmg.domain.event;

import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventSchedulerTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventScheduler eventScheduler;

    @Test
    @DisplayName("7일이 지난 모임들을 EXPIRED 상태로 변경한다")
    void expireEvents_success() {
        // given
        Event event1 = mock(Event.class);
        Event event2 = mock(Event.class);
        List<Event> expiredEvents = List.of(event1, event2);

        given(eventRepository.findByStatusNotExpiredAndCreatedAtBefore(any(LocalDateTime.class)))
                .willReturn(expiredEvents);

        // when
        eventScheduler.expireEvents();

        // then
        verify(event1, times(1)).expire();
        verify(event2, times(1)).expire();
        verify(eventRepository, times(1)).findByStatusNotExpiredAndCreatedAtBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("만료할 모임이 없으면 아무 작업도 하지 않는다")
    void expireEvents_noAction_whenNoExpiredEvents() {
        // given
        given(eventRepository.findByStatusNotExpiredAndCreatedAtBefore(any(LocalDateTime.class)))
                .willReturn(List.of());

        // when
        eventScheduler.expireEvents();

        // then
        verify(eventRepository, times(1)).findByStatusNotExpiredAndCreatedAtBefore(any(LocalDateTime.class));
        verifyNoMoreInteractions(eventRepository);
    }
}
