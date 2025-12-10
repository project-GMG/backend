package eusyaeusya.gmg.domain.place.service;

import eusyaeusya.gmg.api.event.response.EventErrorCode;
import eusyaeusya.gmg.api.place.response.PlaceTypeWithCategoriesResponse;
import eusyaeusya.gmg.common.api.exception.NotFoundException;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.entity.EventPlaceType;
import eusyaeusya.gmg.domain.event.repository.EventPlaceTypeRepository;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.place.entity.PlaceCategory;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.repository.PlaceCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceCategoryService {

    private final EventRepository eventRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final EventPlaceTypeRepository eventPlaceTypeRepository;

    public PlaceTypeWithCategoriesResponse getPlaceTypeWithCategoriesResponse(String hashUrl) {
        Event event = getEvent(hashUrl);
        List<PlaceType> placeTypes = getPlaceTypes(event);

        Map<Long, List<PlaceCategory>> categoriesByPlaceTypeId = getCategoriesByPlaceTypeId(placeTypes);

        List<PlaceTypeWithCategoriesResponse.PlaceTypeWithCategories> placeTypeWithCategoriesList =
                groupPlaceTypesWithCategories(placeTypes, categoriesByPlaceTypeId);

        return new PlaceTypeWithCategoriesResponse(placeTypeWithCategoriesList);
    }

    private Event getEvent(String hashUrl) {
        return eventRepository.findByHashUrl(hashUrl)
                .orElseThrow(() -> new NotFoundException(
                        EventErrorCode.EVENT_NOT_FOUND,
                        String.format(EventErrorCode.EVENT_NOT_FOUND.getMessage(), ": %s", hashUrl)
                ));
    }

    private List<PlaceType> getPlaceTypes(Event event) {
        List<PlaceType> placeTypes = eventPlaceTypeRepository.findByEventWithPlaceType(event).stream()
                .map(EventPlaceType::getPlaceType)
                .toList();

        log.info("이벤트의 PlaceType 조회: eventId={}, placeTypeCount={}", event.getId(), placeTypes.size());
        return placeTypes;
    }

    private Map<Long, List<PlaceCategory>> getCategoriesByPlaceTypeId(List<PlaceType> placeTypes) {
        return placeCategoryRepository.findByPlaceTypeIn(placeTypes).stream()
                .collect(Collectors.groupingBy(category -> category.getPlaceType().getId()));
    }

    private @NonNull List<PlaceTypeWithCategoriesResponse.PlaceTypeWithCategories> groupPlaceTypesWithCategories(List<PlaceType> placeTypes, Map<Long, List<PlaceCategory>> categoriesByPlaceTypeId) {
        return placeTypes.stream()
                .map(placeType -> PlaceTypeWithCategoriesResponse.PlaceTypeWithCategories.of(
                        placeType,
                        categoriesByPlaceTypeId.getOrDefault(placeType.getId(), List.of())
                ))
                .toList();
    }
}
