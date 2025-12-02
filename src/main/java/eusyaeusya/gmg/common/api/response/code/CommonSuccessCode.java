package eusyaeusya.gmg.common.api.response.code;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum CommonSuccessCode implements SuccessCode {
    SUCCESS("SU-0001", "요청이 성공적으로 처리되었습니다"),
    CREATE("SU-0002", "리소스가 생성되었습니다"),
    UPDATE("SU-0003", "리소스가 수정되었습니다"),
    DELETE("SU-0004", "리소스가 삭제되었습니다"),
    ;

    private final String value;
    private final String message;
}
