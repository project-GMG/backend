package eusyaeusya.gmg.domain.participant.repository;

import eusyaeusya.gmg.domain.participant.entity.ParticipantDislikedCategory;
import eusyaeusya.gmg.domain.participant.entity.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParticipantDislikedCategoryRepository extends JpaRepository<ParticipantDislikedCategory, Long> {
    @Modifying
    @Query("DELETE FROM ParticipantDislikedCategory pdc WHERE pdc.participant.id = :participantId")
    void deleteAllByParticipantId(@Param("participantId") Long participantId);

    @Query("""
            SELECT pdc.category.id, COUNT(DISTINCT pdc.participant.id)
            FROM ParticipantDislikedCategory pdc
            WHERE pdc.event.id = :eventId
            AND pdc.participant.participantStatus = :status
            GROUP BY pdc.category.id
            """)
    List<Object[]> countDislikesByCategory(
            @Param("eventId") Long eventId,
            @Param("status") ParticipantStatus status
    );
}
