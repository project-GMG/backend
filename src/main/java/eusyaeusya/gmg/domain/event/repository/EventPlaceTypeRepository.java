package eusyaeusya.gmg.domain.event.repository;

import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.entity.EventPlaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventPlaceTypeRepository extends JpaRepository<EventPlaceType, Long> {
    @Query("SELECT ept FROM EventPlaceType ept JOIN FETCH ept.placeType WHERE ept.event = :event")
    List<EventPlaceType> findByEventWithPlaceType(@Param("event") Event event);
}
