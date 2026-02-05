package eusyaeusya.gmg.domain.event;

import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventScheduler {

    private final EventRepository eventRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    @Transactional
    public void expireEvents() {
        log.info("만료된 모임 비활성화 스케줄러 실행");

        LocalDate threshold = LocalDate.now().minusDays(Event.EXPIRATION_DAYS);
        List<Event> expiredEvents = eventRepository.findByStatusNotExpiredAndDateStartBefore(threshold);

        if (expiredEvents.isEmpty()) {
            log.info("만료 처리할 모임이 없습니다.");
            return;
        }

        for (Event event : expiredEvents) {
            event.expire();
            log.info("모임 만료 처리: id={}, hashUrl={}, dateStart={}",
                    event.getId(), event.getHashUrl(), event.getDateStart());
        }

        log.info("총 {}개의 모임이 만료 처리되었습니다.", expiredEvents.size());
    }
}
