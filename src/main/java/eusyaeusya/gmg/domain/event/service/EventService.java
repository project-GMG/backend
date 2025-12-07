package eusyaeusya.gmg.domain.event.service;

import eusyaeusya.gmg.api.event.request.EventCreateRequest;
import eusyaeusya.gmg.api.event.response.EventCreateResponse;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.entity.EventPlaceType;
import eusyaeusya.gmg.domain.event.repository.EventPlaceTypeRepository;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.service.PlaceTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EventPlaceTypeRepository eventPlaceTypeRepository;
    private final PlaceTypeService placeTypeService;

    @Transactional
    public EventCreateResponse createEvent(final EventCreateRequest request) {
        List<PlaceType> placeTypes = placeTypeService.findByCodes(request.placeTypeCodes());

        Event event = Event.create(
                request.title(),
                request.location().centerLatitude(),
                request.location().centerLongitude(),
                request.location().locationName(),
                request.dateRange().startDate(),
                request.dateRange().endDate(),
                request.timeRange().startTime(),
                request.timeRange().endTime()
        );

        Event savedEvent = saveEvent(event);

        saveEventPlaceTypes(placeTypes, savedEvent);

        return EventCreateResponse.from(savedEvent);
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
}
