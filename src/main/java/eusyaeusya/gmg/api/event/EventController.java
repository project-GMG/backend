package eusyaeusya.gmg.api.event;

import eusyaeusya.gmg.api.event.request.EventCreateRequest;
import eusyaeusya.gmg.api.event.response.EventCreateResponse;
import eusyaeusya.gmg.api.event.response.EventSuccessCode;
import eusyaeusya.gmg.common.api.response.ApiResponse;
import eusyaeusya.gmg.domain.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController implements EventApiSpec {

    private final EventService eventService;

    @Override
    @PostMapping
    public ApiResponse<EventCreateResponse> createEvent(@Valid @RequestBody EventCreateRequest request) {
        log.info("POST /events - 새로운 이벤터 생성: {}", request.title());
        EventCreateResponse response = eventService.createEvent(request);

        return ApiResponse.successWithData(EventSuccessCode.EVENT_RETRIEVED, response);
    }
}
