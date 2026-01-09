package eusyaeusya.gmg.domain.event.service;

import eusyaeusya.gmg.api.event.request.EventCreateRequest;
import eusyaeusya.gmg.api.event.response.EventCreateResponse;
import eusyaeusya.gmg.api.event.response.EventErrorCode;
import eusyaeusya.gmg.api.event.response.EventMainResponse;
import eusyaeusya.gmg.common.api.exception.BadRequestException;
import eusyaeusya.gmg.common.api.exception.NotFoundException;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.entity.EventPlaceType;
import eusyaeusya.gmg.domain.event.entity.EventStatus;
import eusyaeusya.gmg.domain.event.repository.EventPlaceTypeRepository;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.participant.repository.ParticipantRepository;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.service.PlaceTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private HeatmapService heatmapService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

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
        then(eventPublisher).should(times(1)).publishEvent(any(PlaceSearchEvent.class));
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

    @Test
    @DisplayName("모임 메인 페이지 조회 성공")
    void getEventMain_success() {
        // given
        String hashUrl = "abc123";
        Event mockEvent = createMockEvent(hashUrl);

        List<EventPlaceType> mockPlaceTypes = List.of(
                createMockEventPlaceType("RESTAURANT", "식당"),
                createMockEventPlaceType("CAFE", "카페")
        );

        List<EventMainResponse.HeatmapSlot> mockHeatmap = List.of(
                EventMainResponse.HeatmapSlot.builder()
                        .date(LocalDate.parse("2025-11-24"))
                        .timeSlot(LocalTime.parse("15:00"))
                        .availableCount(5)
                        .intensity(1.0)
                        .build()
        );

        given(eventRepository.findByHashUrl(hashUrl))
                .willReturn(Optional.of(mockEvent));
        given(eventPlaceTypeRepository.findByEventWithPlaceType(mockEvent))
                .willReturn(mockPlaceTypes);
        given(participantRepository.countByEventId(mockEvent.getId()))
                .willReturn(5);
        given(heatmapService.calculateHeatmap(mockEvent))
                .willReturn(mockHeatmap);

        // when
        EventMainResponse response = eventService.getEventMain(hashUrl);

        // then
        assertThat(response).isNotNull();
        assertThat(response.eventId()).isEqualTo(1L);
        assertThat(response.hashUrl()).isEqualTo(hashUrl);
        assertThat(response.title()).isEqualTo("전북대에서 밥먹자");
        assertThat(response.status()).isEqualTo("OPEN");

        assertThat(response.placeTypes()).hasSize(2);
        assertThat(response.placeTypes().get(0).code()).isEqualTo("RESTAURANT");
        assertThat(response.placeTypes().get(1).code()).isEqualTo("CAFE");

        assertThat(response.location().centerLatitude())
                .isEqualByComparingTo(new BigDecimal("35.8468"));
        assertThat(response.location().locationName()).isEqualTo("전북대학교");

        assertThat(response.dateRange().startDate()).isEqualTo(LocalDate.parse("2025-11-24"));
        assertThat(response.dateRange().endDate()).isEqualTo(LocalDate.parse("2025-11-25"));

        assertThat(response.timeRange().startTime()).isEqualTo(LocalTime.parse("09:00"));
        assertThat(response.timeRange().endTime()).isEqualTo(LocalTime.parse("23:00"));

        assertThat(response.participantCount()).isEqualTo(5);
        assertThat(response.heatmapData()).hasSize(1);
    }

    @Test
    @DisplayName("참여자가 없어도 조회 성공")
    void success_getEventMain_noParticipants() {
        // given
        String hashUrl = "abc123";
        Event mockEvent = createMockEvent(hashUrl);

        given(eventRepository.findByHashUrl(hashUrl))
                .willReturn(Optional.of(mockEvent));
        given(eventPlaceTypeRepository.findByEventWithPlaceType(mockEvent))
                .willReturn(Collections.emptyList());
        given(participantRepository.countByEventId(mockEvent.getId()))
                .willReturn(0);
        given(heatmapService.calculateHeatmap(mockEvent))
                .willReturn(Collections.emptyList());

        // when
        EventMainResponse response = eventService.getEventMain(hashUrl);

        // then
        assertThat(response.participantCount()).isEqualTo(0);
        assertThat(response.heatmapData()).isEmpty();
    }

    @Test
    @DisplayName("이벤트 없음 - 예외 발생")
    void fail_getEventMain_eventNotFound() {
        // given
        String hashUrl = "nonexistent";
        given(eventRepository.findByHashUrl(hashUrl))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> eventService.getEventMain(hashUrl))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(EventErrorCode.EVENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("선택된 장소 타입이 없어도 조회 성공")
    void success_getEventMain_noPlaceTypes() {
        // given
        String hashUrl = "abc123";
        Event mockEvent = createMockEvent(hashUrl);

        given(eventRepository.findByHashUrl(hashUrl))
                .willReturn(Optional.of(mockEvent));
        given(eventPlaceTypeRepository.findByEventWithPlaceType(mockEvent))
                .willReturn(Collections.emptyList());
        given(participantRepository.countByEventId(mockEvent.getId()))
                .willReturn(3);
        given(heatmapService.calculateHeatmap(mockEvent))
                .willReturn(Collections.emptyList());

        // when
        EventMainResponse response = eventService.getEventMain(hashUrl);

        // then
        assertThat(response.placeTypes()).isEmpty();
        assertThat(response.participantCount()).isEqualTo(3);
    }

    private EventPlaceType createMockEventPlaceType(String code, String label) {
        PlaceType placeType = mock(PlaceType.class);
        given(placeType.getId()).willReturn(1L);
        given(placeType.getCode()).willReturn(code);
        given(placeType.getLabel()).willReturn(label);

        EventPlaceType eventPlaceType = mock(EventPlaceType.class);
        given(eventPlaceType.getPlaceType()).willReturn(placeType);
        return eventPlaceType;
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

    private List<PlaceType> createMockPlaceTypes() {
        PlaceType restaurant = mock(PlaceType.class);
        PlaceType cafe = mock(PlaceType.class);
        return List.of(restaurant, cafe);
    }

    private Event createMockEvent(String hashUrl) {
        Event event = mock(Event.class);

        LocalDate dateStart = LocalDate.parse("2025-11-24");
        LocalDate dateEnd = LocalDate.parse("2025-11-25");
        LocalTime timeStart = LocalTime.parse("09:00");
        LocalTime timeEnd = LocalTime.parse("23:00");

        given(event.getId()).willReturn(1L);
        given(event.getHashUrl()).willReturn(hashUrl);
        given(event.getTitle()).willReturn("전북대에서 밥먹자");

        // EventStatus mock
        EventStatus mockStatus = mock(EventStatus.class);
        given(mockStatus.name()).willReturn("OPEN");
        given(event.getStatus()).willReturn(mockStatus);

        given(event.getCenterLatitude()).willReturn(new BigDecimal("35.8468"));
        given(event.getCenterLongitude()).willReturn(new BigDecimal("127.1296"));
        given(event.getLocationName()).willReturn("전북대학교");
        given(event.getDateStart()).willReturn(dateStart);
        given(event.getDateEnd()).willReturn(dateEnd);
        given(event.getTimeStart()).willReturn(timeStart);
        given(event.getTimeEnd()).willReturn(timeEnd);
        given(event.getCreatedAt()).willReturn(java.time.LocalDateTime.now());

        return event;
    }

    private Event createMockEvent() {
        Event mockEvent = mock(Event.class);
        given(mockEvent.getId()).willReturn(1L);
        given(mockEvent.getHashUrl()).willReturn("abc123def");
        given(mockEvent.getCreatedAt()).willReturn(java.time.LocalDateTime.now());
        return mockEvent;
    }
}