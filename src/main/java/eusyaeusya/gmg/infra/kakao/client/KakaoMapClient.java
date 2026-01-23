package eusyaeusya.gmg.infra.kakao.client;

import eusyaeusya.gmg.infra.kakao.config.KakaoMapProperties;
import eusyaeusya.gmg.infra.kakao.dto.KakaoApiResponse;
import eusyaeusya.gmg.infra.kakao.dto.KakaoPlaceDto;
import eusyaeusya.gmg.infra.kakao.exception.KakaoMapApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@Profile("!local")
public class KakaoMapClient {
    private static final String KEYWORD_SEARCH_URI = "/v2/local/search/keyword.json";
    private static final int MAX_SIZE_PER_PAGE = 15; // 카카오맵 API 최대 페이지 크기
    private static final int MAX_PAGES = 3; // 최대 3페이지 (총 45개)

    private final WebClient webClient;
    private final KakaoMapProperties kakaoMapProperties;

    public KakaoMapClient(KakaoMapProperties kakaoMapProperties, WebClient.Builder webClientBuilder) {
        this.kakaoMapProperties = kakaoMapProperties;
        this.webClient = webClientBuilder
                .baseUrl(kakaoMapProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoMapProperties.getKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        log.info("KakaoMapClient 초기화 완료: baseUrl={}", kakaoMapProperties.getBaseUrl());
    }

    /**
     * 키워드 목록으로 장소 검색 (중복 제거 포함)
     */
    public List<KakaoPlaceDto> searchByKeywords(
            List<String> keywords,
            String x,
            String y,
            int radius
    ) {
        List<KakaoPlaceDto> allPlaces = new ArrayList<>();
        Set<String> seenIds = new HashSet<>(); // 중복 제거용

        for (String keyword : keywords) {
            List<KakaoPlaceDto> places = searchByKeywordWithPagination(
                    keyword, x, y, radius
            );

            // 중복 제거하면서 추가
            for (KakaoPlaceDto place : places) {
                if (seenIds.add(place.id())) {
                    allPlaces.add(place);
                }
            }
        }

        return allPlaces;
    }

    /**
     * 키워드 검색 (페이징 포함)
     */
    private List<KakaoPlaceDto> searchByKeywordWithPagination(
            String keyword,
            String x,
            String y,
            int radius
    ) {
        List<KakaoPlaceDto> allPlaces = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;

        while (hasMore && page <= MAX_PAGES) {
            try {
                int finalPage = page;
                KakaoApiResponse<KakaoPlaceDto> response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(KEYWORD_SEARCH_URI)
                                .queryParam("query", keyword)
                                .queryParam("x", x)
                                .queryParam("y", y)
                                .queryParam("radius", radius)
                                .queryParam("size", MAX_SIZE_PER_PAGE)
                                .queryParam("page", finalPage)
                                .queryParam("sort", "distance")
                                .build())
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<KakaoApiResponse<KakaoPlaceDto>>() {
                        })
                        .block();

                if (response == null || response.documents() == null) {
                    log.warn("키워드 검색 응답 비어있음: keyword={}, page={}", keyword, page);
                    break;
                }

                List<KakaoPlaceDto> places = response.documents();
                allPlaces.addAll(places);

                log.debug("키워드 '{}' 검색: page={}, count={}", keyword, page, places.size());

                hasMore = !response.meta().isEnd() && !places.isEmpty();
                page++;

            } catch (WebClientResponseException e) {
                log.error("키워드 검색 실패: keyword={}, status={}",
                        keyword, e.getStatusCode(), e);
                throw new KakaoMapApiException("키워드 검색 중 오류 발생", e);
            } catch (Exception e) {
                log.error("키워드 검색 중 예외 발생: keyword={}", keyword, e);
                throw new KakaoMapApiException("키워드 검색 중 알 수 없는 오류 발생", e);
            }
        }

        return allPlaces;
    }
}
