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
import eusyaeusya.gmg.domain.participant.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;

    @Transactional
    public ParticipantNameJoinResponse joinEvent(String hashUrl, ParticipantNameJoinRequest request) {
        Event event = getEvent(hashUrl);

        validateEventStatus(hashUrl, event);

        Participant participant = Participant.create(event, request.name());
        Participant savedParticipant = participantRepository.save(participant);

        log.info("참여자 등록 완료: eventId={}, participantId={}, name={}",
                event.getId(), savedParticipant.getId(), savedParticipant.getName());

        return ParticipantNameJoinResponse.from(savedParticipant);
    }

    @Transactional
    public ParticipantCompleteResponse completeParticipation(String hashUrl, Long participantId) {
        Event event = getEvent(hashUrl);
        validateEventStatus(hashUrl, event);

        Participant participant = getParticipant(participantId);
        validateParticipantBelongsToEvent(participant, event);

        participant.complete();

        log.info("참여자 정보 입력 완료: eventId={}, participantId={}, name={}, status={}",
                event.getId(), participant.getId(), participant.getName(), participant.getParticipantStatus());

        return ParticipantCompleteResponse.from(participant);
    }

    private Event getEvent(String hashUrl) {
        return eventRepository.findByHashUrl(hashUrl)
                .orElseThrow(() -> new NotFoundException(
                        EventErrorCode.EVENT_NOT_FOUND,
                        String.format(EventErrorCode.EVENT_NOT_FOUND.getMessage(), ": %s", hashUrl)
                ));
    }

    private void validateEventStatus(String hashUrl, Event event) {
        if (event.isClosed()) {
            throw new BadRequestException(
                    EventErrorCode.EVENT_ALREADY_CLOSED,
                    String.format(EventErrorCode.EVENT_ALREADY_CLOSED.getMessage(), ": %s", hashUrl)
            );
        }
    }

    private Participant getParticipant(Long participantId) {
        return participantRepository.findById(participantId)
                .orElseThrow(() -> new NotFoundException(
                        ParticipantErrorCode.PARTICIPANT_NOT_FOUND,
                        String.format("참여자를 찾을 수 없습니다: %s", participantId)
                ));
    }

    private void validateParticipantBelongsToEvent(Participant participant, Event event) {
        if (participant.isNotBelongsToEvent(event)) {
            throw new BadRequestException(
                    ParticipantErrorCode.PARTICIPANT_NOT_BELONGS_TO_EVENT,
                    String.format("해당 이벤트에 속한 참여자가 아닙니다: %s", participant.getName())
            );
        }
    }
}
