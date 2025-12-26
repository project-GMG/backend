package eusyaeusya.gmg.domain.place.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class OpeningHours {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, String> hours;

    public OpeningHours(String jsonString) {
        this.hours = parseJson(jsonString);
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

        // 모임 시간대가 영업시간 내에 완전히 포함되는지 확인
        return !dayHours.start().isAfter(startTime) &&
                !dayHours.end().isBefore(endTime);
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
