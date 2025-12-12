package eusyaeusya.gmg.domain.participant.service;

import eusyaeusya.gmg.api.event.response.EventErrorCode;
import eusyaeusya.gmg.api.participant.request.ParticipantNameJoinRequest;
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
}
