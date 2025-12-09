package eusyaeusya.gmg.api;

import eusyaeusya.gmg.api.participant.ParticipantApiSpec;
import eusyaeusya.gmg.api.participant.requset.ParticipantNameJoinRequest;
import eusyaeusya.gmg.api.participant.response.ParticipantNameJoinResponse;
import eusyaeusya.gmg.api.participant.response.ParticipantSuccessCode;
import eusyaeusya.gmg.common.api.response.ApiResponse;
import eusyaeusya.gmg.domain.participant.service.ParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/event/{hashUrl}/particiants")
@RequiredArgsConstructor
public class ParticipantController implements ParticipantApiSpec {

    private final ParticipantService participantService;

    @Override
    @PostMapping
    public ApiResponse<ParticipantNameJoinResponse> joinEvent(
            @PathVariable String hashUrl,
            @Valid @RequestBody ParticipantNameJoinRequest request) {
        log.info("POST /event/{}/participants - 참여자 등록: {}", hashUrl, request.name());

        ParticipantNameJoinResponse response = participantService.joinEvent(hashUrl, request);

        return ApiResponse.successWithData(ParticipantSuccessCode.PARTICIPANT_JOINED, response);
    }
}
