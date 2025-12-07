package eusyaeusya.gmg.domain.event.util;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class EventHashUrlGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private static final int DEFAULT_HASH_LENGTH = 6;

    /**
     * 기본 길이 Hash URL 생성
     */
    public static String generate() {
        return generate(DEFAULT_HASH_LENGTH);
    }

    public static String generate(final int length) {
        if (length < 1 || length > 64) {
            log.debug("Hash URL 길이가 범위를 넘었습니다.");
            throw new IllegalArgumentException("Hash URL 길이는 1-64 사이여야 합니다.");
        }

        String encoded = generateEncodedBytes(length);

        // 요청된 길이만큼 잘라서 반환
        return encoded.substring(0, Math.min(length, encoded.length()));
    }

    private static String generateEncodedBytes(final int length) {
        //3바이트를 4자로 인코딩하므로, 필요한 바이트 수 계산
        int byteLength = (length * 3 / 4) + 1;
        byte[] randomBytes = new byte[byteLength];
        RANDOM.nextBytes(randomBytes);

        return ENCODER.encodeToString(randomBytes);
    }
}
