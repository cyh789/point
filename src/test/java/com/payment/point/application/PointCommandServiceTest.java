package com.payment.point.application;

import com.payment.point.api.request.CancelGrantRequest;
import com.payment.point.api.request.GrantPointRequest;
import com.payment.point.application.service.PointCommandService;
import com.payment.point.domain.grant.PointGrant;
import com.payment.point.infrastructure.persistence.grant.PointGrantEntity;
import com.payment.point.infrastructure.persistence.grant.PointGrantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PointCommandServiceTest {

    private static final AtomicLong counter = new AtomicLong();

    @Autowired
    private PointCommandService pointCommandService;

    @Autowired
    private PointGrantRepository pointGrantRepository;

    @Test
    @DisplayName("포인트를 정상적으로 적립한다")
    void grantPoint_success() {
        // given
        GrantPointRequest request = new GrantPointRequest();
        Long id = nextId();
        setRequest(request, id, 1000L, "AUTO", null);

        // when
        var response = pointCommandService.grantPoint(request);

        // then
        assertThat(response.getGrantId()).isNotNull();
        assertThat(response.getGrantedAmount()).isEqualTo(1000L);
        assertThat(response.getRemainingAmount()).isEqualTo(1000L);
        assertThat(response.getGrantType()).isEqualTo("AUTO");

        int balance = pointGrantRepository.sumRemainingAmountByUserId(id);
        assertThat(balance).isEqualTo(1000L);
    }

    @Test
    @DisplayName("1회 최대 적립 포인트를 초과하면 예외가 발생한다")
    void grantPoint_exceedMaxGrantPerOnce() {
        // given
        GrantPointRequest request = new GrantPointRequest();
        setRequest(request, nextId(), 200_000L, "AUTO", null);

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
        Long id = nextId();
        setRequest(first, id, 90_000L, "AUTO", null);
        pointCommandService.grantPoint(first);

        GrantPointRequest second = new GrantPointRequest();
        setRequest(second, id, 90_000L, "AUTO", null);

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
        setRequest(request, nextId(), 5000L, "MANUAL", null);

        // when
        var response = pointCommandService.grantPoint(request);

        // then
        assertThat(response.getGrantType()).isEqualTo("MANUAL");
    }

    @Test
    @DisplayName("만료일을 지정하지 않으면 기본 만료일 365일이 설정된다")
    void grantPoint_defaultExpireDate() {
        // given
        GrantPointRequest request = new GrantPointRequest();
        setRequest(request, nextId(), 1000L, "AUTO", null);
        LocalDateTime beforeGrant = LocalDateTime.now();

        // when
        var response = pointCommandService.grantPoint(request);

        // then
        assertThat(response.getExpireDate())
                .isNotNull()
                .isAfterOrEqualTo(beforeGrant.plusDays(365))
                .isBeforeOrEqualTo(beforeGrant.plusDays(366));
    }

    @Test
    @DisplayName("모든 포인트는 만료일이 존재하며, 만료일을 부여할 수 있다.")
    void grantPoint_withExpireDate_success() {
        // given
        GrantPointRequest request = new GrantPointRequest();
        LocalDateTime expireDate = LocalDateTime.now().plusDays(30);

        setRequest(request, nextId(), 1000L, "AUTO", expireDate);
        LocalDateTime beforeGrant = LocalDateTime.now();

        // when
        var response = pointCommandService.grantPoint(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getExpireDate()).isNotNull();
        assertThat(response.getExpireDate()).isAfter(beforeGrant);
    }

    @Test
    @DisplayName("포인트의 만료일은 최소 1일 이상이어야 하며, 그렇지 않으면 예외가 발생한다")
    void grantPoint_expireDate_lessThanOneDay_throwException() {
        // given
        GrantPointRequest request = new GrantPointRequest();
        LocalDateTime invalidExpireDate = LocalDateTime.now().plusHours(23);

        setRequest(request, nextId(), 1000L, "AUTO", invalidExpireDate);

        // when & then
        assertThatThrownBy(() -> pointCommandService.grantPoint(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("만료일은 최소");
    }

    @Test
    @DisplayName("포인트의 만료일은 최대 5년 미만이어야 하며, 이를 초과하면 예외가 발생한다")
    void grantPoint_expireDate_overFiveYears_throwException() {
        // given
        GrantPointRequest request = new GrantPointRequest();
        LocalDateTime invalidExpireDate = LocalDateTime.now().plusYears(5);

        setRequest(request, nextId(), 1000L, "AUTO", invalidExpireDate);

        // when & then
        assertThatThrownBy(() -> pointCommandService.grantPoint(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("만료일은 최대");
    }



    @Test
    @DisplayName("사용되지 않은 적립은 취소 가능하다")
    void cancelGrant_success() {
        // given
        GrantPointRequest request = new GrantPointRequest();
        setRequest(request, nextId(), 1000L, "AUTO", null);

        var response = pointCommandService.grantPoint(request);

        // when
        CancelGrantRequest cancelRequest = new CancelGrantRequest();
        setReason(cancelRequest, "사용자 포인트 적립 취소");

        var cancelResponse = pointCommandService.cancelGrant(response.getGrantId(), cancelRequest);

        // then
        assertThat(cancelResponse.getGrantedAmount()).isEqualTo(1000L);
        assertThat(cancelResponse.getRemainingAmount()).isEqualTo(0L);
        assertThat(cancelResponse.getStatus()).isEqualTo("CANCELED");
        assertThat(cancelResponse.getReason()).isEqualTo("사용자 포인트 적립 취소");
    }

    @Test
    @DisplayName("이미 사용된 적립은 취소할 수 없다")
    void cancelGrant_fail_whenUsed() {
        // given
        GrantPointRequest request = new GrantPointRequest();
        setRequest(request, nextId(), 1000L, "AUTO", null);

        var response = pointCommandService.grantPoint(request);

        // when
        PointGrantEntity entity = pointGrantRepository.findById(response.getGrantId())
                .orElseThrow();

        PointGrant grant = entity.toDomain();
        grant.use(100);

        entity.use(100);
        pointGrantRepository.save(entity);

        assertThat(entity.getGrantedAmount()).isEqualTo(1000L);
        assertThat(entity.getRemainingAmount()).isEqualTo(900L);
        assertThat(entity.getStatus()).isEqualTo("USED_PARTIAL");

        CancelGrantRequest cancelRequest = new CancelGrantRequest();

        // then
        assertThatThrownBy(() ->
                pointCommandService.cancelGrant(response.getGrantId(), cancelRequest)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 사용되었거나 만료된 적립");
    }

    private void setRequest(
            GrantPointRequest request,
            Long userId,
            Long amount,
            String grantType,
            java.time.LocalDateTime expireDate
    ) {
        try {
            var userIdField = GrantPointRequest.class.getDeclaredField("userId");
            var amountField = GrantPointRequest.class.getDeclaredField("amount");
            var grantTypeField = GrantPointRequest.class.getDeclaredField("grantType");
            var expireDateField = GrantPointRequest.class.getDeclaredField("expireDate");

            userIdField.setAccessible(true);
            amountField.setAccessible(true);
            grantTypeField.setAccessible(true);
            expireDateField.setAccessible(true);

            userIdField.set(request, userId);
            amountField.set(request, amount);
            grantTypeField.set(request, grantType);
            expireDateField.set(request, expireDate);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void setReason(CancelGrantRequest request, String reason) {
        try {
            var reasonField = CancelGrantRequest.class.getDeclaredField("reason");
            reasonField.setAccessible(true);
            reasonField.set(request, reason);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private long nextId() {
        return counter.incrementAndGet();
    }

}