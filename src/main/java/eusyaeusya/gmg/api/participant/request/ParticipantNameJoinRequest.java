package eusyaeusya.gmg.api.participant.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParticipantNameJoinRequest(
        @NotBlank(message = "참여자 이름은 필수입니다")
        @Size(max = 50, message = "참여자 이름은 최대 50자 입력 가능합니다")
        String name
) {
}
