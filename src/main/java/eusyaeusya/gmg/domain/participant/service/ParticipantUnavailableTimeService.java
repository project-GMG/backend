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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipantUnavailableTimeService {
    private final ParticipantUnavailableTimeRepository unavailableTimeRepository;
    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final HeatmapService heatmapService;
    private final SseService sseService;

    @Transactional
    public ParticipantUnavailableTimeResponse registerUnavailableTimes(
            String hashUrl,
            Long participantId,
            ParticipantUnavailableTimeRequest request
    ) {
        Event event = getEventWithLock(hashUrl);
        validateEventStatus(hashUrl, event);

        Participant participant = getParticipant(participantId);
        validateParticipantBelongsToEvent(participant, event);

        //기존 불가능 시간 전체 삭제
        deleteExistUnavailableTime(participantId);

        List<ParticipantUnavailableTime> unavailableTimes =
                createUnavailableTimes(participantId, request, event, participant);

        EventHeatmapStreamResponse heatmapData = heatmapService.calculateHeatmapForStream(event);
        sseService.broadcast(hashUrl, "heatmap-update", heatmapData);

        return ParticipantUnavailableTimeResponse.of(participantId, unavailableTimes.size());
    }

    private Event getEvent(String hashUrl) {
        return eventRepository.findByHashUrl(hashUrl)
                .orElseThrow(() -> new NotFoundException(
                        EventErrorCode.EVENT_NOT_FOUND,
                        String.format(EventErrorCode.EVENT_NOT_FOUND.getMessage(), ": %s", hashUrl)
                ));
    }

    private Event getEventWithLock(String hashUrl) {
        return eventRepository.findByHashUrlWithLock(hashUrl)
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

    private void deleteExistUnavailableTime(Long participantId) {
        unavailableTimeRepository.deleteAllByParticipantId(participantId);
        log.info("기존 불가능 시간 삭제 완료: participantId={}", participantId);
    }

    private @NonNull List<ParticipantUnavailableTime> createUnavailableTimes(Long participantId, ParticipantUnavailableTimeRequest request, Event event, Participant participant) {
        List<ParticipantUnavailableTime> unavailableTimes = request.unavailableTimes().stream()
                .map(slot -> ParticipantUnavailableTime.create(
                        event,
                        participant,
                        slot.date(),
                        slot.startTime(),
                        slot.endTime()
                ))
                .toList();

        unavailableTimeRepository.saveAll(unavailableTimes);
        log.info("불가능 시간 등록 완료: participantId={}, count={}",
                participantId, unavailableTimes.size());
        return unavailableTimes;
    }
}
