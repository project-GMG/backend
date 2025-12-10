package eusyaeusya.gmg.api.event;

import eusyaeusya.gmg.api.event.request.EventCreateRequest;
import eusyaeusya.gmg.api.event.response.EventCreateResponse;
import eusyaeusya.gmg.api.event.response.EventPlaceTypeCategoriesResponse;
import eusyaeusya.gmg.api.event.response.EventSuccessCode;
import eusyaeusya.gmg.common.api.response.ApiResponse;
import eusyaeusya.gmg.domain.event.service.EventPlaceTypeService;
import eusyaeusya.gmg.domain.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController implements EventApiSpec {

    private final EventService eventService;
    private final EventPlaceTypeService eventPlaceTypeService;

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
}
