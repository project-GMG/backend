package eusyaeusya.gmg.config.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import eusyaeusya.gmg.api.event.response.EventHeatmapStreamResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private static final Long DEFAULT_TIMEOUT = 60 * 60 * 1000L; // 1시간
    private final ObjectMapper objectMapper;

    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emittersByEvent = new ConcurrentHashMap<>();

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
            log.warn("SSE 구독 에러: hashUrl={}", hashUrl, e);
            emitters.remove(emitter);
            emitter.completeWithError(e);
            cleanupEmptyList(hashUrl);
        });

        return emitter;
    }

    public void broadcast(String hashUrl, EventHeatmapStreamResponse heatmapData) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByEvent.get(hashUrl);

        if (emitters == null || emitters.isEmpty()) {
            log.debug("브로드캐스트 대상 없음: hashUrl={}", hashUrl);
            return;
        }

        log.info("히트맵 브로드캐스트 시작: hashUrl={}, 구독자 수={}", hashUrl, emitters.size());

        int successCount = 0;
        int failCount = 0;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("heatmap-update")
                        .data(heatmapData));
                successCount++;
            } catch (IOException e) {
                log.warn("SSE 전송 실패: hashUrl={}", hashUrl, e);
                emitters.remove(emitter);
                emitter.completeWithError(e);
                failCount++;
            }
        }

        log.info("히트맵 브로드캐스트 완료: hashUrl={}, 성공={}, 실패={}",
                hashUrl, successCount, failCount);

        cleanupEmptyList(hashUrl);
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
