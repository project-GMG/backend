package eusyaeusya.gmg.domain.place.repository;

import eusyaeusya.gmg.domain.place.entity.Place;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceRepository extends JpaRepository<Place, Long> {

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
}
