package eusyaeusya.gmg.domain.event.service;

import eusyaeusya.gmg.api.event.request.EventCreateRequest;
import eusyaeusya.gmg.api.event.response.EventCreateResponse;
import eusyaeusya.gmg.api.event.response.EventErrorCode;
import eusyaeusya.gmg.api.event.response.EventMainResponse;
import eusyaeusya.gmg.common.api.exception.BadRequestException;
import eusyaeusya.gmg.common.api.exception.NotFoundException;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.entity.EventPlaceType;
import eusyaeusya.gmg.domain.event.repository.EventPlaceTypeRepository;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.participant.entity.ParticipantStatus;
import eusyaeusya.gmg.domain.participant.repository.ParticipantRepository;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.service.PlaceTypeService;
import eusyaeusya.gmg.domain.place.service.listener.PlaceSearchEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EventPlaceTypeRepository eventPlaceTypeRepository;
    private final PlaceTypeService placeTypeService;
    private final ParticipantRepository participantRepository;
    private final HeatmapService heatmapService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public EventCreateResponse createEvent(final EventCreateRequest request) {
        log.info("모임 생성 시작: title={}", request.title());
        List<PlaceType> placeTypes = placeTypeService.findByCodes(request.placeTypeCodes());

        Event event = Event.create(
                request.title(),
                request.location().centerLatitude(),
                request.location().centerLongitude(),
                request.location().locationName(),
                request.selectedDates(),
                request.timeRange().startTime(),
                request.timeRange().endTime()
        );

        Event savedEvent = saveEvent(event);

        saveEventPlaceTypes(placeTypes, savedEvent);

        eventPublisher.publishEvent(new PlaceSearchEvent(savedEvent.getId()));

        log.info("모임 생성 완료: title={}", savedEvent.getTitle());
        return EventCreateResponse.from(savedEvent);
    }

    public EventMainResponse getEventMain(String hashUrl) {
        log.info("모임 정보 조회 시작: hashUrl={}", hashUrl);

        Event event = getEvent(hashUrl);

        List<EventMainResponse.PlaceTypeInfo> placeTypes = getPlaceTypeInfos(event);

        int participantCount = participantRepository.countByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED);

        List<EventMainResponse.HeatmapSlot> heatmapData = heatmapService.calculateHeatmap(event);

        EventMainResponse response = buildEventMainResponse(event, placeTypes, participantCount, heatmapData);

        log.info("모임 정보 조회 완료: hashUrl={}, participantCount={}, heatmapSlots={}",
                hashUrl, participantCount, heatmapData.size());

        return response;
    }

    private Event saveEvent(final Event event) {
        Event savedEvent = eventRepository.save(event);
        log.info("이벤트 생성 : id={}, hashUrl={}", savedEvent.getId(), savedEvent.getHashUrl());
        return savedEvent;
    }

    private void saveEventPlaceTypes(final List<PlaceType> placeTypes, final Event savedEvent) {
        List<EventPlaceType> eventPlaceTypes = placeTypes.stream()
                .map(placeType -> EventPlaceType.create(savedEvent, placeType))
                .toList();
        eventPlaceTypeRepository.saveAll(eventPlaceTypes);
    }

    private Event getEvent(String hashUrl) {
        Event event = eventRepository.findByHashUrl(hashUrl)
                .orElseThrow(() -> new NotFoundException(
                        EventErrorCode.EVENT_NOT_FOUND,
                        String.format("이벤트를 찾을 수 없습니다: %s", hashUrl)
                ));

        if (event.isExpired()) {
            throw new BadRequestException(
                    EventErrorCode.EVENT_EXPIRED,
                    String.format("만료된 이벤트입니다: %s", hashUrl)
            );
        }

        return event;
    }

    private @NonNull List<EventMainResponse.PlaceTypeInfo> getPlaceTypeInfos(Event event) {
        List<EventPlaceType> eventPlaceTypes = eventPlaceTypeRepository.findByEventWithPlaceType(event);

        return eventPlaceTypes.stream()
                .map(ept -> EventMainResponse.PlaceTypeInfo.builder()
                        .id(ept.getPlaceType().getId())
                        .code(ept.getPlaceType().getCode())
                        .label(ept.getPlaceType().getLabel())
                        .build())
                .collect(Collectors.toList());
    }

    private EventMainResponse buildEventMainResponse(Event event, List<EventMainResponse.PlaceTypeInfo> placeTypes, int participantCount, List<EventMainResponse.HeatmapSlot> heatmapData) {
        return EventMainResponse.builder()
                .eventId(event.getId())
                .hashUrl(event.getHashUrl())
                .title(event.getTitle())
                .status(event.getStatus().name())
                .placeTypes(placeTypes)
                .location(EventMainResponse.LocationInfo.builder()
                        .centerLatitude(event.getCenterLatitude())
                        .centerLongitude(event.getCenterLongitude())
                        .locationName(event.getLocationName())
                        .build())
                .selectedDates(event.getSelectedDates())
                .timeRange(EventMainResponse.TimeRangeInfo.builder()
                        .startTime(event.getTimeStart())
                        .endTime(event.getTimeEnd())
                        .build())
                .participantCount(participantCount)
                .createdAt(event.getCreatedAt())
                .heatmapData(heatmapData)
                .build();
    }
}
