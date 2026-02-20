package eusyaeusya.gmg.api.participant.response;

import eusyaeusya.gmg.domain.participant.entity.Participant;

public record ParticipantCheckResponse(
        boolean exists,
        Long participantId
) {
    public static ParticipantCheckResponse found(Participant participant) {
        return new ParticipantCheckResponse(true, participant.getId());
    }

    public static ParticipantCheckResponse notFound() {
        return new ParticipantCheckResponse(false, null);
    }
}
