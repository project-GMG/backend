package eusyaeusya.gmg.api.event;

import eusyaeusya.gmg.api.event.request.EventCreateRequest;
import eusyaeusya.gmg.api.event.response.EventCreateResponse;
import eusyaeusya.gmg.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Event API", description = "이벤트 관련 기능을 제공합니다.")
public interface EventApiSpec {
    @Operation(
            summary = "새로운 이벤트 생성",
            description = "이벤트 생성 요청(EventCreateRequest)을 받아 새로운 이벤트를 생성합니다. 성공 시 생성된 이벤트 정보를 반환합니다."
    )
    @PostMapping
    ApiResponse<EventCreateResponse> createEvent(@Valid @RequestBody EventCreateRequest request);
}
