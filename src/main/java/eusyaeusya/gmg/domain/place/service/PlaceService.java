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
import eusyaeusya.gmg.domain.place.entity.Place;
import eusyaeusya.gmg.domain.place.entity.PlaceCategory;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.repository.PlaceCategoryRepository;
import eusyaeusya.gmg.domain.place.repository.PlaceRepository;
import eusyaeusya.gmg.domain.place.repository.PlaceSimpleProjection;
import eusyaeusya.gmg.domain.place.repository.PlaceTypeRepository;
import eusyaeusya.gmg.domain.place.util.GeometryUtil;
import eusyaeusya.gmg.infra.kakao.dto.KakaoPlaceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private static final int DEFAULT_RADIUS_METERS = 250;

    private final PlaceRepository placeRepository;
    private final PlaceTypeRepository placeTypeRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final EventRepository eventRepository;
    private final EventPlaceTypeRepository eventPlaceTypeRepository;

    /**
     * 카카오맵에서 조회한 장소 DTO를 저장
     * 동시성 이슈 처리 포함 (중복 저장 시도 시 재조회)
     */
    @Transactional
    public List<Place> savePlaces(List<KakaoPlaceDto> placeDtos) {
        if (placeDtos.isEmpty()) {
            return List.of();
        }

        // 1. 기존 장소 확인
        List<String> externalIds = placeDtos.stream()
                .map(KakaoPlaceDto::id)
                .toList();

        List<Place> existingPlaces = placeRepository.findAllByPlaceExternalIdIn(externalIds);
        Set<String> existingIds = existingPlaces.stream()
                .map(Place::getPlaceExternalId)
                .collect(Collectors.toSet());

        // 2. 신규 장소만 필터링 (동시에 저장 제외 대상 필터링)
        List<KakaoPlaceDto> newDtos = placeDtos.stream()
                .filter(dto -> !dto.isExcluded()) // 뷔페, 패밀리레스토랑 등 제외
                .filter(dto -> !existingIds.contains(dto.id()))
                .toList();

        log.info("장소 저장: 기존={}개, 신규={}개 (제외 필터링 후)", existingPlaces.size(), newDtos.size());

        // 3. 신규 장소 저장
        List<Place> savedNewPlaces = saveNewPlaces(newDtos);

        // 4. 전체 반환
        List<Place> allPlaces = new ArrayList<>(existingPlaces);
        allPlaces.addAll(savedNewPlaces);

        return allPlaces;
    }

    /**
     * 신규 장소 저장 (동시성 처리 포함)
     */
    private List<Place> saveNewPlaces(List<KakaoPlaceDto> newDtos) {
        List<Place> savedPlaces = new ArrayList<>();

        for (KakaoPlaceDto dto : newDtos) {
            Place saved = savePlace(dto);
            savedPlaces.add(saved);
        }

        return savedPlaces;
    }

    /**
     * 단일 장소 저장
     * 동시성 이슈로 중복 저장 시도 시 기존 장소 재조회
     */
    private Place savePlace(KakaoPlaceDto dto) {
        try {
            PlaceType placeType = getPlaceType(dto);
            PlaceCategory category = getPlaceCategory(dto);

            Place place = Place.fromKakao(dto, placeType, category);
            Place saved = placeRepository.save(place);

            log.debug("신규 장소 저장: id={}, name={}, type={}, category={}",
                    dto.id(), dto.placeName(),
                    placeType.getCode(),
                    category != null ? category.getCode() : "null");

            return saved;

        } catch (DataIntegrityViolationException e) {
            log.warn("중복 저장 감지: externalId={}, 재조회 중...", dto.id());

            return placeRepository.findByPlaceExternalId(dto.id())
                    .orElseThrow(() -> new IllegalStateException(
                            "Place가 존재해야 하는데 없음: " + dto.id()
                    ));
        }
    }

    /**
     * DTO에서 PlaceType 조회
     */
    private PlaceType getPlaceType(KakaoPlaceDto dto) {
        String placeTypeCode = dto.inferPlaceTypeCode();
        return placeTypeRepository.findByCode(placeTypeCode)
                .orElseThrow(() -> new NotFoundException(
                        PlaceErrorCode.PLACE_TYPE_NOT_FOUND,
                        String.format("PlaceType을 찾을 수 없습니다: %s", placeTypeCode)
                ));
    }

    /**
     * DTO에서 PlaceCategory 조회 (없으면 null)
     */
    private PlaceCategory getPlaceCategory(KakaoPlaceDto dto) {
        String categoryCode = dto.mapToCategoryCode();
        if (categoryCode == null) {
            return null;
        }
        return placeCategoryRepository.findByCode(categoryCode).orElse(null);
    }

    public PlaceListResponse getPlacesWithinRadius(String hashUrl, Long categoryId, int page, int size) {
        log.info("반경 내 장소 조회 시작: hashUrl={}, categoryId={}", hashUrl, categoryId);

        Event event = getEvent(hashUrl);
        PlaceCategory category = getCategory(categoryId);

        validateCategoryBelongsToEventPlaceTypes(event, category);

        double centerLat = event.getCenterLatitude().doubleValue();
        double centerLng = event.getCenterLongitude().doubleValue();

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

        log.info("반경 내 장소 조회 완료: eventId={}, categoryId={}, radius={}m, count={}",
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