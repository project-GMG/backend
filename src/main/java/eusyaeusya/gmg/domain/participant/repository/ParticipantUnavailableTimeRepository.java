package eusyaeusya.gmg.domain.participant.repository;

import eusyaeusya.gmg.domain.participant.entity.ParticipantUnavailableTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParticipantUnavailableTimeRepository extends JpaRepository<ParticipantUnavailableTime, Long> {

    @Modifying
    @Query("DELETE FROM ParticipantUnavailableTime pt WHERE pt.participant.id = :participantId")
    void deleteAllByParticipantId(@Param("participantId") Long participantId);

    @Query("SELECT put FROM ParticipantUnavailableTime put " +
            "JOIN FETCH put.participant " +
            "WHERE put.event.id = :eventId")
    List<ParticipantUnavailableTime> findAllByEventId(@Param("eventId") Long eventId);
}
