package eusyaeusya.gmg.domain.place.repository;

import eusyaeusya.gmg.domain.place.entity.Place;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByPlaceExternalId(String placeExternalId);

    List<Place> findAllByPlaceExternalIdIn(List<String> placeExternalIds);

    @Query("SELECT p FROM Place p JOIN FETCH p.placeType WHERE p.placeType.id IN :placeTypeIds AND p.isActive = true")
    List<Place> findAllByPlaceTypeIdInAndIsActiveTrue(@Param("placeTypeIds") List<Long> placeTypeIds);

    @Query(value = """
            SELECT p.id AS id, p.name AS name, p.image_url AS imageUrl
            FROM places p
            WHERE p.category_id = :categoryId
              AND p.is_active = true
              AND p.latitude BETWEEN :minLat AND :maxLat
              AND p.longitude BETWEEN :minLng AND :maxLng
              AND ST_Distance_Sphere(
                    POINT(p.longitude, p.latitude),
                    POINT(:centerLng, :centerLat)
                  ) <= :radiusMeters
            ORDER BY ST_Distance_Sphere(
                       POINT(p.longitude, p.latitude),
                       POINT(:centerLng, :centerLat)
                     ) ASC
            """, nativeQuery = true)
    Slice<PlaceSimpleProjection> findPlacesWithinRadius(
            @Param("categoryId") Long categoryId,
            @Param("centerLat") double centerLat,
            @Param("centerLng") double centerLng,
            @Param("radiusMeters") int radiusMeters,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng,
            Pageable pageable
    );

    @Query(value = """
            SELECT p.*
            FROM places p
            WHERE p.place_type_id IN (:placeTypeIds)
              AND p.is_active = true
              AND p.latitude BETWEEN :minLat AND :maxLat
              AND p.longitude BETWEEN :minLng AND :maxLng
              AND ST_Distance_Sphere(
                    POINT(p.longitude, p.latitude),
                    POINT(:centerLng, :centerLat)
                  ) <= :radiusMeters
            """, nativeQuery = true)
    List<Place> findPlacesWithinRadiusByPlaceTypeIds(
            @Param("placeTypeIds") List<Long> placeTypeIds,
            @Param("centerLat") double centerLat,
            @Param("centerLng") double centerLng,
            @Param("radiusMeters") int radiusMeters,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng
    );
}
