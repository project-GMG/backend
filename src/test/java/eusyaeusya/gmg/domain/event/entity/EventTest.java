package eusyaeusya.gmg.domain.event.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {

    @Test
    @DisplayName("비연속 선택 날짜를 그대로 보존하고 gap day는 포함하지 않는다")
    void create_preservesSelectedDates() {
        List<LocalDate> selectedDates = List.of(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(8),
                LocalDate.now().plusDays(9)
        );

        Event event = Event.create(
                "다같이 만나요",
                new BigDecimal("35.8468"),
                new BigDecimal("127.1296"),
                "전북대학교",
                selectedDates,
                LocalTime.of(13, 0),
                LocalTime.of(18, 0)
        );

        assertThat(event.getSelectedDates()).containsExactlyElementsOf(selectedDates);
        assertThat(event.containsDate(selectedDates.getFirst())).isTrue();
        assertThat(event.containsDate(selectedDates.get(1).plusDays(1))).isFalse();
        assertThat(event.getTotalDays()).isEqualTo(4);
    }

    @Test
    @DisplayName("countDaysMatching은 선택 날짜만 기준으로 계산한다")
    void countDaysMatching_countsOnlySelectedDates() {
        List<LocalDate> selectedDates = List.of(
                LocalDate.of(2026, 3, 13),
                LocalDate.of(2026, 3, 14),
                LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 3, 21)
        );

        Event event = Event.create(
                "다같이 만나요",
                new BigDecimal("35.8468"),
                new BigDecimal("127.1296"),
                "전북대학교",
                selectedDates,
                LocalTime.of(13, 0),
                LocalTime.of(18, 0)
        );

        int matchingDays = event.countDaysMatching((date, start, end) -> date.getDayOfWeek().getValue() == 6);

        assertThat(matchingDays).isEqualTo(2);
    }
}
