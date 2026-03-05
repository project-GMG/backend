package eusyaeusya.gmg.domain.participant.repository;

import eusyaeusya.gmg.domain.participant.entity.ParticipantDislikedPlace;
import eusyaeusya.gmg.domain.participant.entity.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParticipantDislikedPlaceRepository extends JpaRepository<ParticipantDislikedPlace, Long> {
    @Modifying
    @Query("DELETE FROM ParticipantDislikedPlace pdp WHERE pdp.participant.id = :participantId")
    void deleteAllByParticipantId(@Param("participantId") Long participantId);

    @Query("""
            SELECT pdp.place.id, COUNT(DISTINCT pdp.participant.id)
            FROM ParticipantDislikedPlace pdp
            WHERE pdp.event.id = :eventId
              AND pdp.participant.participantStatus = :status
            GROUP BY pdp.place.id
            """)
    List<Object[]> countDislikesByPlace(
            @Param("eventId") Long eventId,
            @Param("status") ParticipantStatus status
    );
}
