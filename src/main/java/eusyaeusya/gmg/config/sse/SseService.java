package eusyaeusya.gmg.config.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private static final Long DEFAULT_TIMEOUT = 60 * 60 * 1000L; // 1시간
    private static final String HEARTBEAT_EVENT_NAME = "heartbeat";
    private static final String HEARTBEAT_DATA = "keep-alive";
    private final ObjectMapper objectMapper;

    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emittersByEvent = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 10000)
    public void sendHeartbeat() {
        if (emittersByEvent.isEmpty()) {
            return;
        }

        log.debug("SSE Heartbeat 전송 시작");
        emittersByEvent.forEach((hashUrl, emitters) -> {
            List<SseEmitter> deadEmitters = new ArrayList<>();
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .comment("keep-alive"));
                } catch (IOException e) {
                    log.warn("Heartbeat 전송 실패, emitter 제거: {}", e.getMessage());
                    deadEmitters.add(emitter);
                }
            }
            if (!deadEmitters.isEmpty()) {
                emitters.removeAll(deadEmitters);
                cleanupEmptyList(hashUrl);
            }
        });
    }

    public SseEmitter subscribe(String hashUrl) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        CopyOnWriteArrayList<SseEmitter> emitters = emittersByEvent.computeIfAbsent(
                hashUrl,
                k -> new CopyOnWriteArrayList<>()
        );
        emitters.add(emitter);

        log.info("SSE 구독 추가: hashUrl={}, 현재 구독자 수={}", hashUrl, emitters.size());

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE connection established"));
        } catch (IOException e) {
            log.warn("SSE 연결 완료 이벤트 전송 실패: hashUrl={}", hashUrl, e);
            emitters.remove(emitter);
            return emitter;
        }

        emitter.onCompletion(() -> {
            log.info("SSE 구독 완료: hashUrl={}", hashUrl);
            emitters.remove(emitter);
            cleanupEmptyList(hashUrl);
        });

        emitter.onTimeout(() -> {
            log.info("SSE 구독 타임아웃: hashUrl={}", hashUrl);
            emitters.remove(emitter);
            emitter.complete();
            cleanupEmptyList(hashUrl);
        });

        emitter.onError(e -> {
            log.debug("SSE 구독 에러: hashUrl={}, error={}", hashUrl, e.getMessage());
            emitters.remove(emitter);
            cleanupEmptyList(hashUrl);
        });

        return emitter;
    }

    public <T> void broadcast(String hashUrl, String eventName, T data) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByEvent.get(hashUrl);

        if (emitters == null || emitters.isEmpty()) {
            log.debug("브로드캐스트 대상 없음: hashUrl={}, eventName={}", hashUrl, eventName);
            return;
        }

        String jsonData;
        try {
            jsonData = objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("데이터 직렬화 실패: hashUrl={}, eventName={}", hashUrl, eventName, e);
            return;
        }

        log.info("브로드캐스트 시작: hashUrl={}, eventName={}, 구독자={}, 크기={}bytes",
                hashUrl, eventName, emitters.size(), jsonData.length());

        int successCount = 0;
        int failCount = 0;
        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(jsonData, MediaType.APPLICATION_JSON));
                successCount++;
            } catch (IOException e) {
                String msg = e.getMessage();
                if (msg != null && (msg.contains("Broken pipe") ||
                        msg.contains("Connection reset"))) {
                    log.debug("SSE 연결 종료됨: hashUrl={}, eventName={}", hashUrl, eventName);
                } else {
                    log.warn("SSE 전송 실패: hashUrl={}, eventName={}, error={}",
                            hashUrl, eventName, msg);
                }
                deadEmitters.add(emitter);
                failCount++;
            }
        }

        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
        }

        log.info("브로드캐스트 완료: hashUrl={}, eventName={}, 성공={}, 실패={}",
                hashUrl, eventName, successCount, failCount);

        cleanupEmptyList(hashUrl);
    }

    public void broadcast(String hashUrl, Object heatmapData) {
        broadcast(hashUrl, "heatmap-update", heatmapData);
    }

    private void cleanupEmptyList(String hashUrl) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByEvent.get(hashUrl);
        if (emitters != null && emitters.isEmpty()) {
            emittersByEvent.remove(hashUrl);
            log.debug("빈 emitter 리스트 제거: hashUrl={}", hashUrl);
        }
    }

    public void closeAll(String hashUrl) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByEvent.remove(hashUrl);

        if (emitters != null) {
            log.info("모든 SSE 구독 종료: hashUrl={}, 구독자 수={}", hashUrl, emitters.size());
            emitters.forEach(SseEmitter::complete);
            emitters.clear();
        }
    }

    public int getSubscriberCount(String hashUrl) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByEvent.get(hashUrl);
        return emitters != null ? emitters.size() : 0;
    }
}