package eusyaeusya.gmg.domain.place.repository;

import eusyaeusya.gmg.domain.place.entity.PlaceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaceTypeRepository extends JpaRepository<PlaceType, Long> {
    List<PlaceType> findByCodeIn(List<String> codes);

    Optional<PlaceType> findByCode(String placeTypeCode);
}
