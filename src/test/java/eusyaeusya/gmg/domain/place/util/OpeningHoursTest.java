package eusyaeusya.gmg.domain.place.util;

import eusyaeusya.gmg.infra.google.dto.GooglePlaceDetailsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpeningHoursTest {

    @Test
    @DisplayName("구글 영업시간 데이터를 JSON 문자열로 변환한다")
    void shouldConvertGooglePeriodsToJson() {
        // given
        List<GooglePlaceDetailsResponse.Period> periods = List.of(
                new GooglePlaceDetailsResponse.Period(
                        new GooglePlaceDetailsResponse.TimeInfo(1, 10, 0),
                        new GooglePlaceDetailsResponse.TimeInfo(1, 23, 0)
                ),
                new GooglePlaceDetailsResponse.Period(
                        new GooglePlaceDetailsResponse.TimeInfo(2, 10, 0),
                        new GooglePlaceDetailsResponse.TimeInfo(2, 23, 0)
                )
        );

        // when
        String json = OpeningHours.fromGooglePeriods(periods);

        // then
        System.out.println("Generated JSON: " + json);
        assertThat(json).contains("\"mon\":\"10:00-23:00\"");
        assertThat(json).contains("\"tue\":\"10:00-23:00\"");
        assertThat(json).doesNotContain("\\\""); // Escape 문자가 없는지 확인
    }
}
