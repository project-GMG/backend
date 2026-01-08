package eusyaeusya.gmg.infra.kakao.exception;

public class KakaoMapApiException extends RuntimeException {
    public KakaoMapApiException(String message) {
        super(message);
    }

    public KakaoMapApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
