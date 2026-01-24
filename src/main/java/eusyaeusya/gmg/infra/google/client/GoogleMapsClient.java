package eusyaeusya.gmg.infra.google.client;

import eusyaeusya.gmg.infra.google.config.GoogleMapsProperties;
import eusyaeusya.gmg.infra.google.dto.GooglePlaceDetailsResponse;
import eusyaeusya.gmg.infra.google.dto.GooglePlaceSearchResponse;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class GoogleMapsClient {

    private final GoogleMapsProperties properties;
    private final WebClient webClient;

    public GoogleMapsClient(GoogleMapsProperties properties) {
        this.properties = properties;
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 연결 타임아웃 5초
                .responseTimeout(Duration.ofSeconds(10)) // 응답 타임아웃 10초
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS)));

        this.webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("X-Goog-Api-Key", properties.getKey())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        log.info("GoogleMapsClient 초기화 완료: baseUrl={}", properties.getBaseUrl());
    }

    /**
     * 장소 이름과 주소로 Google Place ID 조회 (Text Search v1)
     */
    public Optional<String> findPlaceId(String name, String address) {
        log.info("Google Place ID 조회 시작: name={}", name);

        if (!properties.hasKey()) {
            return Optional.empty();
        }

        String textQuery = name + " " + address;
        Map<String, String> body = new HashMap<>(Map.of("textQuery", textQuery));
        body.put("languageCode", "ko");
        body.put("regionCode", "KR");
        body.put("pageSize", "3");

        try {
            GooglePlaceSearchResponse response = webClient.post()
                    .uri("/v1/places:searchText")
                    .header("X-Goog-FieldMask", "places.id,places.displayName")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(GooglePlaceSearchResponse.class)
                    .block();
            log.info("Google Place ID 조회 완료: name={}", name);
            if (response != null && response.places() != null && !response.places().isEmpty()) {
                return Optional.ofNullable(response.places().getFirst().id());
            }
        } catch (Exception e) {
            log.error("Google Place ID 조회 실패 (v1): name={}, address={}", name, address, e);
        }

        return Optional.empty();
    }

    /**
     * Place ID로 장소 상세 정보 조회 (Place Details v1)
     */
    public Optional<GooglePlaceDetailsResponse> getPlaceDetails(String placeId) {
        log.info("Google Place 상세 정보 조회 시작: placeId={}", placeId);
        if (!properties.hasKey()) {
            return Optional.empty();
        }

        try {
            Optional<GooglePlaceDetailsResponse> placeDetails = webClient.get()
                    .uri("/v1/places/{placeId}", placeId)
                    .header("X-Goog-FieldMask", "id,displayName,regularOpeningHours,photos,rating")
                    .header("Accept-Language", "ko")
                    .retrieve()
                    .bodyToMono(GooglePlaceDetailsResponse.class)
                    .map(Optional::ofNullable)
                    .block();
            log.info("Google Place 상세 정보 조회 완료: placeId={}", placeId);
            return placeDetails;
        } catch (Exception e) {
            log.error("Google Place 상세 정보 조회 실패: placeId={}", placeId, e);
        }

        return Optional.empty();
    }

    /**
     * Photo Resource Name으로 이미지 URL 생성 (Place Media v1)
     */
    public String getPhotoUrl(String photoName, int maxWidth, int maxHeight) {
        log.info("Google Place 사진 URL 생성 시작");
        String url = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path("/v1/{photoName}/media")
                .queryParam("maxWidthPx", maxWidth)
                .queryParam("maxHeightPx", maxHeight)
                .queryParam("key", properties.getKey())
                .buildAndExpand(photoName)
                .toUriString();
        log.info("Google Place 사진 URL 생성 완료");
        return url;
    }
}
