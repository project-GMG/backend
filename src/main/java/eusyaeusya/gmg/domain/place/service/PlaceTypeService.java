package eusyaeusya.gmg.domain.place.service;

import eusyaeusya.gmg.domain.place.dto.PlaceTypeResponse;
import eusyaeusya.gmg.domain.place.repository.PlaceTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceTypeService {
    private final PlaceTypeRepository placeTypeRepository;

    public List<PlaceTypeResponse> getAllPlaceTypes() {
        return placeTypeRepository.findAll().stream()
                .map(PlaceTypeResponse::from)
                .toList();
    }
}
