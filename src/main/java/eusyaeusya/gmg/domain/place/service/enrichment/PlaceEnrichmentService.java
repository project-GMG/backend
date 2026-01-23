package eusyaeusya.gmg.domain.place.service.enrichment;

import eusyaeusya.gmg.domain.place.entity.Place;
import eusyaeusya.gmg.domain.place.util.OpeningHours;
import eusyaeusya.gmg.infra.google.client.GoogleMapsClient;
import eusyaeusya.gmg.infra.google.dto.GooglePlaceDetailsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceEnrichmentService {

    private final GoogleMapsClient googleMapsClient;

    /**
     * 구글 API를 사용하여 단일 장소 정보를 보완합니다.
     */
    @Transactional
    public void enrichPlace(Place place) {
        if (place.getImageUrl() != null && place.getOpenHoursJson() != null) {
            log.debug("이미 정보가 존재하는 장소입니다. 건너뜁니다: id={}, name={}", place.getId(), place.getName());
            return;
        }

        log.info("장소 정보 보완 시작: id={}, name={}", place.getId(), place.getName());

        Optional<String> placeIdOpt = googleMapsClient.findPlaceId(place.getName(), place.getAddress());
        if (placeIdOpt.isEmpty()) {
            log.warn("구글 Place ID를 찾을 수 없습니다: name={}", place.getName());
            return;
        }

        String googlePlaceId = placeIdOpt.get();
        Optional<GooglePlaceDetailsResponse> detailsOpt = googleMapsClient.getPlaceDetails(googlePlaceId);

        if (detailsOpt.isPresent()) {
            GooglePlaceDetailsResponse details = detailsOpt.get();

            // 이미지 URL 보완 (첫 번째 사진 사용)
            String imageUrl = null;
            if (details.photos() != null && !details.photos().isEmpty()) {
                imageUrl = googleMapsClient.getPhotoUrl(details.photos().get(0).name(), 800, 800);
            }

            // 영업시간 보완
            String openHoursJson = null;
            if (details.regularOpeningHours() != null && details.regularOpeningHours().periods() != null) {
                openHoursJson = OpeningHours.fromGooglePeriods(details.regularOpeningHours().periods());
            }

            // 평점 보완
            BigDecimal rating = null;
            if (details.rating() != null) {
                rating = BigDecimal.valueOf(details.rating());
            }

            place.updateInfoFromGoogle(imageUrl, openHoursJson, rating);
            log.info("장소 정보 보완 완료: id={}, name={}, hasImage={}, hasOpenHours={}, rating={}",
                    place.getId(), place.getName(), imageUrl != null, openHoursJson != null, rating);
        }
    }

    /**
     * 여러 장소 정보를 비동기적으로 보완합니다.
     */
    @Transactional
    public void enrichPlaces(List<Place> places) {
        log.info("총 {}개의 장소 정보 보완 시작", places.size());

        List<CompletableFuture<Void>> futures = places.stream()
                .map(place -> CompletableFuture.runAsync(() -> enrichPlaceInNewTransaction(place)))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        log.info("총 {}개의 장소 정보 보완 완료", places.size());
    }

    /**
     * 별도의 트랜잭션에서 개별 장소 보완 로직을 실행합니다.
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void enrichPlaceInNewTransaction(Place place) {
        enrichPlace(place);
    }
}
