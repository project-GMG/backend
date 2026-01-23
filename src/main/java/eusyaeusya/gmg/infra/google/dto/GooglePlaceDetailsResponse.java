package eusyaeusya.gmg.infra.google.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GooglePlaceDetailsResponse(
        @JsonProperty("id") String id,
        @JsonProperty("displayName") DisplayName displayName,
        @JsonProperty("regularOpeningHours") OpeningHours regularOpeningHours,
        @JsonProperty("photos") List<Photo> photos
) {
    public record DisplayName(
            @JsonProperty("text") String text,
            @JsonProperty("languageCode") String languageCode
    ) {
    }

    public record OpeningHours(
            @JsonProperty("periods") List<Period> periods,
            @JsonProperty("weekdayDescriptions") List<String> weekdayDescriptions
    ) {
    }

    public record Period(
            @JsonProperty("open") TimeInfo open,
            @JsonProperty("close") TimeInfo close
    ) {
    }

    public record TimeInfo(
            @JsonProperty("day") Integer day,
            @JsonProperty("hour") Integer hour,
            @JsonProperty("minute") Integer minute
    ) {
    }

    public record Photo(
            @JsonProperty("name") String name,
            @JsonProperty("heightPx") Integer heightPx,
            @JsonProperty("widthPx") Integer widthPx
    ) {
    }
}
