package eusyaeusya.gmg.domain.place.service;

import eusyaeusya.gmg.api.event.response.EventErrorCode;
import eusyaeusya.gmg.api.place.response.PlaceErrorCode;
import eusyaeusya.gmg.api.place.response.PlaceListResponse;
import eusyaeusya.gmg.common.api.exception.BadRequestException;
import eusyaeusya.gmg.common.api.exception.NotFoundException;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.entity.EventPlaceType;
import eusyaeusya.gmg.domain.event.repository.EventPlaceTypeRepository;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.place.entity.PlaceCategory;
import eusyaeusya.gmg.domain.place.repository.PlaceCategoryRepository;
import eusyaeusya.gmg.domain.place.repository.PlaceRepository;
import eusyaeusya.gmg.domain.place.repository.PlaceSimpleProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private static final int DEFAULT_RADIUS_METERS = 500;

    private final PlaceRepository placeRepository;
    private final EventRepository eventRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final EventPlaceTypeRepository eventPlaceTypeRepository;

    public PlaceListResponse getPlacesWithinRadius(String hashUrl, Long categoryId, int page, int size) {
        Event event = getEvent(hashUrl);
        PlaceCategory category = getCategory(categoryId);

        validateCategoryBelongsToEventPlaceTypes(event, category);

        double centerLat = event.getCenterLatitude().doubleValue();
        double centerLng = event.getCenterLongitude().doubleValue();

        // MBR(최소 경계 사각형)
        GeometryUtil.BoundingBox boundingBox = GeometryUtil.calculateBoundingBox(
                centerLat, centerLng, DEFAULT_RADIUS_METERS
        );

        Slice<PlaceSimpleProjection> placesSlice = placeRepository.findPlacesWithinRadius(
                categoryId,
                centerLat,
                centerLng,
                DEFAULT_RADIUS_METERS,
                boundingBox.minLat(),
                boundingBox.maxLat(),
                boundingBox.minLng(),
                boundingBox.maxLng(),
                PageRequest.of(page, size)
        );

        List<PlaceSimpleProjection> places = placesSlice.getContent();

        log.info("반경 내 매장 조회 완료: eventId={}, categoryId={}, radius={}m, count={}",
                event.getId(), categoryId, DEFAULT_RADIUS_METERS, places.size());

        return PlaceListResponse.from(places, placesSlice.hasNext());
    }

    private Event getEvent(String hashUrl) {
        return eventRepository.findByHashUrl(hashUrl)
                .orElseThrow(() -> new NotFoundException(
                        EventErrorCode.EVENT_NOT_FOUND,
                        String.format("이벤트를 찾을 수 없습니다: %s", hashUrl)
                ));
    }

    private PlaceCategory getCategory(Long categoryId) {
        return placeCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(
                        PlaceErrorCode.PLACE_CATEGORY_NOT_FOUND,
                        String.format("장소 카테고리를 찾을 수 없습니다: %s", categoryId)
                ));
    }

    private void validateCategoryBelongsToEventPlaceTypes(Event event, PlaceCategory category) {
        List<EventPlaceType> eventPlaceTypes = eventPlaceTypeRepository.findByEventWithPlaceType(event);

        boolean belongs = eventPlaceTypes.stream()
                .map(EventPlaceType::getPlaceType)
                .anyMatch(placeType -> placeType.equals(category.getPlaceType()));

        if (!belongs) {
            throw new BadRequestException(
                    PlaceErrorCode.CATEGORY_NOT_IN_EVENT_PLACE_TYPES,
                    String.format("해당 카테고리는 이벤트에서 선택한 장소 타입에 속하지 않습니다: categoryId=%d", category.getId())
            );
        }
    }
}
