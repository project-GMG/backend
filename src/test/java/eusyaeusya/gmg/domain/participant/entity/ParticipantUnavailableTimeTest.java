package eusyaeusya.gmg.domain.participant.entity;

import eusyaeusya.gmg.common.api.exception.BadRequestException;
import eusyaeusya.gmg.domain.event.entity.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ParticipantUnavailableTimeTest {

    @Test
    @DisplayName("선택되지 않은 gap day의 불가능 시간은 생성할 수 없다")
    void create_rejectsGapDay() {
        Event event = Event.create(
                "다같이 만나요",
                new BigDecimal("35.8468"),
                new BigDecimal("127.1296"),
                "전북대학교",
                List.of(
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(2),
                        LocalDate.now().plusDays(8),
                        LocalDate.now().plusDays(9)
                ),
                LocalTime.of(13, 0),
                LocalTime.of(18, 0)
        );

        Participant participant = mock(Participant.class);
        LocalDate gapDay = LocalDate.now().plusDays(4);

        assertThatThrownBy(() -> ParticipantUnavailableTime.create(
                event,
                participant,
                gapDay,
                LocalTime.of(13, 0),
                LocalTime.of(13, 30)
        )).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("참여할 수 없는 날입니다");
    }
}
