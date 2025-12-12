package eusyaeusya.gmg.api.participant;

import eusyaeusya.gmg.api.participant.requset.ParticipantNameJoinRequest;
import eusyaeusya.gmg.api.participant.requset.ParticipantUnavailableTimeRequest;
import eusyaeusya.gmg.api.participant.response.ParticipantNameJoinResponse;
import eusyaeusya.gmg.api.participant.response.ParticipantSuccessCode;
import eusyaeusya.gmg.api.participant.response.ParticipantUnavailableTimeResponse;
import eusyaeusya.gmg.common.api.response.ApiResponse;
import eusyaeusya.gmg.domain.participant.service.ParticipantService;
import eusyaeusya.gmg.domain.participant.service.ParticipantUnavailableTimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/event/{hashUrl}/participants")
@RequiredArgsConstructor
public class ParticipantController implements ParticipantApiSpec {

    private final ParticipantService participantService;
    private final ParticipantUnavailableTimeService unavailableTimeService;

    @Override
    @PostMapping
    public ApiResponse<ParticipantNameJoinResponse> joinEvent(
            @PathVariable String hashUrl,
            @Valid @RequestBody ParticipantNameJoinRequest request) {
        log.info("POST /event/{}/participants - 참여자 등록: {}", hashUrl, request.name());

        ParticipantNameJoinResponse response = participantService.joinEvent(hashUrl, request);

        return ApiResponse.successWithData(ParticipantSuccessCode.PARTICIPANT_JOINED, response);
    }

    @Override
    @PostMapping("/{participantId}/unavailble-times")
    public ApiResponse<ParticipantUnavailableTimeResponse> registerUnavailableTimes(
            @PathVariable String hashUrl,
            @PathVariable Long participantId,
            @Valid @RequestBody ParticipantUnavailableTimeRequest request
    ) {
        log.info("POST /event/{}/participants/{}/unavailable-times - 불가능 시간 등록: count={}",
                hashUrl, participantId, request.unavailableTimes().size());

        ParticipantUnavailableTimeResponse response =
                unavailableTimeService.registerUnavailableTimes(hashUrl, participantId, request);

        return ApiResponse.successWithData(
                ParticipantSuccessCode.UNAVAILABLE_TIME_REGISTERED,
                response
        );
    }
}
