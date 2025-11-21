package eusyaeusya.gmg.common.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import eusyaeusya.gmg.common.api.response.code.ErrorCode;
import eusyaeusya.gmg.common.api.response.code.SuccessCode;

public record ApiResponse<T>(String code, String message, @JsonInclude(JsonInclude.Include.NON_NULL) T data) {
    public ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 성공 응답 생성 (데이터 없음)
    public static ApiResponse<Void> success(final SuccessCode code) {
        return new ApiResponse<>(code.getValue(), code.getMessage(), null);
    }

    // 성공 응답 생성 (데이터 포함)
    public static <T> ApiResponse<T> successWithData(final SuccessCode code, final T data) {
        return new ApiResponse<>(code.getValue(), code.getMessage(), data);
    }

    // 실패 응답 생성
    public static ApiResponse<Void> fail(final ErrorCode errorCode, final String message) {
        return new ApiResponse<>(errorCode.getValue(), message, null);
    }
}
