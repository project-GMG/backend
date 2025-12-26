package eusyaeusya.gmg.api.place;

import eusyaeusya.gmg.api.place.response.PlaceListResponse;
import eusyaeusya.gmg.api.place.response.PlaceRecommendationResponse;
import eusyaeusya.gmg.api.place.response.PlaceSuccessCode;
import eusyaeusya.gmg.common.api.response.ApiResponse;
import eusyaeusya.gmg.domain.place.service.PlaceRecommendationService;
import eusyaeusya.gmg.domain.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/events/{hashUrl}/places")
@RequiredArgsConstructor
public class PlaceController implements PlaceApiSpec {
    private final PlaceService placeService;
    private final PlaceRecommendationService recommendationService;

    @Override
    @GetMapping
    public ApiResponse<PlaceListResponse> getPlaces(
            @PathVariable String hashUrl,
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size
    ) {
        log.info("GET /events/{}/places - 매장 조회: categoryId={}, page={}, size={}",
                hashUrl, categoryId, page, size);

        PlaceListResponse response = placeService.getPlacesWithinRadius(
                hashUrl, categoryId, page, size
        );

        return ApiResponse.successWithData(
                PlaceSuccessCode.PLACES_RETRIEVED,
                response
        );
    }

    @Override
    @GetMapping("/recommendations")
    public ApiResponse<PlaceRecommendationResponse> getRecommendations(@PathVariable String hashUrl) {
        log.info("추천 장소 조회 요청: hashUrl={}", hashUrl);

        PlaceRecommendationResponse response = recommendationService.generateRecommendations(hashUrl);

        log.info("추천 장소 조회 완료: hashUrl={}, 카테고리 수={}", hashUrl, response.recommendations().size());

        return ApiResponse.successWithData(PlaceSuccessCode.RECOMMENDATION_PLACE, response);
    }
}
