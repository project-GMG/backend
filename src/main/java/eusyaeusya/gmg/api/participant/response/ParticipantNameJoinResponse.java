package eusyaeusya.gmg.api.participant.response;

import eusyaeusya.gmg.domain.participant.entity.Participant;

import java.time.LocalDateTime;

public record ParticipantNameJoinResponse(
        Long participantId,
        String name,
        LocalDateTime joinedAt
) {
    public static ParticipantNameJoinResponse from(Participant participant) {
        return new ParticipantNameJoinResponse(
                participant.getId(),
                participant.getName(),
                participant.getJoinedAt()
        );
    }
}
