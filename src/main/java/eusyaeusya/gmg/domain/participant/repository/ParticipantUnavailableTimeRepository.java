package eusyaeusya.gmg.domain.participant.repository;

import eusyaeusya.gmg.domain.participant.entity.ParticipantStatus;
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
            "JOIN FETCH put.participant p " +
            "WHERE put.event.id = :eventId " +
            "AND p.participantStatus = :status")
    List<ParticipantUnavailableTime> findAllByEventIdAndStatus(
            @Param("eventId") Long eventId,
            @Param("status") ParticipantStatus status
    );

    /**
     * 기존 시간표 조회
     */
    @Query("SELECT put FROM ParticipantUnavailableTime put " +
            "WHERE put.participant.id = :participantId " +
            "ORDER BY put.unavailableDate, put.unavailableTimeStart")
    List<ParticipantUnavailableTime> findAllByParticipantId(@Param("participantId") Long participantId);
}
