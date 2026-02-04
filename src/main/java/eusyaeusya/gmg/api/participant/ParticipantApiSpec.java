package eusyaeusya.gmg.api.participant;

import eusyaeusya.gmg.api.participant.request.ParticipantDislikedRequest;
import eusyaeusya.gmg.api.participant.request.ParticipantNameJoinRequest;
import eusyaeusya.gmg.api.participant.request.ParticipantUnavailableTimeRequest;
import eusyaeusya.gmg.api.participant.response.*;
import eusyaeusya.gmg.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Participant API", description = "참여자 관련 기능을 제공합니다")
public interface ParticipantApiSpec {
    @Operation(
            summary = "참여자 이름 등록",
            description = "참여자 이름을 등록합니다. 이벤트가 OPEN 상태일 때만 가능합니다"
    )
    @PostMapping
    ApiResponse<ParticipantNameJoinResponse> joinEvent(
            @Parameter(description = "이벤트 해시 URL", example = "abc123")
            @PathVariable
            String hashUrl,
            @Valid @RequestBody ParticipantNameJoinRequest request);

    @Operation(
            summary = "참여자의 불가능한 시간 등록",
            description = "참여자의 불가능한 시간대를 등록합니다."
    )
    @PostMapping
    ApiResponse<ParticipantUnavailableTimeResponse> registerUnavailableTimes(
            @Parameter(description = "이벤트 해시 URL", example = "abc123")
            @PathVariable String hashUrl,
            @Parameter(description = "사용자 id", example = "1")
            @PathVariable Long participantId,
            @Valid @RequestBody ParticipantUnavailableTimeRequest request
    );

    @Operation(
            summary = "참여자의 비선호 카테고리 및 장소 등록",
            description = "참여자의 비선호 카테고리와 장소를 등록합니다."
    )
    @PostMapping
    ApiResponse<ParticipantDislikedResponse> registerDisliked(
            @Parameter(description = "이벤트 해시 URL", example = "abc123")
            @PathVariable String hashUrl,
            @Parameter(description = "참여자 ID", example = "1")
            @PathVariable Long participantId,
            @Valid @RequestBody ParticipantDislikedRequest request
    );

    @Operation(
            summary = "참여자 정보 입력 완료",
            description = "참여자의 시간/장소 비선호 입력을 완료하고 COMPLETED 상태로 변경합니다."
    )
    @PostMapping
    ApiResponse<ParticipantCompleteResponse> completeParticipation(
            @Parameter(description = "이벤트 해시 URL", example = "abc123")
            @PathVariable String hashUrl,
            @Parameter(description = "참여자 ID", example = "1")
            @PathVariable Long participantId
    );

    @Operation(
            summary = "참여자의 불가능한 시간 조회",
            description = "참여자가 등록한 불가능한 시간대를 조회합니다."
    )
    @GetMapping("/{participantId}/unavailable-times")
    ApiResponse<ParticipantUnavailableTimeListResponse> getUnavailableTimes(
            @Parameter(description = "이벤트 해시 URL", example = "abc123")
            @PathVariable String hashUrl,
            @Parameter(description = "참여자 ID", example = "1")
            @PathVariable Long participantId
    );
}
