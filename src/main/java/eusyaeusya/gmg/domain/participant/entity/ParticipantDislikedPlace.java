package eusyaeusya.gmg.domain.participant.entity;

import eusyaeusya.gmg.common.audit.entity.BaseTimeEntity;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.place.entity.Place;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "participant_disliked_places",
        indexes = {
                @Index(name = "idx_participant_disliked_place", columnList = "participant_id, place_id", unique = true)
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipantDislikedPlace extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Builder
    private ParticipantDislikedPlace(
            Event event,
            Participant participant,
            Place place
    ) {
        this.event = event;
        this.participant = participant;
        this.place = place;
    }

    public static ParticipantDislikedPlace create(
            Event event,
            Participant participant,
            Place place
    ) {
        return ParticipantDislikedPlace.builder()
                .event(event)
                .participant(participant)
                .place(place)
                .build();
    }
}
