package eusyaeusya.gmg.domain.event.service;

import eusyaeusya.gmg.api.event.response.EventErrorCode;
import eusyaeusya.gmg.api.event.response.EventPlaceTypeCategoriesResponse;
import eusyaeusya.gmg.common.api.exception.NotFoundException;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.entity.EventPlaceType;
import eusyaeusya.gmg.domain.event.repository.EventPlaceTypeRepository;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.place.entity.PlaceCategory;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.service.PlaceCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventPlaceTypeService {

    private final EventRepository eventRepository;
    private final EventPlaceTypeRepository eventPlaceTypeRepository;
    private final PlaceCategoryService placeCategoryService;

    public EventPlaceTypeCategoriesResponse getAvailableCategoriesForEvent(String hashUrl) {
        Event event = getEvent(hashUrl);

        List<PlaceType> selectedPlaceTypes = getSelectedPlaceTypes(event);
        log.info("이벤트의 선택된 PlaceType 조회: eventId={}, placeTypeCount={}",
                event.getId(), selectedPlaceTypes.size());

        Map<PlaceType, List<PlaceCategory>> categoriesGroupedByPlaceType =
                placeCategoryService.getCategoriesGroupedByPlaceType(selectedPlaceTypes);

        return buildResponse(selectedPlaceTypes, categoriesGroupedByPlaceType);
    }

    private Event getEvent(String hashUrl) {
        return eventRepository.findByHashUrl(hashUrl)
                .orElseThrow(() -> new NotFoundException(
                        EventErrorCode.EVENT_NOT_FOUND,
                        String.format("이벤트를 찾을 수 없습니다: %s", hashUrl)
                ));
    }

    private List<PlaceType> getSelectedPlaceTypes(Event event) {
        List<EventPlaceType> eventPlaceTypes = eventPlaceTypeRepository.findByEventWithPlaceType(event);

        return eventPlaceTypes.stream()
                .map(EventPlaceType::getPlaceType)
                .toList();
    }

    private EventPlaceTypeCategoriesResponse buildResponse(
            List<PlaceType> placeTypes,
            Map<PlaceType, List<PlaceCategory>> categoriesMap
    ) {
        List<EventPlaceTypeCategoriesResponse.PlaceTypeWithCategories> placeTypeWithCategoriesList =
                placeTypes.stream()
                        .map(placeType -> {
                            List<PlaceCategory> categories = categoriesMap.getOrDefault(placeType, List.of());
                            return EventPlaceTypeCategoriesResponse.PlaceTypeWithCategories.of(
                                    placeType,
                                    categories
                            );
                        })
                        .toList();

        return new EventPlaceTypeCategoriesResponse(placeTypeWithCategoriesList);
    }
}
