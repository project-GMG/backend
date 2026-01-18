package eusyaeusya.gmg.domain.place.entity;

import eusyaeusya.gmg.common.audit.entity.BaseTimeEntity;
import eusyaeusya.gmg.common.converter.JsonStringConverter;
import eusyaeusya.gmg.domain.place.util.OpeningHours;
import eusyaeusya.gmg.infra.kakao.dto.KakaoPlaceDto;
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
                @Index(name = "idx_is_active", columnList = "is_active"),
                @Index(name = "idx_place_external_id", columnList = "place_external_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_external_id", unique = true, length = 50)
    private String placeExternalId;

    @Column(length = 20)
    private String provider;

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

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(precision = 2, scale = 1, nullable = false)
    private BigDecimal rating;

    @Convert(converter = JsonStringConverter.class)
    @Column(name = "open_hours_json", columnDefinition = "JSON")
    private String openHoursJson;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder
    private Place(
            String placeExternalId,
            String provider,
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
        this.placeExternalId = placeExternalId;
        this.provider = provider;
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

    public static Place fromKakao(
            KakaoPlaceDto dto,
            PlaceType placeType,
            PlaceCategory category
    ) {
        return Place.builder()
                .placeExternalId(dto.id())
                .provider("KAKAO")
                .name(dto.placeName())
                .placeType(placeType)
                .category(category)
                .latitude(new BigDecimal(dto.y()))  // 카카오는 y가 위도
                .longitude(new BigDecimal(dto.x())) // x가 경도
                .address(dto.addressName())
                .imageUrl(null) // 카카오맵 API에는 이미지 URL 없음
                .rating(BigDecimal.ZERO) // 초기값
                .openHoursJson(null) // 카카오맵 기본 API에는 영업시간 없음
                .build();
    }

    public void updateInfoFromGoogle(String imageUrl, String openHoursJson) {
        if (imageUrl != null && !imageUrl.isBlank()) {
            this.imageUrl = imageUrl;
        }
        if (openHoursJson != null && !openHoursJson.equals("{}")) {
            this.openHoursJson = openHoursJson;
        }
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
