package eusyaeusya.gmg.domain.place.service;

import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.entity.EventPlaceType;
import eusyaeusya.gmg.domain.event.repository.EventPlaceTypeRepository;
import eusyaeusya.gmg.domain.place.entity.PlaceCategory;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.repository.PlaceCategoryRepository;
import eusyaeusya.gmg.domain.place.service.search.PlaceFetchService;
import eusyaeusya.gmg.infra.kakao.client.KakaoMapClient;
import eusyaeusya.gmg.infra.kakao.dto.KakaoPlaceDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PlaceFetchServiceTest {

    private PlaceFetchService placeFetchService;

    @Mock
    private EventPlaceTypeRepository eventPlaceTypeRepository;

    @Mock
    private PlaceCategoryRepository placeCategoryRepository;

    @Mock
    private KakaoMapClient kakaoMapClient;

    @BeforeEach
    void setUp() {
        placeFetchService = new PlaceFetchService(
                eventPlaceTypeRepository,
                placeCategoryRepository,
                kakaoMapClient
        );
    }

    @Test
    @DisplayName("KakaoMapClient가 null이면 빈 리스트를 반환한다")
    void shouldReturnEmptyListWhenKakaoMapClientIsNull() {
        // given
        PlaceFetchService serviceWithNullClient = new PlaceFetchService(
                eventPlaceTypeRepository,
                placeCategoryRepository,
                null
        );
        Event event = mock(Event.class);

        // when
        List<KakaoPlaceDto> result = serviceWithNullClient.fetchPlacesForEvent(event);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이벤트에 해당하는 장소를 카카오맵에서 조회하고 중복을 제거한다")
    void shouldFetchAndDeduplicatePlaces() {
        // given
        Event event = mock(Event.class);
        given(event.getId()).willReturn(1L);
        given(event.getCenterLongitude()).willReturn(new BigDecimal("127.0"));
        given(event.getCenterLatitude()).willReturn(new BigDecimal("37.0"));

        PlaceType placeType = mock(PlaceType.class);
        EventPlaceType eventPlaceType = mock(EventPlaceType.class);
        given(eventPlaceType.getPlaceType()).willReturn(placeType);
        given(eventPlaceTypeRepository.findByEventWithPlaceType(event)).willReturn(List.of(eventPlaceType));

        PlaceCategory category1 = mock(PlaceCategory.class);
        given(category1.getName()).willReturn("개인카페"); // "카페"로 변환되어야 함
        PlaceCategory category2 = mock(PlaceCategory.class);
        given(category2.getName()).willReturn("맛집");
        given(placeCategoryRepository.findByPlaceTypeIn(anyList())).willReturn(List.of(category1, category2));

        KakaoPlaceDto dto1 = new KakaoPlaceDto("1", "Place 1", "FD6", "음식점", "한식", "127.0", "37.0", "Address 1", "Road 1", "010-1234-5678", "Link 1", "0");
        KakaoPlaceDto dto2 = new KakaoPlaceDto("2", "Place 2", "CE7", "카페", "카페", "127.1", "37.1", "Address 2", "Road 2", "010-1234-5679", "Link 2", "0");
        KakaoPlaceDto dto3 = new KakaoPlaceDto("1", "Place 1 Duplicate", "FD6", "음식점", "한식", "127.0", "37.0", "Address 1", "Road 1", "010-1234-5678", "Link 1", "0");

        given(kakaoMapClient.searchByKeywords(anyList(), anyString(), anyString(), anyInt()))
                .willReturn(List.of(dto1, dto2, dto3));

        // when
        List<KakaoPlaceDto> result = placeFetchService.fetchPlacesForEvent(event);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(KakaoPlaceDto::id).containsExactlyInAnyOrder("1", "2");
    }
}
