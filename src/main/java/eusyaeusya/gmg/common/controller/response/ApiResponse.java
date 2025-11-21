package eusyaeusya.gmg.common.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import eusyaeusya.gmg.common.exception.ErrorCode;

public class ApiResponse<T> {
    private final String code;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    private ApiResponse(String code, String message, T data) {
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
