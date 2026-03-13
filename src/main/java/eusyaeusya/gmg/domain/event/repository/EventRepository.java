package eusyaeusya.gmg.domain.event.repository;

import eusyaeusya.gmg.domain.event.entity.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByHashUrl(String hashUrl);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.hashUrl =:hashUrl")
    Optional<Event> findByHashUrlWithLock(@Param("hashUrl") String hashUrl);

    @Query("SELECT e FROM Event e WHERE e.status != 'EXPIRED' AND e.dateEnd < :threshold")
    List<Event> findByStatusNotExpiredAndDateEndBefore(@Param("threshold") LocalDate threshold);
}
