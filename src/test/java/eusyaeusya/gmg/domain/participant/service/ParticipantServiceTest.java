package eusyaeusya.gmg.domain.participant.service;

import eusyaeusya.gmg.api.event.response.EventErrorCode;
import eusyaeusya.gmg.api.participant.request.ParticipantNameJoinRequest;
import eusyaeusya.gmg.api.participant.response.ParticipantCompleteResponse;
import eusyaeusya.gmg.api.participant.response.ParticipantErrorCode;
import eusyaeusya.gmg.api.participant.response.ParticipantNameJoinResponse;
import eusyaeusya.gmg.common.api.exception.BadRequestException;
import eusyaeusya.gmg.common.api.exception.NotFoundException;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.participant.entity.Participant;
import eusyaeusya.gmg.domain.participant.entity.ParticipantStatus;
import eusyaeusya.gmg.domain.participant.repository.ParticipantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @InjectMocks
    private ParticipantService participantService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Test
    @DisplayName("참여자 이름 등록 성공")
    void success_joinEventTest() {
        // given
        String hashUrl = "abc123";
        ParticipantNameJoinRequest request = new ParticipantNameJoinRequest("홍길동");

        Event mockEvent = mock(Event.class);
        given(mockEvent.isClosed()).willReturn(false);
        given(mockEvent.getId()).willReturn(1L);
        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(mockEvent));

        Participant mockParticipant = mock(Participant.class);
        given(mockParticipant.getId()).willReturn(1L);
        given(mockParticipant.getName()).willReturn(request.name());
        given(mockParticipant.getJoinedAt()).willReturn(java.time.LocalDateTime.now());
        given(participantRepository.save(any(Participant.class))).willReturn(mockParticipant);

        // when
        ParticipantNameJoinResponse response = participantService.joinEvent(hashUrl, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.participantId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("홍길동");

        verify(eventRepository).findByHashUrl(hashUrl);
        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    @DisplayName("존재하지 않는 이벤트로 참여 시도 시 예외 발생")
    void fail_joinEventWithNonExistingEventTest() {
        // given
        String hashUrl = "invalid";
        ParticipantNameJoinRequest request = new ParticipantNameJoinRequest("홍길동");
        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> participantService.joinEvent(hashUrl, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(EventErrorCode.EVENT_NOT_FOUND.getMessage());

        verify(participantRepository, never()).save(any());
    }

    @Test
    @DisplayName("마감된 이벤트에 참여 시도 시 예외 발생")
    void fail_joinClosedEvent() {
        // given
        String hashUrl = "abc123";
        ParticipantNameJoinRequest request = new ParticipantNameJoinRequest("홍길동");

        Event mockEvent = mock(Event.class);
        given(mockEvent.isClosed()).willReturn(true);
        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(mockEvent));

        // when & then
        assertThatThrownBy(() -> participantService.joinEvent(hashUrl, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(EventErrorCode.EVENT_ALREADY_CLOSED.getMessage());

        verify(participantRepository, never()).save(any());
    }

    @Test
    @DisplayName("참여자 정보 입력 완료 성공")
    void success_completeParticipation() {
        // given
        String hashUrl = "abc123";
        Long participantId = 1L;

        Event mockEvent = mock(Event.class);
        given(mockEvent.isClosed()).willReturn(false);
        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(mockEvent));

        Participant mockParticipant = mock(Participant.class);
        given(mockParticipant.getId()).willReturn(participantId);
        given(mockParticipant.getName()).willReturn("홍길동");
        given(mockParticipant.getParticipantStatus()).willReturn(ParticipantStatus.COMPLETED);
        given(mockParticipant.getCompletedAt()).willReturn(LocalDateTime.now());
        given(mockParticipant.isNotBelongsToEvent(mockEvent)).willReturn(false);
        given(participantRepository.findById(participantId)).willReturn(Optional.of(mockParticipant));

        // when
        ParticipantCompleteResponse response = participantService.completeParticipation(hashUrl, participantId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.participantId()).isEqualTo(participantId);
        assertThat(response.status()).isEqualTo(ParticipantStatus.COMPLETED);
        assertThat(response.completedAt()).isNotNull();

        verify(mockParticipant).complete();
    }

    @Test
    @DisplayName("이미 완료된 참여자 - 멱등성 보장")
    void success_completeParticipation_alreadyCompleted() {
        // given
        String hashUrl = "abc123";
        Long participantId = 1L;

        Event mockEvent = mock(Event.class);
        given(mockEvent.isClosed()).willReturn(false);
        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(mockEvent));

        Participant mockParticipant = mock(Participant.class);
        given(mockParticipant.getId()).willReturn(participantId);
        given(mockParticipant.getName()).willReturn("홍길동");
        given(mockParticipant.getParticipantStatus()).willReturn(ParticipantStatus.COMPLETED);
        given(mockParticipant.getCompletedAt()).willReturn(LocalDateTime.now());
        given(mockParticipant.isNotBelongsToEvent(mockEvent)).willReturn(false);
        given(participantRepository.findById(participantId)).willReturn(Optional.of(mockParticipant));

        // when
        ParticipantCompleteResponse response = participantService.completeParticipation(hashUrl, participantId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ParticipantStatus.COMPLETED);

        verify(mockParticipant).complete();
    }

    @Test
    @DisplayName("마감된 이벤트에 완료 시도 시 예외 발생")
    void fail_completeParticipation_eventClosed() {
        // given
        String hashUrl = "abc123";
        Long participantId = 1L;

        Event mockEvent = mock(Event.class);
        given(mockEvent.isClosed()).willReturn(true);
        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(mockEvent));

        // when & then
        assertThatThrownBy(() -> participantService.completeParticipation(hashUrl, participantId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(EventErrorCode.EVENT_ALREADY_CLOSED.getMessage());

        verify(participantRepository, never()).findById(any());
    }

    @Test
    @DisplayName("존재하지 않는 참여자로 완료 시도 시 예외 발생")
    void fail_completeParticipation_participantNotFound() {
        // given
        String hashUrl = "abc123";
        Long participantId = 99L;

        Event mockEvent = mock(Event.class);
        given(mockEvent.isClosed()).willReturn(false);
        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(mockEvent));
        given(participantRepository.findById(participantId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> participantService.completeParticipation(hashUrl, participantId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(ParticipantErrorCode.PARTICIPANT_NOT_FOUND.getMessage());
    }

}