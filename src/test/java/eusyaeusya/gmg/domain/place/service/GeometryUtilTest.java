package eusyaeusya.gmg.domain.place.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GeometryUtilTest {

    private static final double METERS_PER_DEGREE_LAT = 111319.9;

    @Test
    @DisplayName("좌표 기준 반경의 BoundingBox를 계산한다")
    void success_calculateBoundingBoxTest() {
        // given
        double centerLat = 37.5665;
        double centerLng = 126.9780;
        int radiusMeters = 500;

        // when
        GeometryUtil.BoundingBox result = GeometryUtil.calculateBoundingBox(centerLat, centerLng, radiusMeters);

        // then
        assertThat(result.minLat()).isLessThan(centerLat);
        assertThat(result.maxLat()).isGreaterThan(centerLat);
        assertThat(result.minLng()).isLessThan(centerLng);
        assertThat(result.maxLng()).isGreaterThan(centerLng);

        //위도 변화량 검증
        double expectedDeltaLat = radiusMeters / METERS_PER_DEGREE_LAT;
        assertThat(result.maxLat() - centerLat).isCloseTo(expectedDeltaLat, within(0.0000001));
        assertThat(centerLat - result.minLat()).isCloseTo(expectedDeltaLat, within(0.0000001));

        //경도 변화량 검증
        double metersPerDegreeLng = METERS_PER_DEGREE_LAT * Math.cos(Math.toRadians(centerLat));
        double expectedDeltaLng = radiusMeters / metersPerDegreeLng;
        assertThat(result.maxLng() - centerLng).isCloseTo(expectedDeltaLng, within(0.0000001));
        assertThat(centerLng - result.minLng()).isCloseTo(expectedDeltaLng, within(0.0000001));
    }
}