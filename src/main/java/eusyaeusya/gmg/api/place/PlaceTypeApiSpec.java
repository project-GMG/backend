package eusyaeusya.gmg.api.place;

import eusyaeusya.gmg.api.place.response.PlaceTypeResponse;
import eusyaeusya.gmg.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "PlaceTypes 조회 API")
public interface PlaceTypeApiSpec {
    @Operation(
            summary = "PlaceTypes 조회",
            description = "장소 타입 목록을 반환합니다."
    )
    ApiResponse<List<PlaceTypeResponse>> getAllPlaceTypes();
}
