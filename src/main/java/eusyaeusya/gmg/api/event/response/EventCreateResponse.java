package eusyaeusya.gmg.api.event.response;

import eusyaeusya.gmg.domain.event.entity.Event;

import java.time.LocalDateTime;

public record EventCreateResponse(
        Long eventId,
        String hashUrl,
        LocalDateTime createdAt
) {
    public static EventCreateResponse from(Event event) {
        return new EventCreateResponse(
                event.getId(),
                event.getHashUrl(),
                event.getCreatedAt()
        );
    }
}
