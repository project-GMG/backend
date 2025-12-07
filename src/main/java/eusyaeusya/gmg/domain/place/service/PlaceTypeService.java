package eusyaeusya.gmg.domain.place.service;

import eusyaeusya.gmg.api.place.response.PlaceErrorCode;
import eusyaeusya.gmg.api.place.response.PlaceTypeResponse;
import eusyaeusya.gmg.common.api.exception.NotFoundException;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.repository.PlaceTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public List<PlaceType> findByCodes(List<String> codes) {
        List<PlaceType> placeTypes = placeTypeRepository.findByCodeIn(codes);

        if (placeTypes.size() != codes.size()) {
            Set<String> foundCodes = placeTypes.stream()
                    .map(PlaceType::getCode)
                    .collect(Collectors.toSet());

            List<String> notFoundCodes = codes.stream()
                    .filter(code -> !foundCodes.contains(code))
                    .toList();

            log.warn("PlaceType에 해당하지 않는 코드입니다: {}", notFoundCodes);
            throw new NotFoundException(
                    PlaceErrorCode.PLACE_TYPE_NOT_FOUND,
                    String.format("존재하지 않는 장소 타입입니다: %s", String.join(", ", notFoundCodes))
            );
        }

        return placeTypes;
    }
}
