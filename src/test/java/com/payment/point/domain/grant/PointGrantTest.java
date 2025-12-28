package com.payment.point.domain.grant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PointGrantTest {

    @Test
    @DisplayName("적립 직후에는 취소 가능하다")
    void canCancel_whenNotUsed() {
        PointGrant grant = PointGrant.create(
                1L,
                1_000L,
                "AUTO",
                null
        );

        assertThat(grant.canBeCanceled()).isTrue();
    }

    @Test
    @DisplayName("일부라도 사용된 적립은 취소할 수 없다")
    void cannotCancel_whenUsedPartially() {
        PointGrant grant = PointGrant.create(
                1L,
                1_000L,
                "AUTO",
                null
        );

        grant.use(100);

        assertThat(grant.canBeCanceled()).isFalse();
    }

    @Test
    @DisplayName("취소 처리되면 다시 취소할 수 없다")
    void cannotCancel_whenAlreadyCanceled() {
        PointGrant grant = PointGrant.create(
                1L,
                1_000L,
                "AUTO",
                null
        );

        grant.cancel("사용자 적립 취소 요청");

        assertThat(grant.canBeCanceled()).isFalse();
    }
}
