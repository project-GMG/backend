package eusyaeusya.gmg.api.feedback;

import eusyaeusya.gmg.common.api.response.ApiResponse;
import eusyaeusya.gmg.domain.feedback.entity.Feedback;
import eusyaeusya.gmg.domain.feedback.repository.FeedbackRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;

    @PostMapping
    public ApiResponse<Void> submitFeedback(
            @Valid @RequestBody FeedbackRequest request,
            HttpServletRequest httpRequest
    ) {
        String userAgent = httpRequest.getHeader("User-Agent");

        log.info("POST /api/feedback - 피드백 접수: rating={}, page={}", request.rating(), request.page());

        Feedback feedback = Feedback.create(
                request.rating(),
                request.comment(),
                request.page(),
                userAgent
        );

        feedbackRepository.save(feedback);

        return ApiResponse.success(FeedbackSuccessCode.FEEDBACK_SUBMITTED);
    }
}
