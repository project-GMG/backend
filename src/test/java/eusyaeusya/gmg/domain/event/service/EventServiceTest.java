package eusyaeusya.gmg.domain.event.service;

import eusyaeusya.gmg.api.event.request.EventCreateRequest;
import eusyaeusya.gmg.api.event.response.EventCreateResponse;
import eusyaeusya.gmg.api.event.response.EventErrorCode;
import eusyaeusya.gmg.common.api.exception.BadRequestException;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.repository.EventPlaceTypeRepository;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.service.PlaceTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {
    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventPlaceTypeRepository eventPlaceTypeRepository;

    @Mock
    private PlaceTypeService placeTypeService;

    @InjectMocks
    private EventService eventService;

    @Test
    @DisplayName("Event 정상 생성 테스트")
    void success_createEvent() {
        // given
        EventCreateRequest request = createValidRequest();
        List<PlaceType> mockPlaceTypes = createMockPlaceTypes();
        Event mockSavedEvent = createMockEvent();

        given(placeTypeService.findByCodes(request.placeTypeCodes()))
                .willReturn(mockPlaceTypes);
        given(eventRepository.save(any(Event.class)))
                .willReturn(mockSavedEvent);
        given(eventPlaceTypeRepository.saveAll(any()))
                .willReturn(List.of());
        // when
        EventCreateResponse response = eventService.createEvent(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.eventId()).isEqualTo(mockSavedEvent.getId());
        assertThat(response.hashUrl()).isEqualTo(mockSavedEvent.getHashUrl());
        assertThat(response.createdAt()).isNotNull();

        // verify
        then(placeTypeService).should(times(1)).findByCodes(request.placeTypeCodes());
        then(eventRepository).should(times(1)).save(any(Event.class));
        then(eventPlaceTypeRepository).should(times(1)).saveAll(any());
    }

    @Test
    @DisplayName("유효하지 않은 날짜 범위로 생성 시도 시 예외 발생")
    void fail_createEventWithInvalidDateRange() {
        // given
        EventCreateRequest request = createInvalidDateRangeRequest();
        List<PlaceType> mockPlaceTypes = createMockPlaceTypes();

        given(placeTypeService.findByCodes(request.placeTypeCodes()))
                .willReturn(mockPlaceTypes);
        // when // then
        assertThatThrownBy(() -> eventService.createEvent(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(EventErrorCode.INVALID_DATE_RANGE.getMessage());
        // verify
        then(eventRepository).should(never()).save(any(Event.class));
    }

    @Test
    @DisplayName("유효하지 않은 시간 범위로 생성 시도 시 예외 발생")
    void fail_createEventWithInvalidTimeRange() {
        // given
        EventCreateRequest request = createInvalidTimeRangeRequest();
        List<PlaceType> mockPlaceTypes = createMockPlaceTypes();

        given(placeTypeService.findByCodes(request.placeTypeCodes()))
                .willReturn(mockPlaceTypes);
        // when // then
        assertThatThrownBy(() -> eventService.createEvent(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(EventErrorCode.INVALID_TIME_RANGE.getMessage());

        // verify
        then(eventRepository).should(never()).save(any(Event.class));
    }

    private EventCreateRequest createValidRequest() {
        return new EventCreateRequest(
                "전북대에서 밥먹자",
                List.of("RESTAURANT", "CAFE"),
                new EventCreateRequest.LocationInfo(
                        new BigDecimal("35.8468"),
                        new BigDecimal("127.1296"),
                        "전북대학교"
                ),
                new EventCreateRequest.DateRangeInfo(
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(3)
                ),
                new EventCreateRequest.TimeRangeInfo(
                        LocalTime.of(13, 0),
                        LocalTime.of(23, 0)
                )
        );
    }

    private EventCreateRequest createInvalidDateRangeRequest() {
        return new EventCreateRequest(
                "전북대에서 밥먹자",
                List.of("RESTAURANT", "CAFE"),
                new EventCreateRequest.LocationInfo(
                        new BigDecimal("35.8468"),
                        new BigDecimal("127.1296"),
                        "전북대학교"
                ),
                new EventCreateRequest.DateRangeInfo(
                        LocalDate.now().plusDays(5), // 종료일이 시작일보다 빠름
                        LocalDate.now().plusDays(3)
                ),
                new EventCreateRequest.TimeRangeInfo(
                        LocalTime.of(13, 0),
                        LocalTime.of(23, 0)
                )
        );
    }

    private EventCreateRequest createInvalidTimeRangeRequest() {
        return new EventCreateRequest(
                "전북대에서 밥먹자",
                List.of("RESTAURANT", "CAFE"),
                new EventCreateRequest.LocationInfo(
                        new BigDecimal("35.8468"),
                        new BigDecimal("127.1296"),
                        "전북대학교"
                ),
                new EventCreateRequest.DateRangeInfo(
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(3)
                ),
                new EventCreateRequest.TimeRangeInfo(
                        LocalTime.of(23, 0), // 종료 시간이 시작 시간보다 빠름
                        LocalTime.of(13, 0)
                )
        );
    }

    private EventCreateRequest createTooLongDateRangeRequest() {
        return new EventCreateRequest(
                "전북대에서 밥먹자",
                List.of("RESTAURANT", "CAFE"),
                new EventCreateRequest.LocationInfo(
                        new BigDecimal("35.8468"),
                        new BigDecimal("127.1296"),
                        "전북대학교"
                ),
                new EventCreateRequest.DateRangeInfo(
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(40) // 35일 초과
                ),
                new EventCreateRequest.TimeRangeInfo(
                        LocalTime.of(13, 0),
                        LocalTime.of(23, 0)
                )
        );
    }

    private List<PlaceType> createMockPlaceTypes() {
        PlaceType restaurant = mock(PlaceType.class);
        PlaceType cafe = mock(PlaceType.class);
        return List.of(restaurant, cafe);
    }

    private Event createMockEvent() {
        Event mockEvent = mock(Event.class);
        given(mockEvent.getId()).willReturn(1L);
        given(mockEvent.getHashUrl()).willReturn("abc123def");
        given(mockEvent.getCreatedAt()).willReturn(java.time.LocalDateTime.now());
        return mockEvent;
    }
}