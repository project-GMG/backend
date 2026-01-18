package eusyaeusya.gmg.domain.place.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eusyaeusya.gmg.infra.google.dto.GooglePlaceDetailsResponse;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpeningHours {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();
    private final Map<String, String> hours;

    public OpeningHours(String jsonString) {
        this.hours = parseJson(jsonString);
    }

    public static String fromGooglePeriods(List<GooglePlaceDetailsResponse.Period> periods) {
        if (periods == null || periods.isEmpty()) {
            return "{}";
        }

        Map<String, String> hoursMap = new HashMap<>();
        for (GooglePlaceDetailsResponse.Period period : periods) {
            if (period.open() == null || period.close() == null) continue;

            String dayKey = convertGoogleDayToKey(period.open().day());
            String timeRange = formatGoogleTime(period.open()) + "-" + formatGoogleTime(period.close());
            hoursMap.put(dayKey, timeRange);
        }

        try {
            return objectMapper.writeValueAsString(hoursMap);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private static String convertGoogleDayToKey(int googleDay) {
        return switch (googleDay) {
            case 0 -> "sun";
            case 1 -> "mon";
            case 2 -> "tue";
            case 3 -> "wed";
            case 4 -> "thu";
            case 5 -> "fri";
            case 6 -> "sat";
            default -> "unknown";
        };
    }

    private static String formatGoogleTime(GooglePlaceDetailsResponse.TimeInfo timeInfo) {
        if (timeInfo == null || timeInfo.hour() == null || timeInfo.minute() == null) {
            return "00:00";
        }
        return String.format("%02d:%02d", timeInfo.hour(), timeInfo.minute());
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseJson(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, HashMap.class);
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    /**
     * 특정 요일의 영업 시간을 반환
     */
    public TimeRange getTimeRange(DayOfWeek dayOfWeek) {
        String dayKey = convertDayOfWeekToKey(dayOfWeek);
        String timeStr = hours.get(dayKey);

        if (timeStr == null || timeStr.isEmpty()) {
            return null; // 영업하지 않음
        }

        return TimeRange.parse(timeStr);
    }

    private String convertDayOfWeekToKey(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "mon";
            case TUESDAY -> "tue";
            case WEDNESDAY -> "wed";
            case THURSDAY -> "thu";
            case FRIDAY -> "fri";
            case SATURDAY -> "sat";
            case SUNDAY -> "sun";
        };
    }

    /**
     * 특정 요일과 시간대에 영업 중인지 확인
     */
    public boolean isOpenDuring(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        TimeRange dayHours = getTimeRange(dayOfWeek);
        if (dayHours == null) {
            return false;
        }

        LocalTime open = dayHours.start();
        LocalTime close = dayHours.end();

        if (open.isBefore(close)) {
            // 일반적인 영업 시간 (예: 09:00 - 22:00)
            return !open.isAfter(startTime) && !close.isBefore(endTime);
        } else {
            // 자정을 넘기는 영업 시간 (예: 18:00 - 02:00)
            // startTime-endTime이 (open ~ 23:59) 또는 (00:00 ~ close)에 완전히 포함되어야 함
            return !open.isAfter(startTime) || !close.isBefore(endTime);
        }
    }

    public record TimeRange(LocalTime start, LocalTime end) {

        public static TimeRange parse(String timeStr) {
            // "11:00-23:00" 형식 파싱
            String[] parts = timeStr.split("-");
            if (parts.length != 2) {
                return null;
            }

            try {
                LocalTime start = LocalTime.parse(parts[0].trim());
                LocalTime end = LocalTime.parse(parts[1].trim());
                return new TimeRange(start, end);
            } catch (Exception e) {
                return null;
            }
        }

    }
}
