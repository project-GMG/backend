package eusyaeusya.gmg.domain.participant.repository;

import eusyaeusya.gmg.domain.participant.entity.ParticipantDislikedPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipantDislikedPlaceRepository extends JpaRepository<ParticipantDislikedPlace, Long> {
    @Modifying
    @Query("DELETE FROM ParticipantDislikedPlace pdp WHERE pdp.participant.id = :participantId")
    void deleteAllByParticipantId(@Param("participantId") Long participantId);
}
