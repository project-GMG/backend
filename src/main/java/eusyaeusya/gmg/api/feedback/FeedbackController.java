package eusyaeusya.gmg.api.feedback;

import eusyaeusya.gmg.common.api.response.ApiResponse;
import eusyaeusya.gmg.common.api.exception.RateLimitException;
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
    private final FeedbackRateLimiter rateLimiter;

    @PostMapping
    public ApiResponse<Void> submitFeedback(
            @Valid @RequestBody FeedbackRequest request,
            HttpServletRequest httpRequest) {
        String ip = resolveClientIp(httpRequest);
        if (!rateLimiter.isAllowed(ip)) {
            throw new RateLimitException(FeedbackErrorCode.RATE_LIMIT_EXCEEDED,
                    "피드백 요청 제한 횟수를 초과했습니다. 잠시 후 다시 시도해주세요.");
        }

        String userAgent = httpRequest.getHeader("User-Agent");

        log.info("POST /api/feedback - 피드백 접수: rating={}, page={}, ip={}", request.rating(), request.page(), ip);

        Feedback feedback = Feedback.create(
                request.rating(),
                request.comment(),
                request.page(),
                userAgent);

        feedbackRepository.save(feedback);

        return ApiResponse.success(FeedbackSuccessCode.FEEDBACK_SUBMITTED);
    }

    private String resolveClientIp(HttpServletRequest request) {
        // Cloudflare IP 헤더 최우선 확인
        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isBlank()) {
            return cfConnectingIp.trim();
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
