package eusyaeusya.gmg.domain.event.service;

import eusyaeusya.gmg.api.event.response.EventHeatmapStreamResponse;
import eusyaeusya.gmg.api.event.response.EventMainResponse;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.participant.entity.Participant;
import eusyaeusya.gmg.domain.participant.entity.ParticipantStatus;
import eusyaeusya.gmg.domain.participant.entity.ParticipantUnavailableTime;
import eusyaeusya.gmg.domain.participant.repository.ParticipantRepository;
import eusyaeusya.gmg.domain.participant.repository.ParticipantUnavailableTimeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class HeatmapServiceTest {

    @InjectMocks
    private HeatmapService heatmapService;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private ParticipantUnavailableTimeRepository unavailableTimeRepository;

    @Test
    @DisplayName("참여자가 없으면 빈 히트맵 반환")
    void calculateHeatmap_noParticipants_returnsEmpty() {
        // given
        Event event = createMockEvent();
                given(participantRepository.countByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED)).willReturn(0);

        // when
        List<EventMainResponse.HeatmapSlot> result = heatmapService.calculateHeatmap(event);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("불가능한 시간이 없으면 모든 슬롯이 intensity=1.0")
    void calculateHeatmap_noUnavailableTimes_allSlotsFullIntensity() {
        // given
        Event event = createMockEvent();
                given(participantRepository.countByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED)).willReturn(5);
        given(unavailableTimeRepository.findAllByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED))
                .willReturn(Collections.emptyList());

        // when
        List<EventMainResponse.HeatmapSlot> result = heatmapService.calculateHeatmap(event);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(slot ->
                slot.availableCount() == 5 && slot.intensity() == 1.0
        );
    }

    @Test
    @DisplayName("일부 참여자가 불가능한 시간 - 정확한 가능 인원 계산")
    void calculateHeatmap_someUnavailable_correctAvailableCount() {
        // given
        Event event = createMockEvent();
        int totalParticipants = 5;

        given(participantRepository.countByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED))
                .willReturn(totalParticipants);

        // 3명이 11/24 15:00-16:00 불가능
        List<ParticipantUnavailableTime> unavailableTimes = List.of(
                createUnavailableTime(event, 1L, "2025-11-24", "15:00", "16:00"),
                createUnavailableTime(event, 2L, "2025-11-24", "15:00", "16:00"),
                createUnavailableTime(event, 3L, "2025-11-24", "15:00", "16:00")
        );

        given(unavailableTimeRepository.findAllByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED))
                .willReturn(unavailableTimes);

        // when
        List<EventMainResponse.HeatmapSlot> result = heatmapService.calculateHeatmap(event);

        // then
        // 15:00 슬롯은 2명 가능 (5 - 3)
        EventMainResponse.HeatmapSlot slot1500 = result.stream()
                .filter(slot -> slot.date().equals(LocalDate.parse("2025-11-24"))
                        && slot.timeSlot().equals(LocalTime.parse("15:00")))
                .findFirst()
                .orElseThrow();

        assertThat(slot1500.availableCount()).isEqualTo(2);
        assertThat(slot1500.intensity()).isLessThan(1.0);

        // 15:30도 불가능 범위에 포함 (15:00-16:00)
        EventMainResponse.HeatmapSlot slot1530 = result.stream()
                .filter(slot -> slot.date().equals(LocalDate.parse("2025-11-24"))
                        && slot.timeSlot().equals(LocalTime.parse("15:30")))
                .findFirst()
                .orElseThrow();

        assertThat(slot1530.availableCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("중복 참여자는 한 번만 카운트")
    void calculateHeatmap_duplicateParticipant_countsOnce() {
        // given
        Event event = createMockEvent();
                given(participantRepository.countByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED)).willReturn(3);

        // 동일 참여자가 같은 시간대에 여러 번 등록 (실제로는 발생하지 않지만 방어)
        List<ParticipantUnavailableTime> unavailableTimes = List.of(
                createUnavailableTime(event, 1L, "2025-11-24", "15:00", "16:00"),
                createUnavailableTime(event, 1L, "2025-11-24", "15:00", "15:30") // 중복
        );

        given(unavailableTimeRepository.findAllByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED))
                .willReturn(unavailableTimes);

        // when
        List<EventMainResponse.HeatmapSlot> result = heatmapService.calculateHeatmap(event);

        // then
        EventMainResponse.HeatmapSlot slot1500 = result.stream()
                .filter(slot -> slot.date().equals(LocalDate.parse("2025-11-24"))
                        && slot.timeSlot().equals(LocalTime.parse("15:00")))
                .findFirst()
                .orElseThrow();

        // 중복 제거되어 1명만 불가능 → 2명 가능
        assertThat(slot1500.availableCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("intensity 계산 - 로그 함수 기반")
    void calculateHeatmap_intensityCalculation() {
        // given
        Event event = createMockEvent();
        int totalParticipants = 10;

        given(participantRepository.countByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED))
                .willReturn(totalParticipants);

        // 5명이 불가능 → 5명 가능
        List<ParticipantUnavailableTime> unavailableTimes = List.of(
                createUnavailableTime(event, 1L, "2025-11-24", "15:00", "16:00"),
                createUnavailableTime(event, 2L, "2025-11-24", "15:00", "16:00"),
                createUnavailableTime(event, 3L, "2025-11-24", "15:00", "16:00"),
                createUnavailableTime(event, 4L, "2025-11-24", "15:00", "16:00"),
                createUnavailableTime(event, 5L, "2025-11-24", "15:00", "16:00")
        );

        given(unavailableTimeRepository.findAllByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED))
                .willReturn(unavailableTimes);

        // when
        List<EventMainResponse.HeatmapSlot> result = heatmapService.calculateHeatmap(event);

        // then
        EventMainResponse.HeatmapSlot slot1500 = result.stream()
                .filter(slot -> slot.date().equals(LocalDate.parse("2025-11-24"))
                        && slot.timeSlot().equals(LocalTime.parse("15:00")))
                .findFirst()
                .orElseThrow();

        // intensity = log(5+1) / log(10+1) ≈ 0.75
        assertThat(slot1500.intensity()).isBetween(0.74, 0.76);
    }

    @Test
    @DisplayName("calculateHeatmapForStream - EventHeatmapStreamResponse 형식 변환")
    void calculateHeatmapForStream_convertsToStreamResponse() {
        // given
        Event event = createMockEvent();
                given(participantRepository.countByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED)).willReturn(3);
        given(unavailableTimeRepository.findAllByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED))
                .willReturn(Collections.emptyList());

        // when
        EventHeatmapStreamResponse result = heatmapService.calculateHeatmapForStream(event);

        // then
        assertThat(result.eventId()).isEqualTo(event.getId());
        assertThat(result.heatmapData()).isNotEmpty();
    }

    @Test
    @DisplayName("선택되지 않은 gap day는 히트맵 슬롯에 포함되지 않는다")
    void calculateHeatmap_skipsGapDays() {
        Event event = createMockEvent(List.of(
                LocalDate.parse("2025-11-21"),
                LocalDate.parse("2025-11-22"),
                LocalDate.parse("2025-11-28"),
                LocalDate.parse("2025-11-29")
        ));
        given(participantRepository.countByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED)).willReturn(2);
        given(unavailableTimeRepository.findAllByEventIdAndStatus(event.getId(), ParticipantStatus.COMPLETED))
                .willReturn(Collections.emptyList());

        List<EventMainResponse.HeatmapSlot> result = heatmapService.calculateHeatmap(event);

        assertThat(result).extracting(EventMainResponse.HeatmapSlot::date)
                .doesNotContain(LocalDate.parse("2025-11-23"));
    }

    private Event createMockEvent() {
        return createMockEvent(List.of(LocalDate.parse("2025-11-24")));
    }

    private Event createMockEvent(List<LocalDate> selectedDates) {
        LocalTime startTime = LocalTime.parse("15:00");
        LocalTime endTime = LocalTime.parse("17:00");

        Event event = mock(Event.class);

        // lenient()를 사용하여 여러 번 호출되어도 동일한 값 반환
        lenient().when(event.getId()).thenReturn(1L);
        lenient().when(event.getSelectedDates()).thenReturn(selectedDates);
        lenient().when(event.getTimeStart()).thenReturn(startTime);
        lenient().when(event.getTimeEnd()).thenReturn(endTime);

        return event;
    }

    private ParticipantUnavailableTime createUnavailableTime(
            Event event,
            Long participantId,
            String date,
            String startTime,
            String endTime) {

        Participant participant = mock(Participant.class);
        given(participant.getId()).willReturn(participantId);

        LocalDate localDate = LocalDate.parse(date);
        LocalTime localStartTime = LocalTime.parse(startTime);
        LocalTime localEndTime = LocalTime.parse(endTime);

        ParticipantUnavailableTime unavailableTime = mock(ParticipantUnavailableTime.class);
        given(unavailableTime.getParticipant()).willReturn(participant);
        given(unavailableTime.getUnavailableDate()).willReturn(localDate);
        given(unavailableTime.getUnavailableTimeStart()).willReturn(localStartTime);
        given(unavailableTime.getUnavailableTimeEnd()).willReturn(localEndTime);

        return unavailableTime;
    }
}
