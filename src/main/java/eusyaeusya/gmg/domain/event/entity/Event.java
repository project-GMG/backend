package eusyaeusya.gmg.domain.event.entity;

import eusyaeusya.gmg.api.event.response.EventErrorCode;
import eusyaeusya.gmg.common.api.exception.BadRequestException;
import eusyaeusya.gmg.common.audit.entity.BaseTimeEntity;
import eusyaeusya.gmg.domain.event.util.EventHashUrlGenerator;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "events",
        indexes = {
                @Index(name = "idx_hash_url", columnList = "hash_url"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_created_at", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseTimeEntity {

    public static final int EXPIRATION_DAYS = 7;
    private static final int MAX_SELECTED_DATES = 35;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hash_url", nullable = false, unique = true, length = 64)
    private String hashUrl;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "center_latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal centerLatitude;

    @Column(name = "center_longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal centerLongitude;

    @Column(name = "location_name", length = 255)
    private String locationName;

    @Column(name = "date_start", nullable = false)
    private LocalDate dateStart;

    @Column(name = "date_end", nullable = false)
    private LocalDate dateEnd;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "event_available_dates",
            joinColumns = @JoinColumn(name = "event_id")
    )
    @OrderColumn(name = "sort_order")
    @Column(name = "available_date", nullable = false)
    private List<LocalDate> selectedDates = new ArrayList<>();

    @Column(name = "time_start", nullable = false)
    private LocalTime timeStart;

    @Column(name = "time_end", nullable = false)
    private LocalTime timeEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaceSearchStatus placeSearchStatus;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;

    private Event(
            String hashUrl,
            String title,
            BigDecimal centerLatitude,
            BigDecimal centerLongitude,
            String locationName,
            List<LocalDate> selectedDates,
            LocalTime timeStart,
            LocalTime timeEnd,
            EventStatus status,
            PlaceSearchStatus placeSearchStatus,
            String timezone
    ) {
        List<LocalDate> normalizedSelectedDates = normalizeSelectedDates(selectedDates);
        validateTimeRange(timeStart, timeEnd);

        this.hashUrl = hashUrl;
        this.title = title;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.locationName = locationName;
        this.selectedDates = new ArrayList<>(normalizedSelectedDates);
        this.dateStart = normalizedSelectedDates.getFirst();
        this.dateEnd = normalizedSelectedDates.getLast();
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.status = status != null ? status : EventStatus.OPEN;
        this.placeSearchStatus = placeSearchStatus != null ? placeSearchStatus : PlaceSearchStatus.PENDING;
        this.timezone = timezone != null ? timezone : "Asia/Seoul";
    }

    public static Event create(
            String title,
            BigDecimal centerLatitude,
            BigDecimal centerLongitude,
            String locationName,
            List<LocalDate> selectedDates,
            LocalTime timeStart,
            LocalTime timeEnd
    ) {
        return new Event(
                EventHashUrlGenerator.generate(),
                title,
                centerLatitude,
                centerLongitude,
                locationName,
                selectedDates,
                timeStart,
                timeEnd,
                EventStatus.OPEN,
                PlaceSearchStatus.PENDING,
                "Asia/Seoul"
        );
    }

    private static List<LocalDate> normalizeSelectedDates(List<LocalDate> selectedDates) {
        if (selectedDates == null || selectedDates.isEmpty()) {
            throw new BadRequestException(
                    EventErrorCode.INVALID_DATE_RANGE,
                    "선택 날짜는 최소 1개 이상이어야 합니다"
            );
        }

        if (selectedDates.stream().anyMatch(Objects::isNull)) {
            throw new BadRequestException(
                    EventErrorCode.INVALID_DATE_RANGE,
                    "선택 날짜에 null이 포함될 수 없습니다"
            );
        }

        List<LocalDate> normalized = selectedDates.stream()
                .sorted()
                .toList();

        if (normalized.size() > MAX_SELECTED_DATES) {
            throw new BadRequestException(
                    EventErrorCode.DATE_RANGE_TOO_LONG,
                    EventErrorCode.DATE_RANGE_TOO_LONG.getMessage()
            );
        }

        for (int i = 1; i < normalized.size(); i++) {
            if (normalized.get(i - 1).equals(normalized.get(i))) {
                throw new BadRequestException(
                        EventErrorCode.INVALID_DATE_RANGE,
                        "선택 날짜는 중복될 수 없습니다"
                );
            }
        }

        return normalized;
    }

    private static void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new BadRequestException(
                    EventErrorCode.INVALID_TIME_RANGE,
                    "시작 시간은 종료 시간보다 이전이어야 합니다"
            );
        }
    }

    public int getTotalDays() {
        return selectedDates.size();
    }

    public boolean containsDate(LocalDate date) {
        return selectedDates.contains(date);
    }

    public LocalDate getFirstSelectedDate() {
        return selectedDates.isEmpty() ? null : selectedDates.getFirst();
    }

    public LocalDate getLastSelectedDate() {
        return selectedDates.isEmpty() ? null : selectedDates.getLast();
    }

    public boolean isClosed() {
        return status == EventStatus.CLOSED;
    }

    public int countDaysMatching(DateCondition condition) {
        int count = 0;
        for (LocalDate selectedDate : selectedDates) {
            if (condition.test(selectedDate, timeStart, timeEnd)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public void completePlaceSearch() {
        this.placeSearchStatus = PlaceSearchStatus.COMPLETED;
    }

    public void failPlaceSearch() {
        this.placeSearchStatus = PlaceSearchStatus.FAILED;
    }

    public boolean isExpired() {
        if (status == EventStatus.EXPIRED) {
            return true;
        }

        LocalDate lastSelectedDate = getLastSelectedDate();
        if (lastSelectedDate == null) {
            return false;
        }

        return lastSelectedDate.plusDays(EXPIRATION_DAYS).isBefore(LocalDate.now());
    }

    public void expire() {
        this.status = EventStatus.EXPIRED;
    }

    @FunctionalInterface
    public interface DateCondition {
        boolean test(LocalDate date, LocalTime timeStart, LocalTime timeEnd);
    }
}
