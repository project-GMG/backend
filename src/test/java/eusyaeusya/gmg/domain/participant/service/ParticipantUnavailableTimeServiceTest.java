package eusyaeusya.gmg.domain.participant.service;

import eusyaeusya.gmg.api.event.response.EventErrorCode;
import eusyaeusya.gmg.api.event.response.EventHeatmapStreamResponse;
import eusyaeusya.gmg.api.participant.request.ParticipantUnavailableTimeRequest;
import eusyaeusya.gmg.api.participant.response.ParticipantErrorCode;
import eusyaeusya.gmg.api.participant.response.ParticipantUnavailableTimeResponse;
import eusyaeusya.gmg.common.api.exception.BadRequestException;
import eusyaeusya.gmg.common.api.exception.NotFoundException;
import eusyaeusya.gmg.config.sse.SseService;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.event.service.HeatmapService;
import eusyaeusya.gmg.domain.participant.entity.Participant;
import eusyaeusya.gmg.domain.participant.entity.ParticipantUnavailableTime;
import eusyaeusya.gmg.domain.participant.repository.ParticipantRepository;
import eusyaeusya.gmg.domain.participant.repository.ParticipantUnavailableTimeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantUnavailableTimeServiceTest {
    @InjectMocks
    private ParticipantUnavailableTimeService unavailableTimeService;

    @Mock
    private ParticipantUnavailableTimeRepository unavailableTimeRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private HeatmapService heatmapService;

    @Mock
    private SseService sseService;

    @Test
    @DisplayName("불가능 시간 등록 성공")
    void success_registerUnavailableTimes() {
        // given
        String hashUrl = "hash123";
        Long participantId = 1L;

        Event event = mockOpenEvent();
        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(event));

        Participant participant = mock(Participant.class);
        given(participant.isNotBelongsToEvent(event)).willReturn(false);
        given(participantRepository.findById(participantId)).willReturn(Optional.of(participant));

        ParticipantUnavailableTimeRequest request = createValidRequest(event);

        // HeatmapService와 SseService Mock 설정
        EventHeatmapStreamResponse mockHeatmapResponse = mock(EventHeatmapStreamResponse.class);
        given(heatmapService.calculateHeatmapForStream(event)).willReturn(mockHeatmapResponse);

        // when
        ParticipantUnavailableTimeResponse response = unavailableTimeService.registerUnavailableTimes(
                hashUrl, participantId, request
        );

        // then
        assertThat(response.participantId()).isEqualTo(participantId);
        assertThat(response.registeredCount()).isEqualTo(request.unavailableTimes().size());

        ArgumentCaptor<List<ParticipantUnavailableTime>> captor = ArgumentCaptor.forClass(List.class);
        then(unavailableTimeRepository).should().deleteAllByParticipantId(participantId);
        then(unavailableTimeRepository).should().saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(request.unavailableTimes().size());

        // 히트맵 계산 및 SSE 브로드캐스트 검증
        then(heatmapService).should(times(1)).calculateHeatmapForStream(event);
        then(sseService).should(times(1)).broadcast(eq(hashUrl), eq(mockHeatmapResponse));
    }

    @Test
    @DisplayName("존재하지 않는 이벤트로 등록 시도 시 예외 발생")
    void fail_registerUnavailableTimesWithNonExistingEvent() {
        // given
        String hashUrl = "not-exist";
        Long participantId = 1L;
        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.empty());

        ParticipantUnavailableTimeRequest request = new ParticipantUnavailableTimeRequest(
                List.of(new ParticipantUnavailableTimeRequest.UnavailableTimeSlot(
                        LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0)
                ))
        );

        // when // then
        assertThatThrownBy(() -> unavailableTimeService.registerUnavailableTimes(hashUrl, participantId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(EventErrorCode.EVENT_NOT_FOUND.getMessage());

        then(participantRepository).shouldHaveNoInteractions();
        then(unavailableTimeRepository).shouldHaveNoInteractions();
        then(heatmapService).shouldHaveNoInteractions();
        then(sseService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("마감된 이벤트에 등록 시도 시 예외 발생")
    void fail_registerUnavailableTimesInClosedEvent() {
        // given
        String hashUrl = "closed";
        Long participantId = 1L;
        Event closedEvent = mockClosedEvent();
        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(closedEvent));

        ParticipantUnavailableTimeRequest request = new ParticipantUnavailableTimeRequest(
                List.of(new ParticipantUnavailableTimeRequest.UnavailableTimeSlot(
                        LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0)
                ))
        );

        // when // then
        assertThatThrownBy(() -> unavailableTimeService.registerUnavailableTimes(hashUrl, participantId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(EventErrorCode.EVENT_ALREADY_CLOSED.getMessage());

        then(participantRepository).shouldHaveNoInteractions();
        then(unavailableTimeRepository).shouldHaveNoInteractions();
        then(heatmapService).shouldHaveNoInteractions();
        then(sseService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("존재하지 않는 참여자로 등록 시도 시 예외 발생")
    void fail_registerUnavailableTimesWithNonExistingParticipant() {
        // given
        String hashUrl = "hash123";
        Long participantId = 99L;

        Event event = mock(Event.class);
        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(event));
        given(participantRepository.findById(participantId)).willReturn(Optional.empty());

        ParticipantUnavailableTimeRequest request = new ParticipantUnavailableTimeRequest(
                List.of(new ParticipantUnavailableTimeRequest.UnavailableTimeSlot(
                        LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0)
                ))
        );

        // when // then
        assertThatThrownBy(() -> unavailableTimeService.registerUnavailableTimes(hashUrl, participantId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(ParticipantErrorCode.PARTICIPANT_NOT_FOUND.getMessage());

        then(unavailableTimeRepository).shouldHaveNoInteractions();
        then(heatmapService).shouldHaveNoInteractions();
        then(sseService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("다른 이벤트의 참여자로 등록 시도 시 예외 발생")
    void fail_registerUnavailableTimesWithParticipantFromDifferentEvent() {
        // given
        String hashUrl = "hashA";
        Long participantId = 1L;
        Event eventA = mock(Event.class); // 최소 스텁: isClosed 기본값 false
        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(eventA));

        Participant participant = mock(Participant.class);
        given(participantRepository.findById(participantId)).willReturn(Optional.of(participant));
        given(participant.isNotBelongsToEvent(eventA)).willReturn(true);

        // 이벤트 정보에 의존하지 않는 단순 요청 (참여자-이벤트 검증에서 예외 발생 예정)
        ParticipantUnavailableTimeRequest request = new ParticipantUnavailableTimeRequest(
                List.of(new ParticipantUnavailableTimeRequest.UnavailableTimeSlot(
                        LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0)
                ))
        );

        // when // then (오류 코드 검증)
        assertThatThrownBy(() -> unavailableTimeService.registerUnavailableTimes(hashUrl, participantId, request))
                .isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode())
                        .isEqualTo(ParticipantErrorCode.PARTICIPANT_NOT_BELONGS_TO_EVENT)
                );

        then(unavailableTimeRepository).shouldHaveNoInteractions();
        then(heatmapService).shouldHaveNoInteractions();
        then(sseService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("기존 데이터 삭제 후 새 데이터로 덮어쓰기 성공")
    void success_overwriteExistingUnavailableTimes() {
        // given
        String hashUrl = "hash123";
        Long participantId = 1L;

        Event event = mockOpenEvent();
        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(event));

        Participant participant = mock(Participant.class);
        given(participant.isNotBelongsToEvent(event)).willReturn(false);
        given(participantRepository.findById(participantId)).willReturn(Optional.of(participant));

        ParticipantUnavailableTimeRequest request = createValidRequest(event);

        // HeatmapService와 SseService Mock 설정
        EventHeatmapStreamResponse mockHeatmapResponse = mock(EventHeatmapStreamResponse.class);
        given(heatmapService.calculateHeatmapForStream(event)).willReturn(mockHeatmapResponse);

        InOrder inOrder = inOrder(unavailableTimeRepository, heatmapService, sseService);

        // when
        ParticipantUnavailableTimeResponse response = unavailableTimeService.registerUnavailableTimes(
                hashUrl, participantId, request
        );

        // then
        assertThat(response.registeredCount()).isEqualTo(request.unavailableTimes().size());

        ArgumentCaptor<List<ParticipantUnavailableTime>> captor = ArgumentCaptor.forClass(List.class);

        // 순서 검증: 삭제 → 저장 → 히트맵 계산 → SSE 브로드캐스트
        inOrder.verify(unavailableTimeRepository).deleteAllByParticipantId(participantId);
        inOrder.verify(unavailableTimeRepository).saveAll(captor.capture());
        inOrder.verify(heatmapService).calculateHeatmapForStream(event);
        inOrder.verify(sseService).broadcast(eq(hashUrl), eq(mockHeatmapResponse));

        assertThat(captor.getValue()).hasSize(request.unavailableTimes().size());
    }

    private Event mockOpenEvent() {
        Event event = mock(Event.class);
        LocalDate today = LocalDate.now();
        given(event.isClosed()).willReturn(false);
        given(event.getDateStart()).willReturn(today);
        given(event.getDateEnd()).willReturn(today);
        given(event.getTimeStart()).willReturn(LocalTime.of(9, 0));
        given(event.getTimeEnd()).willReturn(LocalTime.of(21, 0));
        return event;
    }

    private Event mockClosedEvent() {
        Event event = mock(Event.class);
        given(event.isClosed()).willReturn(true);
        return event;
    }

    private ParticipantUnavailableTimeRequest createValidRequest(Event event) {
        LocalDate date = event.getDateStart();
        return new ParticipantUnavailableTimeRequest(
                List.of(
                        new ParticipantUnavailableTimeRequest.UnavailableTimeSlot(
                                date, LocalTime.of(10, 0), LocalTime.of(11, 0)
                        ),
                        new ParticipantUnavailableTimeRequest.UnavailableTimeSlot(
                                date, LocalTime.of(12, 0), LocalTime.of(13, 0)
                        )
                )
        );
    }

}