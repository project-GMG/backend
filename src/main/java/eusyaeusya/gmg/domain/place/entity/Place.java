package eusyaeusya.gmg.domain.place.entity;

import eusyaeusya.gmg.common.audit.entity.BaseTimeEntity;
import eusyaeusya.gmg.domain.place.util.OpeningHours;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "places",
        indexes = {
                @Index(name = "idx_location", columnList = "latitude, longitude"),
                @Index(name = "idx_place_type", columnList = "place_type_id"),
                @Index(name = "idx_category_id", columnList = "category_id"),
                @Index(name = "idx_is_active", columnList = "is_active")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_type_id", nullable = false)
    private PlaceType placeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private PlaceCategory category;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 255)
    private String address;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(precision = 2, scale = 1, nullable = false)
    private BigDecimal rating;

    @Column(name = "open_hours_json", columnDefinition = "JSON")
    private String openHoursJson;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder
    private Place(
            String name,
            PlaceType placeType,
            PlaceCategory category,
            BigDecimal latitude,
            BigDecimal longitude,
            String address,
            String imageUrl,
            BigDecimal rating,
            String openHoursJson
    ) {
        this.name = name;
        this.placeType = placeType;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.openHoursJson = openHoursJson;
        this.isActive = true;
    }

    public static Place create(
            String name,
            PlaceType placeType,
            PlaceCategory category,
            BigDecimal latitude,
            BigDecimal longitude,
            String address,
            String imageUrl,
            BigDecimal rating,
            String openHoursJson
    ) {
        return Place.builder()
                .name(name)
                .placeType(placeType)
                .category(category)
                .latitude(latitude)
                .longitude(longitude)
                .address(address)
                .imageUrl(imageUrl)
                .rating(rating)
                .openHoursJson(openHoursJson)
                .build();
    }

    public OpeningHours getOpeningHours() {
        return new OpeningHours(openHoursJson);
    }

    public boolean isOpenOn(LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (openHoursJson == null || openHoursJson.isEmpty()) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return getOpeningHours().isOpenDuring(dayOfWeek, startTime, endTime);
    }
}
