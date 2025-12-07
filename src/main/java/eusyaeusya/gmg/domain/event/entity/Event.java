package eusyaeusya.gmg.domain.event.entity;

import eusyaeusya.gmg.common.audit.entity.BaseTimeEntity;
import eusyaeusya.gmg.domain.event.util.EventHashUrlGenerator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

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

    @Column(name = "time_start", nullable = false)
    private LocalTime timeStart;

    @Column(name = "time_end", nullable = false)
    private LocalTime timeEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventStatus status;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;

    @Builder
    private Event(
            String hashUrl,
            String title,
            BigDecimal centerLatitude,
            BigDecimal centerLongitude,
            String locationName,
            LocalDate dateStart,
            LocalDate dateEnd,
            LocalTime timeStart,
            LocalTime timeEnd,
            EventStatus status,
            String timezone
    ) {
        this.hashUrl = hashUrl;
        this.title = title;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.locationName = locationName;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.status = status != null ? status : EventStatus.OPEN;
        this.timezone = timezone != null ? timezone : "Asia/Seoul";
    }

    public static Event create(
            String title,
            BigDecimal centerLatitude,
            BigDecimal centerLongitude,
            String locationName,
            LocalDate dateStart,
            LocalDate dateEnd,
            LocalTime timeStart,
            LocalTime timeEnd
    ) {
        return Event.builder()
                .hashUrl(EventHashUrlGenerator.generate())
                .title(title)
                .centerLatitude(centerLatitude)
                .centerLongitude(centerLongitude)
                .locationName(locationName)
                .dateStart(dateStart)
                .dateEnd(dateEnd)
                .timeStart(timeStart)
                .timeEnd(timeEnd)
                .build();
    }
}
