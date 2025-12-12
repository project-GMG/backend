package eusyaeusya.gmg.api.place;

import eusyaeusya.gmg.api.place.response.PlaceListResponse;
import eusyaeusya.gmg.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Place API", description = "장소 관련 기능을 제공합니다")
public interface PlaceApiSpec {
    @Operation(
            summary = "이벤트 반경 내 매장 조회",
            description = """
                    이벤트 중심 좌표 기준 반경 500m 이내의 매장을 조회합니다.
                    요청한 카테고리가 이벤트에서 선택한 PlaceType에 속하지 않으면 400 에러를 반환합니다.
                    """
    )
    @GetMapping
    ApiResponse<PlaceListResponse> getPlaces(
            @Parameter(description = "이벤트 해시 URL", example = "abc123")
            @PathVariable String hashUrl,

            @Parameter(description = "장소 카테고리 ID", example = "1")
            @RequestParam Long categoryId,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "16")
            @RequestParam(defaultValue = "16") int size
    );
}
