package eusyaeusya.gmg.domain.participant.entity;

import eusyaeusya.gmg.domain.event.entity.Event;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "participants",
        indexes = {
                @Index(name = "idx_event_participant", columnList = "event_id, name")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ParticipantStatus participantStatus;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private Participant(Event event, String name) {
        this.event = event;
        this.name = name;
        this.participantStatus = ParticipantStatus.DRAFT;
        this.joinedAt = LocalDateTime.now();
    }

    public static Participant create(Event event, String name) {
        return new Participant(event, name);
    }

    public boolean isNotBelongsToEvent(Event event) {
        return !this.event.equals(event);
    }

    public void complete() {
        if (this.participantStatus == ParticipantStatus.COMPLETED) {
            return;
        }
        this.participantStatus = ParticipantStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
