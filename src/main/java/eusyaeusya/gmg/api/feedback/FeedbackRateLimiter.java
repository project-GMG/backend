package eusyaeusya.gmg.api.feedback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@Component
public class FeedbackRateLimiter {

    private final ConcurrentHashMap<String, Deque<Instant>> requestLog = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS = 5;
    private static final Duration WINDOW = Duration.ofHours(1);

    public boolean isAllowed(String ip) {
        if (ip == null || ip.isBlank()) {
            return true;
        }

        Deque<Instant> timestamps = requestLog.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        Instant cutoff = Instant.now().minus(WINDOW);

        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= MAX_REQUESTS) {
            log.warn("Rate limit exceeded for IP: {}. Requests in last hour: {}", ip, timestamps.size());
            return false;
        }

        timestamps.addLast(Instant.now());
        return true;
    }
}
