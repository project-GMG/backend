package eusyaeusya.gmg.domain.participant.service;

import eusyaeusya.gmg.api.event.response.EventErrorCode;
import eusyaeusya.gmg.api.participant.request.ParticipantDislikedRequest;
import eusyaeusya.gmg.api.participant.response.ParticipantDislikedResponse;
import eusyaeusya.gmg.api.participant.response.ParticipantErrorCode;
import eusyaeusya.gmg.api.place.response.PlaceErrorCode;
import eusyaeusya.gmg.common.api.exception.BadRequestException;
import eusyaeusya.gmg.common.api.exception.NotFoundException;
import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.event.entity.EventPlaceType;
import eusyaeusya.gmg.domain.event.repository.EventPlaceTypeRepository;
import eusyaeusya.gmg.domain.event.repository.EventRepository;
import eusyaeusya.gmg.domain.participant.entity.Participant;
import eusyaeusya.gmg.domain.participant.repository.ParticipantDislikedCategoryRepository;
import eusyaeusya.gmg.domain.participant.repository.ParticipantDislikedPlaceRepository;
import eusyaeusya.gmg.domain.participant.repository.ParticipantRepository;
import eusyaeusya.gmg.domain.place.entity.Place;
import eusyaeusya.gmg.domain.place.entity.PlaceCategory;
import eusyaeusya.gmg.domain.place.entity.PlaceType;
import eusyaeusya.gmg.domain.place.repository.PlaceCategoryRepository;
import eusyaeusya.gmg.domain.place.repository.PlaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantDislikedServiceTest {

    @InjectMocks
    private ParticipantDislikedService dislikedService;

    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventPlaceTypeRepository eventPlaceTypeRepository;
    @Mock
    private PlaceCategoryRepository placeCategoryRepository;
    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private ParticipantDislikedCategoryRepository dislikedCategoryRepository;
    @Mock
    private ParticipantDislikedPlaceRepository dislikedPlaceRepository;

    @Test
    @DisplayName("비선호 카테고리 및 장소 등록 성공")
    void success_registerDisliked() {
        // given
        String hashUrl = "hash123";
        Long participantId = 1L;

        Event event = mockOpenEvent();
        Participant participant = mockParticipant(event);
        PlaceType placeType = mockPlaceType();
        EventPlaceType eventPlaceType = mockEventPlaceType(placeType);

        PlaceCategory category1 = mockCategory(1L, placeType);
        PlaceCategory category2 = mockCategory(2L, placeType);
        Place place1 = mockPlace(10L, placeType);
        Place place2 = mockPlace(20L, placeType);

        ParticipantDislikedRequest request = new ParticipantDislikedRequest(
                List.of(1L, 2L),
                List.of(10L, 20L)
        );

        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(event));
        given(participantRepository.findById(participantId)).willReturn(Optional.of(participant));
        given(eventPlaceTypeRepository.findByEventWithPlaceType(event)).willReturn(List.of(eventPlaceType));
        given(placeCategoryRepository.findAllById(request.dislikedCategoryIds()))
                .willReturn(List.of(category1, category2));
        given(placeRepository.findAllById(request.dislikedPlaceIds()))
                .willReturn(List.of(place1, place2));

        // when
        ParticipantDislikedResponse response = dislikedService.registerDisliked(
                hashUrl, participantId, request
        );

        // then
        assertThat(response.participantId()).isEqualTo(participantId);
        assertThat(response.dislikedCategoryCount()).isEqualTo(2);
        assertThat(response.dislikedPlaceCount()).isEqualTo(2);

        then(dislikedCategoryRepository).should().deleteAllByParticipantId(participantId);
        then(dislikedPlaceRepository).should().deleteAllByParticipantId(participantId);
        then(dislikedCategoryRepository).should().saveAll(any());
        then(dislikedPlaceRepository).should().saveAll(any());
    }

    @Test
    @DisplayName("빈 배열로 요청 시 기존 데이터만 삭제")
    void success_registerDisliked_emptyArrays() {
        // given
        String hashUrl = "hash123";
        Long participantId = 1L;

        Event event = mockOpenEvent();
        Participant participant = mockParticipant(event);

        ParticipantDislikedRequest request = new ParticipantDislikedRequest(
                List.of(),
                List.of()
        );

        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(event));
        given(participantRepository.findById(participantId)).willReturn(Optional.of(participant));

        // when
        ParticipantDislikedResponse response = dislikedService.registerDisliked(
                hashUrl, participantId, request
        );

        // then
        assertThat(response.dislikedCategoryCount()).isZero();
        assertThat(response.dislikedPlaceCount()).isZero();

        then(dislikedCategoryRepository).should().deleteAllByParticipantId(participantId);
        then(dislikedPlaceRepository).should().deleteAllByParticipantId(participantId);
        then(dislikedCategoryRepository).should(never()).saveAll(any());
        then(dislikedPlaceRepository).should(never()).saveAll(any());
    }

    @Test
    @DisplayName("존재하지 않는 이벤트로 등록 시 예외 발생")
    void fail_registerDisliked_eventNotFound() {
        // given
        String hashUrl = "invalid";
        Long participantId = 1L;
        ParticipantDislikedRequest request = new ParticipantDislikedRequest(List.of(), List.of());

        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dislikedService.registerDisliked(hashUrl, participantId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(EventErrorCode.EVENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("마감된 이벤트에 등록 시 예외 발생")
    void fail_registerDisliked_eventClosed() {
        // given
        String hashUrl = "hash123";
        Long participantId = 1L;
        Event closedEvent = mockClosedEvent();
        ParticipantDislikedRequest request = new ParticipantDislikedRequest(List.of(), List.of());

        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(closedEvent));

        // when & then
        assertThatThrownBy(() -> dislikedService.registerDisliked(hashUrl, participantId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(EventErrorCode.EVENT_ALREADY_CLOSED.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 참여자로 등록 시 예외 발생")
    void fail_registerDisliked_participantNotFound() {
        // given
        String hashUrl = "hash123";
        Long participantId = 999L;
        Event event = mockOpenEvent();
        ParticipantDislikedRequest request = new ParticipantDislikedRequest(List.of(), List.of());

        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(event));
        given(participantRepository.findById(participantId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dislikedService.registerDisliked(hashUrl, participantId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(ParticipantErrorCode.PARTICIPANT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 ID 요청 시 예외 발생")
    void fail_registerDisliked_categoryNotFound() {
        // given
        String hashUrl = "hash123";
        Long participantId = 1L;
        Event event = mockOpenEvent();
        Participant participant = mockParticipant(event);

        ParticipantDislikedRequest request = new ParticipantDislikedRequest(
                List.of(1L, 999L),
                List.of()
        );

        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(event));
        given(participantRepository.findById(participantId)).willReturn(Optional.of(participant));
        // 일부만 존재하도록 1개만 반환 (검증 로직은 개수 비교만 하므로 getPlaceType 스텁 불필요)
        given(placeCategoryRepository.findAllById(request.dislikedCategoryIds()))
                .willReturn(List.of(mock(PlaceCategory.class)));

        // when
        Throwable thrown = catchThrowable(() ->
                dislikedService.registerDisliked(hashUrl, participantId, request)
        );

        // then
        assertThat(thrown).isInstanceOf(NotFoundException.class);
        NotFoundException nfe = (NotFoundException) thrown;
        assertThat(nfe.getErrorCode()).isEqualTo(PlaceErrorCode.PLACE_CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("이벤트 PlaceType에 속하지 않는 카테고리 요청 시 예외 발생")
    void fail_registerDisliked_categoryNotBelongsToEventPlaceType() {
        // given
        String hashUrl = "hash123";
        Long participantId = 1L;
        Event event = mockOpenEvent();
        Participant participant = mockParticipant(event);

        // 이벤트는 placeTypeA만 허용
        PlaceType placeTypeA = mockPlaceType();
        EventPlaceType allowed = mockEventPlaceType(placeTypeA);

        // 요청 카테고리는 placeTypeB로 설정하여 불일치 유발
        PlaceType placeTypeB = mockPlaceType();
        PlaceCategory invalidCategory = mockCategory(100L, placeTypeB);

        ParticipantDislikedRequest request = new ParticipantDislikedRequest(
                List.of(100L),
                List.of()
        );

        given(eventRepository.findByHashUrl(hashUrl)).willReturn(Optional.of(event));
        given(participantRepository.findById(participantId)).willReturn(Optional.of(participant));
        given(placeCategoryRepository.findAllById(request.dislikedCategoryIds()))
                .willReturn(List.of(invalidCategory));
        given(eventPlaceTypeRepository.findByEventWithPlaceType(event))
                .willReturn(List.of(allowed));

        // when & then
        assertThatThrownBy(() -> dislikedService.registerDisliked(hashUrl, participantId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(PlaceErrorCode.CATEGORY_NOT_IN_EVENT_PLACE_TYPES.getMessage());
    }

    // ===== helper mocks =====
    private Event mockOpenEvent() {
        Event event = mock(Event.class);
        when(event.isClosed()).thenReturn(false);
        return event;
    }

    private Event mockClosedEvent() {
        Event event = mock(Event.class);
        when(event.isClosed()).thenReturn(true);
        return event;
    }

    private Participant mockParticipant(Event event) {
        Participant participant = mock(Participant.class);
        when(participant.isNotBelongsToEvent(event)).thenReturn(false);
        return participant;
    }

    private PlaceType mockPlaceType() {
        return mock(PlaceType.class);
    }

    private EventPlaceType mockEventPlaceType(PlaceType placeType) {
        EventPlaceType eventPlaceType = mock(EventPlaceType.class);
        when(eventPlaceType.getPlaceType()).thenReturn(placeType);
        return eventPlaceType;
    }

    private PlaceCategory mockCategory(Long id, PlaceType placeType) {
        PlaceCategory category = mock(PlaceCategory.class);
        when(category.getPlaceType()).thenReturn(placeType);
        return category;
    }

    private Place mockPlace(Long id, PlaceType placeType) {
        Place place = mock(Place.class);
        when(place.getPlaceType()).thenReturn(placeType);
        return place;
    }
}
