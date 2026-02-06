package eusyaeusya.gmg.api.participant.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParticipantNameJoinRequest(
        @Schema(description = "이름", example = "구성재", type = "string", required = true)
        @NotBlank(message = "참여자 이름은 필수입니다")
        @Size(max = 50, message = "참여자 이름은 최대 50자 입력 가능합니다")
        String name
) {
}
