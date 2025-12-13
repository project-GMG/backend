package eusyaeusya.gmg.api.participant.response;

import eusyaeusya.gmg.domain.participant.entity.Participant;
import eusyaeusya.gmg.domain.participant.entity.ParticipantStatus;

import java.time.LocalDateTime;

public record ParticipantCompleteResponse(
        Long participantId,
        String name,
        ParticipantStatus status,
        LocalDateTime completedAt
) {
    public static ParticipantCompleteResponse from(Participant participant) {
        return new ParticipantCompleteResponse(
                participant.getId(),
                participant.getName(),
                participant.getParticipantStatus(),
                participant.getCompletedAt()
        );
    }
}
