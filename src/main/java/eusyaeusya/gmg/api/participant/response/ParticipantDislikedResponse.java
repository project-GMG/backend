package eusyaeusya.gmg.api.participant.response;

public record ParticipantDislikedResponse(
        Long participantId,
        Integer dislikedCategoryCount,
        Integer dislikedPlaceCount
) {
    public static ParticipantDislikedResponse of(
            Long participantId,
            int categoryCount,
            int placeCount
    ) {
        return new ParticipantDislikedResponse(
                participantId,
                categoryCount,
                placeCount
        );
    }
}
