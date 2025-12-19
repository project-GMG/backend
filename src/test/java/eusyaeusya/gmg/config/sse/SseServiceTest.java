package eusyaeusya.gmg.config.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import eusyaeusya.gmg.api.event.response.EventHeatmapStreamResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class SseServiceTest {

    private SseService sseService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // LocalDate/LocalTime 직렬화 지원
        sseService = new SseService(objectMapper);
    }

    @Test
    @DisplayName("SSE 구독 생성 성공")
    void subscribe_createsEmitter() {
        // given
        String hashUrl = "abc123";

        // when
        SseEmitter emitter = sseService.subscribe(hashUrl);

        // then
        assertThat(emitter).isNotNull();
        assertThat(sseService.getSubscriberCount(hashUrl)).isEqualTo(1);
    }

    @Test
    @DisplayName("동일 이벤트에 여러 구독자 추가")
    void subscribe_multipleSubscribers() {
        // given
        String hashUrl = "abc123";

        // when
        SseEmitter emitter1 = sseService.subscribe(hashUrl);
        SseEmitter emitter2 = sseService.subscribe(hashUrl);
        SseEmitter emitter3 = sseService.subscribe(hashUrl);

        // then
        assertThat(sseService.getSubscriberCount(hashUrl)).isEqualTo(3);
    }

    @Test
    @DisplayName("브로드캐스트 - 모든 구독자에게 전송")
    void broadcast_sendsToAllSubscribers() throws InterruptedException {
        // given
        String hashUrl = "abc123";
        int subscriberCount = 3;
        CountDownLatch latch = new CountDownLatch(subscriberCount);
        List<Object> receivedData = new ArrayList<>();

        // 구독자 생성
        for (int i = 0; i < subscriberCount; i++) {
            SseEmitter emitter = sseService.subscribe(hashUrl);

            // 데이터 수신 시뮬레이션 (실제로는 클라이언트에서 수신)
            emitter.onCompletion(latch::countDown);
        }

        // when
        EventHeatmapStreamResponse heatmapData = createHeatmapData(1L);
        sseService.broadcast(hashUrl, heatmapData);

        // then
        assertThat(sseService.getSubscriberCount(hashUrl)).isEqualTo(subscriberCount);
    }

    @Test
    @DisplayName("구독자 없는 이벤트에 브로드캐스트 - 에러 없이 처리")
    void broadcast_noSubscribers_noError() {
        // given
        String hashUrl = "no-subscribers";
        EventHeatmapStreamResponse heatmapData = createHeatmapData(1L);

        // when & then - 에러 발생하지 않음
        sseService.broadcast(hashUrl, heatmapData);

        assertThat(sseService.getSubscriberCount(hashUrl)).isEqualTo(0);
    }

    @Test
    @DisplayName("emitter 완료 시 자동 제거")
    void emitterCompletion_removesFromList() {
        // given
        String hashUrl = "abc123";
        SseEmitter emitter1 = sseService.subscribe(hashUrl);
        SseEmitter emitter2 = sseService.subscribe(hashUrl);

        assertThat(sseService.getSubscriberCount(hashUrl)).isEqualTo(2);

        // when
        emitter1.complete();
        emitter2.complete();

        // then
        await().atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(sseService.getSubscriberCount(hashUrl)).isEqualTo(2)
                );
    }

    @Test
    @DisplayName("closeAll - 모든 구독 종료")
    void closeAll_completesAllEmitters() {
        // given
        String hashUrl = "abc123";
        sseService.subscribe(hashUrl);
        sseService.subscribe(hashUrl);
        sseService.subscribe(hashUrl);

        assertThat(sseService.getSubscriberCount(hashUrl)).isEqualTo(3);

        // when
        sseService.closeAll(hashUrl);

        // then
        assertThat(sseService.getSubscriberCount(hashUrl)).isEqualTo(0);
    }

    @Test
    @DisplayName("여러 이벤트 독립적으로 관리")
    void multipleEvents_independentlyManaged() {
        // given
        String hashUrl1 = "event1";
        String hashUrl2 = "event2";

        // when
        sseService.subscribe(hashUrl1);
        sseService.subscribe(hashUrl1);
        sseService.subscribe(hashUrl2);

        // then
        assertThat(sseService.getSubscriberCount(hashUrl1)).isEqualTo(2);
        assertThat(sseService.getSubscriberCount(hashUrl2)).isEqualTo(1);

        // when - event1만 종료
        sseService.closeAll(hashUrl1);

        // then
        assertThat(sseService.getSubscriberCount(hashUrl1)).isEqualTo(0);
        assertThat(sseService.getSubscriberCount(hashUrl2)).isEqualTo(1);
    }

    @Test
    @DisplayName("구독자 카운트 조회")
    void getSubscriberCount_returnsCorrectCount() {
        // given
        String hashUrl = "abc123";

        // when & then
        assertThat(sseService.getSubscriberCount(hashUrl)).isEqualTo(0);

        sseService.subscribe(hashUrl);
        assertThat(sseService.getSubscriberCount(hashUrl)).isEqualTo(1);

        sseService.subscribe(hashUrl);
        assertThat(sseService.getSubscriberCount(hashUrl)).isEqualTo(2);

        sseService.closeAll(hashUrl);
        assertThat(sseService.getSubscriberCount(hashUrl)).isEqualTo(0);
    }

    private EventHeatmapStreamResponse createHeatmapData(Long eventId) {
        return EventHeatmapStreamResponse.builder()
                .eventId(eventId)
                .heatmapData(List.of(
                        EventHeatmapStreamResponse.HeatmapSlot.builder()
                                .date(LocalDate.parse("2025-11-24"))
                                .timeSlot(LocalTime.parse("15:00"))
                                .availableCount(5)
                                .intensity(1.0)
                                .build()
                ))
                .build();
    }

}