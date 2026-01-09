package eusyaeusya.gmg.domain.place.repository;

import eusyaeusya.gmg.domain.place.entity.PlaceCategory;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaceCategoryRepository extends JpaRepository<PlaceCategory, Long> {

    List<PlaceCategory> findByPlaceTypeIn(List<PlaceType> placeTypesId);

    Optional<PlaceCategory> findByCode(String categoryCode);
}
