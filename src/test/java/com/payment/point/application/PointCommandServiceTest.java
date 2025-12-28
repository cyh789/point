package com.payment.point.application;

import com.payment.point.api.request.GrantPointRequest;
import com.payment.point.infrastructure.persistence.repository.PointGrantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PointCommandServiceTest {

    @Autowired
    private PointCommandService pointCommandService;

    @Autowired
    private PointGrantRepository pointGrantRepository;

    @Test
    @DisplayName("포인트를 정상적으로 적립한다")
    void grantPoint_success() {
        // given
        GrantPointRequest request = new GrantPointRequest();
        setRequest(request, 1L, 1000, "AUTO", null);

        // when
        var response = pointCommandService.grantPoint(request);

        // then
        assertThat(response.getGrantId()).isNotNull();
        assertThat(response.getTotalAmount()).isEqualTo(1000);
        assertThat(response.getRemainingAmount()).isEqualTo(1000);
        assertThat(response.getGrantType()).isEqualTo("AUTO");

        int balance = pointGrantRepository.sumRemainingAmountByUserId(1L);
        assertThat(balance).isEqualTo(1000);
    }

    @Test
    @DisplayName("1회 최대 적립 포인트를 초과하면 예외가 발생한다")
    void grantPoint_exceedMaxGrantPerOnce() {
        // given
        GrantPointRequest request = new GrantPointRequest();
        setRequest(request, 1L, 200_000, "AUTO", null);

        // when / then
        assertThatThrownBy(() -> pointCommandService.grantPoint(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1회 적립 한도");
    }

    @Test
    @DisplayName("개인 최대 보유 포인트를 초과하면 적립이 실패한다")
    void grantPoint_exceedMaxBalance() {
        // given
        GrantPointRequest first = new GrantPointRequest();
        setRequest(first, 1L, 90_000, "AUTO", null);
        pointCommandService.grantPoint(first);

        GrantPointRequest second = new GrantPointRequest();
        setRequest(second, 1L, 90_000, "AUTO", null);

        // when / then
        assertThatThrownBy(() -> pointCommandService.grantPoint(second))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최대 보유 포인트");
    }

    @Test
    @DisplayName("관리자 수기 지급 포인트는 MANUAL 타입으로 저장된다")
    void grantPoint_manualGrant() {
        // given
        GrantPointRequest request = new GrantPointRequest();
        setRequest(request, 2L, 5000, "MANUAL", null);

        // when
        var response = pointCommandService.grantPoint(request);

        // then
        assertThat(response.getGrantType()).isEqualTo("MANUAL");
    }

    @Test
    @DisplayName("만료일을 지정하지 않으면 기본 만료일(365일)이 설정된다")
    void grantPoint_defaultExpireDate() {
        // given
        GrantPointRequest request = new GrantPointRequest();
        setRequest(request, 3L, 1000, "AUTO", null);
        LocalDateTime beforeGrant = LocalDateTime.now();

        // when
        var response = pointCommandService.grantPoint(request);
        LocalDateTime afterGrant = LocalDateTime.now();

        // then
        assertThat(response.getExpireAt()).isNotNull();
        assertThat(response.getExpireAt()).isAfter(beforeGrant);
        assertThat(response.getExpireAt())
                .isBefore(afterGrant.plusDays(366));
    }

    private void setRequest(
            GrantPointRequest request,
            Long userId,
            int amount,
            String grantType,
            java.time.LocalDateTime expireAt
    ) {
        try {
            var userIdField = GrantPointRequest.class.getDeclaredField("userId");
            var amountField = GrantPointRequest.class.getDeclaredField("amount");
            var grantTypeField = GrantPointRequest.class.getDeclaredField("grantType");
            var expireAtField = GrantPointRequest.class.getDeclaredField("expireAt");

            userIdField.setAccessible(true);
            amountField.setAccessible(true);
            grantTypeField.setAccessible(true);
            expireAtField.setAccessible(true);

            userIdField.set(request, userId);
            amountField.set(request, amount);
            grantTypeField.set(request, grantType);
            expireAtField.set(request, expireAt);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}