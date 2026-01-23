package eusyaeusya.gmg.domain.place.service.search;

import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.entity.EventPlaceType;
import eusyaeusya.gmg.domain.event.repository.EventPlaceTypeRepository;
import eusyaeusya.gmg.domain.place.entity.PlaceCategory;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.repository.PlaceCategoryRepository;
import eusyaeusya.gmg.infra.kakao.client.KakaoMapClient;
import eusyaeusya.gmg.infra.kakao.dto.KakaoPlaceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PlaceFetchService {

    private static final int DEFAULT_RADIUS_METERS = 250;

    private final EventPlaceTypeRepository eventPlaceTypeRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final KakaoMapClient kakaoMapClient;

    public PlaceFetchService(
            EventPlaceTypeRepository eventPlaceTypeRepository,
            PlaceCategoryRepository placeCategoryRepository,
            @Autowired(required = false) KakaoMapClient kakaoMapClient) {
        this.eventPlaceTypeRepository = eventPlaceTypeRepository;
        this.placeCategoryRepository = placeCategoryRepository;
        this.kakaoMapClient = kakaoMapClient;
    }

    /**
     * 이벤트에 해당하는 장소를 카카오맵에서 조회
     *
     * @param event 이벤트
     * @return 중복 제거된 장소 DTO 목록 (저장 전 상태)
     */
    public List<KakaoPlaceDto> fetchPlacesForEvent(Event event) {
        if (kakaoMapClient == null) {
            log.warn("KakaoMapClient가 활성화되지 않았습니다 (local 프로필). 장소 검색을 건너뜁니다.");
            return List.of();
        }

        // 1. 이벤트의 PlaceType 조회
        List<PlaceType> placeTypes = getPlaceTypesForEvent(event);

        // 2. 키워드 추출
        List<String> keywords = extractKeywords(placeTypes);

        log.info("이벤트 {} 장소 검색 시작: PlaceTypes={}, 키워드={}",
                event.getId(),
                placeTypes.stream().map(PlaceType::getCode).toList(),
                keywords);

        // 3. 카카오맵 API 호출
        List<KakaoPlaceDto> allDtos = fetchFromKakaoMap(
                keywords,
                event.getCenterLongitude().toString(),
                event.getCenterLatitude().toString()
        );

        // 4. 중복 제거
        List<KakaoPlaceDto> uniqueDtos = deduplicatePlaces(allDtos);

        log.info("카카오맵 조회 완료: 총 {}개 (중복 제거 후 {}개)",
                allDtos.size(), uniqueDtos.size());

        return uniqueDtos;
    }

    /**
     * 이벤트에 연결된 PlaceType 목록 조회
     */
    private List<PlaceType> getPlaceTypesForEvent(Event event) {
        List<EventPlaceType> eventPlaceTypes = eventPlaceTypeRepository
                .findByEventWithPlaceType(event);

        return eventPlaceTypes.stream()
                .map(EventPlaceType::getPlaceType)
                .toList();
    }

    /**
     * PlaceType 목록에서 검색 키워드 추출
     *
     * @param placeTypes PlaceType 목록
     * @return 카카오맵 검색용 키워드 목록
     */
    private List<String> extractKeywords(List<PlaceType> placeTypes) {
        List<PlaceCategory> categories = placeCategoryRepository
                .findByPlaceTypeIn(placeTypes);

        return categories.stream()
                .map(this::mapCategoryToKeyword)
                .toList();
    }

    /**
     * 카테고리를 검색 키워드로 변환
     * 일부 카테고리는 카카오맵에서 직접 검색이 불가하여 변환 필요
     */
    private String mapCategoryToKeyword(PlaceCategory category) {
        // "개인카페"는 카카오맵에서 직접 검색 불가하므로 "카페"로 변환
        if ("개인카페".equals(category.getName())) {
            return "카페";
        }
        return category.getName();
    }

    /**
     * 카카오맵 API 호출
     */
    private List<KakaoPlaceDto> fetchFromKakaoMap(
            List<String> keywords,
            String longitude,
            String latitude) {
        return kakaoMapClient.searchByKeywords(
                keywords,
                longitude,
                latitude,
                DEFAULT_RADIUS_METERS
        );
    }

    /**
     * 중복 장소 제거 (같은 externalId 기준)
     */
    private List<KakaoPlaceDto> deduplicatePlaces(List<KakaoPlaceDto> places) {
        Map<String, KakaoPlaceDto> uniqueMap = places.stream()
                .collect(Collectors.toMap(
                        KakaoPlaceDto::id,
                        dto -> dto,
                        (existing, replacement) -> existing
                ));

        return List.copyOf(uniqueMap.values());
    }
}
