package eusyaeusya.gmg.api.place;

import eusyaeusya.gmg.api.place.response.PlaceSuccessCode;
import eusyaeusya.gmg.api.place.response.PlaceTypeResponse;
import eusyaeusya.gmg.common.api.response.ApiResponse;
import eusyaeusya.gmg.domain.place.service.PlaceTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/place-types")
@RequiredArgsConstructor
public class PlaceTypeController implements PlaceTypeApiSpec {
    private final PlaceTypeService placeTypeService;

    @Override
    @GetMapping
    public ApiResponse<List<PlaceTypeResponse>> getAllPlaceTypes() {
        log.info("GET /place-types - 모든 장소 타입 목록 반환");
        List<PlaceTypeResponse> placeTypes = placeTypeService.getAllPlaceTypes();
        return ApiResponse.successWithData(PlaceSuccessCode.PLACE_TYPES_RETRIEVED, placeTypes);
    }
}
