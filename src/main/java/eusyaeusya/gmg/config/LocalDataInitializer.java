package eusyaeusya.gmg.config;

import eusyaeusya.gmg.domain.place.entity.PlaceCategory;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.repository.PlaceCategoryRepository;
import eusyaeusya.gmg.domain.place.repository.PlaceTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDataInitializer implements ApplicationRunner {
    private final PlaceTypeRepository placeTypeRepository;
    private final PlaceCategoryRepository placeCategoryRepository;

    @Override
    public void run(ApplicationArguments args) {
        initializePlaceTypes();
        initializePlaceCategories();
    }

    private void initializePlaceTypes() {
        if (placeTypeRepository.count() > 0) {
            log.info("PlaceTypes 이미 존재함");
            return;
        }

        log.info("PlaceTypes 생성");

        placeTypeRepository.save(PlaceType.builder()
                .code("RESTAURANT")
                .label("식당")
                .build());

        placeTypeRepository.save(PlaceType.builder()
                .code("CAFE")
                .label("카페")
                .build());

        placeTypeRepository.save(PlaceType.builder()
                .code("BAR")
                .label("술집")
                .build());

        placeTypeRepository.save(PlaceType.builder()
                .code("STUDY")
                .label("스터디")
                .build());

        log.info("PlaceTypes 생성 완료: {} records", placeTypeRepository.count());
    }

    private void initializePlaceCategories() {
        if (placeCategoryRepository.count() > 0) {
            log.info("PlaceCategories 이미 존재함");
            return;
        }

        log.info("PlaceCategories 생성");

        // PlaceType 조회
        PlaceType restaurant = placeTypeRepository.findByCodeIn(List.of("RESTAURANT")).getFirst();
        PlaceType cafe = placeTypeRepository.findByCodeIn(List.of("CAFE")).getFirst();
        PlaceType bar = placeTypeRepository.findByCodeIn(List.of("BAR")).getFirst();
        PlaceType study = placeTypeRepository.findByCodeIn(List.of("STUDY")).getFirst();

        // 식당 카테고리 (9개)
        List<Map.Entry<String, String>> restaurantCategories = List.of(
                Map.entry("한식", "KOREAN_FOOD"),
                Map.entry("일식", "JAPANESE_FOOD"),
                Map.entry("중식", "CHINESE_FOOD"),
                Map.entry("양식", "WESTERN_FOOD"),
                Map.entry("아시안", "ASIAN_FOOD"),
                Map.entry("패스트푸드", "FAST_FOOD"),
                Map.entry("고기", "MEAT"),
                Map.entry("치킨", "CHICKEN"),
                Map.entry("분식", "SNACK_BAR")
        );

        restaurantCategories.forEach(entry ->
                placeCategoryRepository.save(PlaceCategory.create(restaurant, entry.getKey(), entry.getValue()))
        );

        // 카페 카테고리 (4개)
        Map<String, String> cafeCategories = Map.of(
                "개인카페", "LOCAL_CAFE",
                "디저트", "DESSERT_CAFE",
                "가맹점", "FRANCHISE_CAFE",
                "보드게임", "BOARDGAME_CAFE"
        );

        cafeCategories.forEach((name, code) ->
                placeCategoryRepository.save(PlaceCategory.create(cafe, name, code))
        );

        // 술집 카테고리 (4개)
        Map<String, String> barCategories = Map.of(
                "요리주점", "FOOD_BAR",
                "이자카야", "IZAKAYA",
                "실내포차", "INDOOR_POCHA",
                "칵테일바", "COCKTAIL_BAR"
        );

        barCategories.forEach((name, code) ->
                placeCategoryRepository.save(PlaceCategory.create(bar, name, code))
        );

        // 스터디 카테고리 (2개)
        Map<String, String> studyCategories = Map.of(
                "도서관", "LIBRARY",
                "스터디카페", "STUDY_CAFE"
        );

        studyCategories.forEach((name, code) ->
                placeCategoryRepository.save(PlaceCategory.create(study, name, code))
        );

        log.info("PlaceCategories 생성 완료: {} records", placeCategoryRepository.count());
    }
}
