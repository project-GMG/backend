package eusyaeusya.gmg.api.event;

import eusyaeusya.gmg.api.event.request.EventCreateRequest;
import eusyaeusya.gmg.api.event.response.EventCreateResponse;
import eusyaeusya.gmg.api.event.response.EventMainResponse;
import eusyaeusya.gmg.api.event.response.EventPlaceTypeCategoriesResponse;
import eusyaeusya.gmg.api.event.response.EventSuccessCode;
import eusyaeusya.gmg.common.api.response.ApiResponse;
import eusyaeusya.gmg.config.sse.SseService;
import eusyaeusya.gmg.domain.event.service.EventPlaceTypeService;
import eusyaeusya.gmg.domain.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController implements EventApiSpec {

    private final EventService eventService;
    private final EventPlaceTypeService eventPlaceTypeService;
    private final SseService sseService;

    @Override
    @PostMapping
    public ApiResponse<EventCreateResponse> createEvent(@Valid @RequestBody EventCreateRequest request) {
        log.info("POST /events - 새로운 이벤트 생성: {}", request.title());
        EventCreateResponse response = eventService.createEvent(request);

        return ApiResponse.successWithData(EventSuccessCode.EVENT_RETRIEVED, response);
    }

    @Override
    @GetMapping("/{hashUrl}/categories")
    public ApiResponse<EventPlaceTypeCategoriesResponse> getAvailableCategoriesForEvent(
            @PathVariable String hashUrl
    ) {
        log.info("GET /events/{}/categories - 이벤트의 선택 가능한 카테고리 조회", hashUrl);
        EventPlaceTypeCategoriesResponse response =
                eventPlaceTypeService.getAvailableCategoriesForEvent(hashUrl);

        return ApiResponse.successWithData(
                EventSuccessCode.EVENT_PLACE_TYPES_CATEGORIES_RETRIEVED,
                response
        );
    }

    @Override
    @GetMapping("/{hashUrl}")
    public ApiResponse<EventMainResponse> getEventMain(@PathVariable String hashUrl) {
        log.info("GET /events/{} - 이벤트 메인 페이지 정보 조회", hashUrl);
        EventMainResponse response = eventService.getEventMain(hashUrl);

        return ApiResponse.successWithData(EventSuccessCode.EVENT_MAIN_RETRIEVED, response);
    }


    @GetMapping(value = "/{hashUrl}/heatmap/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamHeatmap(@PathVariable String hashUrl) {
        log.info("GET /events/{}/heatmap/stream - SSE 구독 시작", hashUrl);

        return sseService.subscribe(hashUrl);
    }
}
