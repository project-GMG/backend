package eusyaeusya.gmg.domain.participant.repository;

import eusyaeusya.gmg.domain.participant.entity.ParticipantDislikedCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipantDislikedCategoryRepository extends JpaRepository<ParticipantDislikedCategory, Long> {
    @Modifying
    @Query("DELETE FROM ParticipantDislikedCategory pdc WHERE pdc.participant.id = :participantId")
    void deleteAllByParticipantId(@Param("participantId") Long participantId);
}
