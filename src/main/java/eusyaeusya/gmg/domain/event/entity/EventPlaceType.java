package eusyaeusya.gmg.domain.event.entity;

import eusyaeusya.gmg.domain.place.entity.PlaceType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "event_place_types",
        indexes = {
                @Index(name = "idx_event_place_type", columnList = "event_id, place_type_id", unique = true)
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventPlaceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_type_id", nullable = false)
    private PlaceType placeType;

    @Column(name = "selected_at", nullable = false)
    private LocalDateTime selectedAt;

    @Builder
    private EventPlaceType(Event event, PlaceType placeType, LocalDateTime selectedAt) {
        this.event = event;
        this.placeType = placeType;
        this.selectedAt = selectedAt != null ? selectedAt : LocalDateTime.now();
    }

    public static EventPlaceType create(Event event, PlaceType placeType) {
        return EventPlaceType.builder()
                .event(event)
                .placeType(placeType)
                .build();
    }
}
