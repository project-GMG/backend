package eusyaeusya.gmg.domain.place.service;

import eusyaeusya.gmg.api.place.response.PlaceTypeResponse;
import eusyaeusya.gmg.common.api.exception.NotFoundException;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.repository.PlaceTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PlaceTypeServiceTest {

    @InjectMocks
    private PlaceTypeService placeTypeService;

    @Mock
    private PlaceTypeRepository placeTypeRepository;

    private PlaceType samplePlaceType1;
    private PlaceType samplePlaceType2;

    @BeforeEach
    void setUp() {
        samplePlaceType1 = PlaceType.builder().code("CAFE").label("카페").build();
        samplePlaceType2 = PlaceType.builder().code("REST").label("식당").build();
    }

    @Test
    @DisplayName("모든 PlaceType 조회에 성공한다")
    void success_findAllPlaceTypes() {
        // given
        List<PlaceType> mockPlaceTypes = Arrays.asList(samplePlaceType1, samplePlaceType2);
        given(placeTypeRepository.findAll()).willReturn(mockPlaceTypes);

        // when
        List<PlaceTypeResponse> responses = placeTypeService.getAllPlaceTypes();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).code()).isEqualTo("CAFE");
        assertThat(responses.get(1).label()).isEqualTo("식당");
        verify(placeTypeRepository).findAll();
    }

    @Test
    @DisplayName("유효한 코드 목록으로 장소 타입 조회 성공한다")
    void success_findPlaceTypesByCodes() {
        // given
        List<String> codes = Arrays.asList("CAFE", "REST");
        List<PlaceType> mockPlaceTypes = Arrays.asList(samplePlaceType1, samplePlaceType2);
        given(placeTypeRepository.findByCodeIn(codes)).willReturn(mockPlaceTypes);

        // when
        List<PlaceType> result = placeTypeService.findByCodes(codes);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getCode()).isEqualTo("CAFE");
        verify(placeTypeRepository).findByCodeIn(codes);
    }

    @Test
    @DisplayName("유효하지 않은 코드 목록으로 장소 타입 조회하면 예외를 던진다")
    void fail_findPlaceTypesByCodesWithInvalidCodes() {
        // given
        List<String> requestedCodes = Arrays.asList("CAFE", "REST", "HOSPITAL");
        List<PlaceType> foundPlaceTypes = Arrays.asList(samplePlaceType1, samplePlaceType2);
        given(placeTypeRepository.findByCodeIn(requestedCodes)).willReturn(foundPlaceTypes);
        // when // then
        assertThatThrownBy(() -> placeTypeService.findByCodes(requestedCodes))
                .isInstanceOf(NotFoundException.class);

    }
}