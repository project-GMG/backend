package eusyaeusya.gmg.api.participant;

import eusyaeusya.gmg.api.participant.request.ParticipantDislikedRequest;
import eusyaeusya.gmg.api.participant.request.ParticipantNameJoinRequest;
import eusyaeusya.gmg.api.participant.request.ParticipantUnavailableTimeRequest;
import eusyaeusya.gmg.api.participant.response.*;
import eusyaeusya.gmg.common.api.response.ApiResponse;
import eusyaeusya.gmg.domain.participant.service.ParticipantDislikedService;
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
    private final ParticipantDislikedService dislikedService;

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
    @PostMapping("/{participantId}/unavailable-times")
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

    @Override
    @PostMapping("/{participantId}/disliked")
    public ApiResponse<ParticipantDislikedResponse> registerDisliked(
            @PathVariable String hashUrl,
            @PathVariable Long participantId,
            @Valid @RequestBody ParticipantDislikedRequest request
    ) {
        log.info("POST /events/{}/participants/{}/disliked - 비선호 등록: categoryCount={}, placeCount={}",
                hashUrl, participantId,
                request.dislikedCategoryIds().size(),
                request.dislikedPlaceIds().size());

        ParticipantDislikedResponse response =
                dislikedService.registerDisliked(hashUrl, participantId, request);

        return ApiResponse.successWithData(
                ParticipantSuccessCode.DISLIKED_REGISTERED,
                response
        );
    }

    @Override
    @PostMapping("/{participantId}/complete")
    public ApiResponse<ParticipantCompleteResponse> completeParticipation(
            @PathVariable String hashUrl,
            @PathVariable Long participantId
    ) {
        log.info("POST /event/{}/participants/{}/complete - 참여자 정보 입력 완료",
                hashUrl, participantId);

        ParticipantCompleteResponse response =
                participantService.completeParticipation(hashUrl, participantId);

        return ApiResponse.successWithData(
                ParticipantSuccessCode.PARTICIPANT_COMPLETED,
                response
        );
    }

    @Override
    @GetMapping("/{participantId}/unavailable-times")
    public ApiResponse<ParticipantUnavailableTimeListResponse> getUnavailableTimes(
            @PathVariable String hashUrl,
            @PathVariable Long participantId
    ) {
        log.info("GET /event/{}/participants/{}/unavailable-times - 불가능 시간 조회",
                hashUrl, participantId);

        ParticipantUnavailableTimeListResponse response =
                unavailableTimeService.getUnavailableTimes(hashUrl, participantId);

        return ApiResponse.successWithData(
                ParticipantSuccessCode.UNAVAILABLE_TIME_RETRIEVED,
                response
        );
    }

    @Override
    @GetMapping("/check")
    public ApiResponse<ParticipantCheckResponse> checkParticipant(
            @PathVariable String hashUrl,
            @RequestParam String name) {
        log.info("GET /event/{}/participants/check - 참여자 존재 여부 확인: name={}", hashUrl, name);

        ParticipantCheckResponse response = participantService.checkParticipant(hashUrl, name);

        return ApiResponse.successWithData(ParticipantSuccessCode.PARTICIPANT_CHECK_SUCCESS, response);
    }
}
