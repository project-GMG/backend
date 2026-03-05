package eusyaeusya.gmg.domain.participant.repository;

import eusyaeusya.gmg.domain.participant.entity.Participant;
import eusyaeusya.gmg.domain.participant.entity.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Query("SELECT COUNT(p) FROM Participant p WHERE p.event.id = :eventId")
    int countByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(p) FROM Participant p WHERE p.event.id = :eventId AND p.participantStatus = :status")
    int countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") ParticipantStatus status);

    @Query("SELECT p FROM Participant p WHERE p.event.id = :eventId AND p.name = :name")
    Optional<Participant> findByEventIdAndName(@Param("eventId") Long eventId, @Param("name") String name);
}
