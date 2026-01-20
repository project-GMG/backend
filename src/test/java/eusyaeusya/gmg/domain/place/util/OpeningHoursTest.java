package eusyaeusya.gmg.domain.place.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class OpeningHoursTest {

    @Test
    @DisplayName("사용자가 제공한 형식의 JSON이 정상적으로 파싱되고 영업 시간을 반환한다")
    void parseUserJsonFormat() {
        // given
        String json = "{\"thu\":\"11:00-23:00\",\"tue\":\"11:00-23:00\",\"wed\":\"11:00-23:00\",\"sat\":\"11:00-23:00\",\"fri\":\"11:00-23:00\",\"mon\":\"11:00-23:00\"}";
        OpeningHours openingHours = new OpeningHours(json);

        // when & then
        // 월요일(mon) 확인
        OpeningHours.TimeRange monRange = openingHours.getTimeRange(DayOfWeek.MONDAY);
        assertThat(monRange).isNotNull();
        assertThat(monRange.start()).isEqualTo(LocalTime.of(11, 0));
        assertThat(monRange.end()).isEqualTo(LocalTime.of(23, 0));

        // 목요일(thu) 확인
        OpeningHours.TimeRange thuRange = openingHours.getTimeRange(DayOfWeek.THURSDAY);
        assertThat(thuRange).isNotNull();
        assertThat(thuRange.start()).isEqualTo(LocalTime.of(11, 0));
        assertThat(thuRange.end()).isEqualTo(LocalTime.of(23, 0));

        // 데이터에 없는 일요일(sun) 확인
        OpeningHours.TimeRange sunRange = openingHours.getTimeRange(DayOfWeek.SUNDAY);
        assertThat(sunRange).isNull();
    }

    @Test
    @DisplayName("특정 시간대에 영업 중인지 확인하는 로직이 정상 작동한다")
    void isOpenDuring() {
        // given
        String json = "{\"mon\":\"11:00-23:00\"}";
        OpeningHours openingHours = new OpeningHours(json);

        // when & then
        // 영업 시간 내 (12:00-14:00)
        assertThat(openingHours.isOpenDuring(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(14, 0))).isTrue();

        // 영업 시작/종료 시간 정시 (11:00-23:00)
        assertThat(openingHours.isOpenDuring(DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(23, 0))).isTrue();

        // 영업 시간 이전 (10:00-12:00) -> false (11시부터 시작이므로)
        assertThat(openingHours.isOpenDuring(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0))).isFalse();

        // 영업 종료 이후 (22:00-23:30) -> false (23시 종료이므로)
        assertThat(openingHours.isOpenDuring(DayOfWeek.MONDAY, LocalTime.of(22, 0), LocalTime.of(23, 30))).isFalse();

        // 휴무일 (SUNDAY)
        assertThat(openingHours.isOpenDuring(DayOfWeek.SUNDAY, LocalTime.of(12, 0), LocalTime.of(14, 0))).isFalse();
    }

    @Test
    @DisplayName("자정을 넘기는 영업 시간(예: 18:00-02:00)에 대한 체크가 정상 작동한다")
    void isOpenDuring_Overnight() {
        // given
        String json = "{\"mon\":\"18:00-02:00\"}";
        OpeningHours openingHours = new OpeningHours(json);

        // when & then
        // 당일 밤 (22:00-23:00) -> true
        assertThat(openingHours.isOpenDuring(DayOfWeek.MONDAY, LocalTime.of(22, 0), LocalTime.of(23, 0))).isTrue();

        // 다음날 새벽 (01:00-01:30) -> true
        assertThat(openingHours.isOpenDuring(DayOfWeek.MONDAY, LocalTime.of(1, 0), LocalTime.of(1, 30))).isTrue();

        // 영업 시간 외 (17:00-19:00) -> false (18시 시작이므로)
        assertThat(openingHours.isOpenDuring(DayOfWeek.MONDAY, LocalTime.of(17, 0), LocalTime.of(19, 0))).isFalse();

        // 영업 종료 이후 (01:30-02:30) -> false (02시 종료이므로)
        assertThat(openingHours.isOpenDuring(DayOfWeek.MONDAY, LocalTime.of(1, 30), LocalTime.of(2, 30))).isFalse();
    }
}
