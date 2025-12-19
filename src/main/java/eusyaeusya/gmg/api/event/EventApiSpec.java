package eusyaeusya.gmg.api.event;

import eusyaeusya.gmg.api.event.request.EventCreateRequest;
import eusyaeusya.gmg.api.event.response.EventCreateResponse;
import eusyaeusya.gmg.api.event.response.EventMainResponse;
import eusyaeusya.gmg.api.event.response.EventPlaceTypeCategoriesResponse;
import eusyaeusya.gmg.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Event API", description = "이벤트 관련 기능을 제공합니다.")
public interface EventApiSpec {
    @Operation(
            summary = "새로운 이벤트 생성",
            description = "이벤트 생성 요청(EventCreateRequest)을 받아 새로운 이벤트를 생성합니다. 성공 시 생성된 이벤트 정보를 반환합니다."
    )
    @PostMapping
    ApiResponse<EventCreateResponse> createEvent(@Valid @RequestBody EventCreateRequest request);

    @Operation(
            summary = "이벤트의 선택 가능한 카테고리 조회",
            description = """
                    참여자가 비선호 선택 시 사용 가능한 카테고리 목록을 조회합니다.
                    모임장이 이벤트 생성 시 선택한 장소 타입(PlaceType)의 하위 카테고리만 반환됩니다.
                    """
    )
    @GetMapping("/{hashUrl}/categories")
    ApiResponse<EventPlaceTypeCategoriesResponse> getAvailableCategoriesForEvent(
            @Parameter(description = "이벤트 해시 URL", example = "abc123")
            @PathVariable String hashUrl
    );

    @Operation(
            summary = "이벤트 메인 페이지 정보 조회",
            description = """
                    초기 히트맵 데이터를 포함한 모임 정보를 조회합니다
                    """
    )
    @GetMapping("/{hashUrl}")
    ApiResponse<EventMainResponse> getEventMain(@PathVariable String hashUrl);

    @Operation(
            summary = "히트맵 실시간 스트림 구독",
            description = """
                    SSE 방식으로 실시간으로 사용자 데이터를 업데이트 하기 위해 구독합니다.
                    """
    )
    @GetMapping(value = "/{hashUrl}/heatmap/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter streamHeatmap(@PathVariable String hashUrl);
}
