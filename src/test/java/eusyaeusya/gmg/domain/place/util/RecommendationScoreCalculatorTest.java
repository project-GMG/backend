package eusyaeusya.gmg.domain.place.util;

import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.place.entity.Place;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class RecommendationScoreCalculatorTest {

    @Test
    @DisplayName("가중 matching rate 계산은 선택 날짜만 분모로 사용한다")
    void calculateWeightedMatchingRate_ignoresGapDays() {
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
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        Place place = mock(Place.class);
        given(place.getOpeningHours()).willReturn(new OpeningHours("""
                {"mon":"10:00-11:00","tue":"10:00-11:00","wed":"10:00-11:00","thu":"10:00-11:00","fri":"10:00-11:00","sat":"10:00-11:00","sun":"10:00-11:00"}
                """));

        Map<LocalDate, Map<LocalTime, Double>> intensityMap = new LinkedHashMap<>();
        for (LocalDate date : selectedDates) {
            Map<LocalTime, Double> slots = new LinkedHashMap<>();
            slots.put(LocalTime.of(10, 0), 1.0);
            slots.put(LocalTime.of(10, 30), 1.0);
            intensityMap.put(date, slots);
        }

        double weightedRate = RecommendationScoreCalculator.calculateWeightedMatchingRate(
                place,
                event,
                intensityMap
        );

        assertThat(weightedRate).isEqualTo(1.0);
    }
}
