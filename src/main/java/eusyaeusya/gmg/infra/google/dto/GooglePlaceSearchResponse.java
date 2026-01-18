package eusyaeusya.gmg.infra.google.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GooglePlaceSearchResponse(
        @JsonProperty("places") List<Place> places
) {
    public record Place(
            @JsonProperty("id") String id
    ) {
    }
}
