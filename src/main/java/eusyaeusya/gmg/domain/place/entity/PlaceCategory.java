package eusyaeusya.gmg.domain.place.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "place_categories",
        indexes = {
                @Index(name = "idx_place_category_code", columnList = "code")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_type_id", nullable = false)
    private PlaceType placeType;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, length = 50)
    private String code;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private PlaceCategory(PlaceType placeType, String name, String code) {
        this.placeType = placeType;
        this.name = name;
        this.code = code;
        this.createdAt = LocalDateTime.now();
    }

    public static PlaceCategory create(PlaceType placeType, String name, String code) {
        return PlaceCategory.builder()
                .placeType(placeType)
                .name(name)
                .code(code)
                .build();
    }
}
