package eusyaeusya.gmg.api.feedback;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record FeedbackRequest(
        @Min(1) @Max(5)
        int rating,

        @Size(max = 500)
        String comment,

        String page
) {}
