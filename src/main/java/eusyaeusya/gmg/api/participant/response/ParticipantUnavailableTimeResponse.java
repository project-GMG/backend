package eusyaeusya.gmg.api.participant.response;

public record ParticipantUnavailableTimeResponse(
        Long participantId,
        Integer registeredCount
) {
    public static ParticipantUnavailableTimeResponse of(Long participantId, int count) {
        return new ParticipantUnavailableTimeResponse(participantId, count);
    }
}
