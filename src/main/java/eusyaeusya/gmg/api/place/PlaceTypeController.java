package eusyaeusya.gmg.api.place;

import eusyaeusya.gmg.api.place.response.PlaceSuccessCode;
import eusyaeusya.gmg.api.place.response.PlaceTypeResponse;
import eusyaeusya.gmg.common.api.response.ApiResponse;
import eusyaeusya.gmg.domain.place.service.PlaceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/place-types")
@RequiredArgsConstructor
public class PlaceTypeController implements PlaceTypeApiSpec {
    private final PlaceTypeService placeTypeService;

    @Override
    @GetMapping
    public ApiResponse<List<PlaceTypeResponse>> getAllPlaceTypes() {
        List<PlaceTypeResponse> placeTypes = placeTypeService.getAllPlaceTypes();
        return ApiResponse.successWithData(PlaceSuccessCode.PLACE_TYPES_RETRIEVED, placeTypes);
    }
}
