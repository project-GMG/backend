package eusyaeusya.gmg.domain.place.service;

import eusyaeusya.gmg.domain.place.entity.PlaceCategory;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.repository.PlaceCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceCategoryService {

    private final PlaceCategoryRepository placeCategoryRepository;

    public Map<PlaceType, List<PlaceCategory>> getCategoriesGroupedByPlaceType(List<PlaceType> placeTypes) {
        log.debug("PlaceType별 카테고리 조회: placeTypeCount={}", placeTypes.size());

        List<PlaceCategory> categories = placeCategoryRepository.findByPlaceTypeIn(placeTypes);

        return categories.stream()
                .collect(Collectors.groupingBy(PlaceCategory::getPlaceType));
    }
}
