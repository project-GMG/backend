package eusyaeusya.gmg.domain.place.service;

public class GeometryUtil {

    private static final double METERS_PER_DEGREE_LAT = 111319.9;

    private GeometryUtil() {
    }

    public static BoundingBox calculateBoundingBox(double centerLat, double centerLng, int radiusMeters) {
        // 위도 변화량 계산
        double deltaLat = radiusMeters / METERS_PER_DEGREE_LAT;
        double minLat = centerLat - deltaLat;
        double maxLat = centerLat + deltaLat;

        // 경도 변화량 계산 (위도에 따라 길이가 달라짐)
        // 경도 1도당 길이 = 111.32km * cos(위도)
        double metersPerDegreeLng = METERS_PER_DEGREE_LAT * Math.cos(Math.toRadians(centerLat));
        double deltaLng = radiusMeters / metersPerDegreeLng;

        double minLng = centerLng - deltaLng;
        double maxLng = centerLng + deltaLng;

        return new BoundingBox(minLat, maxLat, minLng, maxLng);
    }

    public record BoundingBox(
            double minLat,
            double maxLat,
            double minLng,
            double maxLng
    ) {
        public BoundingBox {
            if (minLat > maxLat) {
                throw new IllegalArgumentException("minLat는 maxLat보다 작아야 합니다");
            }
            if (minLng > maxLng) {
                throw new IllegalArgumentException("minLng는 maxLng보다 작아야 합니다");
            }
        }
    }
}
