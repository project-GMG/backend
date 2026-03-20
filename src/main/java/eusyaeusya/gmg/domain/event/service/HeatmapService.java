package eusyaeusya.gmg.domain.event.service;

import eusyaeusya.gmg.api.event.response.EventHeatmapStreamResponse;
import eusyaeusya.gmg.api.event.response.EventMainResponse;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.participant.entity.ParticipantStatus;
import eusyaeusya.gmg.domain.participant.entity.ParticipantUnavailableTime;
import eusyaeusya.gmg.domain.participant.repository.ParticipantRepository;
import eusyaeusya.gmg.domain.participant.repository.ParticipantUnavailableTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeatmapService {

    private static final int TIME_SLOT_INTERVAL_MINUTES = 30;

    private final ParticipantRepository participantRepository;
    private final ParticipantUnavailableTimeRepository unavailableTimeRepository;

    public List<EventMainResponse.HeatmapSlot> calculateHeatmap(Event event) {
        log.info("히트맵 계산 시작: eventId={}", event.getId());

        int totalParticipants = participantRepository.countByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED);


        if (totalParticipants == 0) {
            log.info("참여자가 없어 빈 히트맵 반환: eventId={}", event.getId());
            return Collections.emptyList();
        }

        List<ParticipantUnavailableTime> unavailableTimes =
                unavailableTimeRepository.findAllByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED);

        List<TimeSlotKey> allSlots = generateAllTimeSlots(event);

        Map<TimeSlotKey, Integer> unavailableCountMap = countUnavailableBySlot(unavailableTimes);

        List<EventMainResponse.HeatmapSlot> heatmapData = allSlots.stream()
                .map(slot -> {
                    int unavailableCount = unavailableCountMap.getOrDefault(slot, 0);
                    int availableCount = totalParticipants - unavailableCount;
                    double intensity = calculateIntensity(availableCount, totalParticipants);

                    return EventMainResponse.HeatmapSlot.builder()
                            .date(slot.date())
                            .timeSlot(slot.time())
                            .availableCount(availableCount)
                            .intensity(intensity)
                            .build();
                })
                .collect(Collectors.toList());

        log.info("히트맵 계산 완료: eventId={}, totalSlots={}, totalParticipants={}",
                event.getId(), heatmapData.size(), totalParticipants);

        return heatmapData;
    }

    public Map<LocalDate, Map<LocalTime, Double>> calculateIntensityMap(Event event) {
        int totalParticipants = participantRepository.countByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED);

        if (totalParticipants == 0) {
            return Collections.emptyMap();
        }

        List<ParticipantUnavailableTime> unavailableTimes =
                unavailableTimeRepository.findAllByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED);

        List<TimeSlotKey> allSlots = generateAllTimeSlots(event);
        Map<TimeSlotKey, Integer> unavailableCountMap = countUnavailableBySlot(unavailableTimes);

        Map<LocalDate, Map<LocalTime, Double>> intensityMap = new LinkedHashMap<>();
        for (TimeSlotKey slot : allSlots) {
            int unavailableCount = unavailableCountMap.getOrDefault(slot, 0);
            int availableCount = totalParticipants - unavailableCount;
            double intensity = calculateIntensity(availableCount, totalParticipants);

            intensityMap
                    .computeIfAbsent(slot.date(), k -> new LinkedHashMap<>())
                    .put(slot.time(), intensity);
        }

        return intensityMap;
    }

    public EventHeatmapStreamResponse calculateHeatmapForStream(Event event) {
        List<EventMainResponse.HeatmapSlot> heatmapSlots = calculateHeatmap(event);

        List<EventHeatmapStreamResponse.HeatmapSlot> streamSlots = heatmapSlots.stream()
                .map(slot -> EventHeatmapStreamResponse.HeatmapSlot.builder()
                        .date(slot.date())
                        .timeSlot(slot.timeSlot())
                        .availableCount(slot.availableCount())
                        .intensity(slot.intensity())
                        .build())
                .collect(Collectors.toList());

        return EventHeatmapStreamResponse.builder()
                .eventId(event.getId())
                .heatmapData(streamSlots)
                .build();
    }

    private List<TimeSlotKey> generateAllTimeSlots(Event event) {
        List<TimeSlotKey> slots = new ArrayList<>();

        for (LocalDate currentDate : event.getSelectedDates()) {
            LocalTime currentTime = event.getTimeStart();

            while (currentTime.isBefore(event.getTimeEnd())) {
                slots.add(new TimeSlotKey(currentDate, currentTime));
                currentTime = currentTime.plusMinutes(TIME_SLOT_INTERVAL_MINUTES);
            }
        }

        return slots;
    }

    private Map<TimeSlotKey, Integer> countUnavailableBySlot(
            List<ParticipantUnavailableTime> unavailableTimes) {

        Map<TimeSlotKey, Set<Long>> unavailableParticipantsBySlot = new HashMap<>();

        for (ParticipantUnavailableTime unavailable : unavailableTimes) {
            LocalDate date = unavailable.getUnavailableDate();
            LocalTime startTime = unavailable.getUnavailableTimeStart();
            LocalTime endTime = unavailable.getUnavailableTimeEnd();
            Long participantId = unavailable.getParticipant().getId();

            // 불가능한 시간 범위 내의 모든 30분 슬롯을 찾아서 마킹
            LocalTime currentTime = startTime;
            while (currentTime.isBefore(endTime)) {
                TimeSlotKey key = new TimeSlotKey(date, currentTime);
                unavailableParticipantsBySlot
                        .computeIfAbsent(key, k -> new HashSet<>())
                        .add(participantId);

                currentTime = currentTime.plusMinutes(TIME_SLOT_INTERVAL_MINUTES);
            }
        }

        // Set 크기를 Integer로 변환
        return unavailableParticipantsBySlot.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().size()
                ));
    }


    private double calculateIntensity(int availableCount, int totalParticipants) {
        if (totalParticipants == 0) {
            return 0.0;
        }

        double intensity = Math.log(availableCount + 1) / Math.log(totalParticipants + 1);

        // 소수점 둘째자리까지 반올림
        return Math.round(intensity * 100.0) / 100.0;
    }


    private record TimeSlotKey(LocalDate date, LocalTime time) {
    }
}
