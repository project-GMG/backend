package eusyaeusya.gmg.config;

import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.repository.PlaceTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDataInitializer implements ApplicationRunner {
    private final PlaceTypeRepository placeTypeRepository;

    @Override
    public void run(ApplicationArguments args) {
        initializePlaceTypes();
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
}
