package eusyaeusya.gmg.config;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {
    /**
     * 장소 검색용 스레드 풀
     * <p>
     * 설정 근거:
     * - corePoolSize: 2 (동시 방 생성이 많지 않을 것으로 예상)
     * - maxPoolSize: 5 (피크 시간에도 5개 이상은 드물 것)
     * - queueCapacity: 10 (대기열, 초과 시 CallerRunsPolicy로 호출 스레드에서 실행)
     * <p>
     * Micrometer Tracing 전파를 위해 Task Decorator 사용
     * <p>
     * MVP 이후 모니터링 결과에 따라 조정 필요
     */
    @Bean(name = "placeSearchExecutor")
    public Executor placeSearchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("place-search-");
        executor.setTaskDecorator(runnable -> {
            ContextSnapshot snapshot = ContextSnapshotFactory.builder().build().captureAll();
            return () -> {
                try (ContextSnapshot.Scope scope = snapshot.setThreadLocals()) {
                    runnable.run();
                }
            };
        });
        executor.setRejectedExecutionHandler((r, e) -> {
            log.warn("장소 검색 스레드 풀 포화 상태, 호출 스레드에서 실행");
            if (!e.isShutdown()) {
                r.run();
            }
        });
        executor.initialize();

        log.info("PlaceSearch ThreadPool 초기화: core={}, max={}, queue={}",
                executor.getCorePoolSize(),
                executor.getMaxPoolSize(),
                executor.getQueueCapacity());

        return executor;
    }
}
