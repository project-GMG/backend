package eusyaeusya.gmg.domain.event.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventHashUrlGeneratorTest {
    private static final int DEFAULT_HASH_LENGTH = 6;
    private static final String URL_SAFE_CHARS_REGEX = "^[A-Za-z0-9\\-_]+$";

    @Test
    @DisplayName("기본 길이 해시 생성 및 길이 검증 테스트")
    void success_generateDefaultLengthHashUrl() {
        // when
        String hashUrl = EventHashUrlGenerator.generate();

        // then
        assertThat(hashUrl).isNotNull();
        assertThat(hashUrl).hasSize(DEFAULT_HASH_LENGTH);
        assertThat(hashUrl).matches(URL_SAFE_CHARS_REGEX);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 64})
    @DisplayName("지정된 길이 해시 생성 및 길이 검증 테스트")
    void success_generateSpecifiedLengthHashUrl(int length) {
        // when
        String hashUrl = EventHashUrlGenerator.generate(length);

        // then
        assertThat(hashUrl).isNotNull();
        assertThat(hashUrl).hasSize(length);
        assertThat(hashUrl).matches(URL_SAFE_CHARS_REGEX);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 65})
    @DisplayName("유효하지 않은 길이(0 또는 65) 예외 발생 테스트")
    void fail_generateHashUrlWithInvalidLength(int length) {
        assertThatThrownBy(() -> EventHashUrlGenerator.generate(length))
                .isInstanceOf(IllegalArgumentException.class);
    }
}