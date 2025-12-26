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
            SELECT pdc.category.placeType.id, COUNT(DISTINCT pdc.participant.id)
            FROM ParticipantDislikedCategory pdc
            WHERE pdc.event.hashUrl = :hashUrl
            AND pdc.participant.participantStatus = :status
            GROUP BY pdc.category.placeType.id
            """)
    List<Object[]> countDislikesByPlaceType(
            @Param("hashUrl") String hashUrl,
            @Param("status") ParticipantStatus status
    );
}
