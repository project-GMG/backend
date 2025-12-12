package eusyaeusya.gmg.domain.participant.repository;

import eusyaeusya.gmg.domain.participant.entity.ParticipantUnavailableTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipantUnavailableTimeRepository extends JpaRepository<ParticipantUnavailableTime, Long> {

    @Modifying
    @Query("DELETE FROM ParticipantUnavailableTime pt WHERE pt.participant.id = :participantId")
    void deleteAllByParticipantId(@Param("participantId") Long participantId);
}
