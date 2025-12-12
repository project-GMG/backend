package eusyaeusya.gmg.domain.participant.entity;

import eusyaeusya.gmg.common.audit.entity.BaseTimeEntity;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.place.entity.PlaceCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "participant_disliked_categories",
        indexes = {
                @Index(name = "idx_participant_disliked_category", columnList = "participant_id, category_id", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipantDislikedCategory extends BaseTimeEntity {

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
    @JoinColumn(name = "category_id", nullable = false)
    private PlaceCategory category;

    @Builder
    private ParticipantDislikedCategory(
            Event event,
            Participant participant,
            PlaceCategory category
    ) {
        this.event = event;
        this.participant = participant;
        this.category = category;
    }

    public static ParticipantDislikedCategory create(
            Event event,
            Participant participant,
            PlaceCategory category
    ) {
        return ParticipantDislikedCategory.builder()
                .event(event)
                .participant(participant)
                .category(category)
                .build();
    }
}
