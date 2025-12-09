package eusyaeusya.gmg.api.participant;

import eusyaeusya.gmg.api.participant.requset.ParticipantNameJoinRequest;
import eusyaeusya.gmg.api.participant.response.ParticipantNameJoinResponse;
import eusyaeusya.gmg.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Participant API", description = "참여자 관련 기능을 제공합니다")
public interface ParticipantApiSpec {
    @Operation(
            summary = "참여자 등록",
            description = "이벤트에 참여자를 등록합니다. 이벤트가 OPEN 상태일 때만 가능합니다"
    )
    @PostMapping
    ApiResponse<ParticipantNameJoinResponse> joinEvent(
            @Parameter(description = "이벤트 해시 URL", example = "abc123")
            @PathVariable
            String hashUrl,
            @Valid @RequestBody ParticipantNameJoinRequest request);
}
