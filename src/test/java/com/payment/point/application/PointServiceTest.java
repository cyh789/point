package com.payment.point.application;

import com.payment.point.api.request.CancelGrantRequest;
import com.payment.point.api.request.CancelUsageRequest;
import com.payment.point.api.request.GrantPointRequest;
import com.payment.point.api.request.UsePointRequest;
import com.payment.point.api.response.PointGrantResponse;
import com.payment.point.api.response.PointUsageResponse;
import com.payment.point.application.service.PointService;
import com.payment.point.domain.allocation.PointAllocation;
import com.payment.point.domain.grant.PointGrantStatus;
import com.payment.point.domain.grant.PointGrantType;
import com.payment.point.infrastructure.persistence.allocation.PointAllocationEntity;
import com.payment.point.infrastructure.persistence.allocation.PointAllocationRepository;
import com.payment.point.infrastructure.persistence.grant.PointGrantEntity;
import com.payment.point.infrastructure.persistence.grant.PointGrantRepository;
import com.payment.point.infrastructure.persistence.usage.PointUsageRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class PointServiceTest {

    private static final AtomicLong counter = new AtomicLong();

    @Autowired
    private PointService pointService;

    @Autowired
    private PointGrantRepository pointGrantRepository;

    @Autowired
    private PointAllocationRepository pointAllocationRepository;

    @Autowired
    private PointUsageRepository pointUsageRepository;


    /** =========================
     *  Point Grant Tests
     *  ========================= */

    @Test
    @DisplayName("포인트를 정상적으로 적립한다")
    void grantPoint_success() {
        // given
        var request = new GrantPointRequest();
        var userId = nextId();
        setPointRequest(request, userId, 1000L, PointGrantType.AUTO, null);

        // when
        var response = pointService.grantPoint(request);

        // then
        assertThat(response.getGrantId()).isNotNull();
        assertThat(response.getGrantedAmount()).isEqualTo(1000L);
        assertThat(response.getRemainingAmount()).isEqualTo(1000L);
        assertThat(response.getGrantType()).isEqualTo(PointGrantType.AUTO);
    }

    @Test
    @DisplayName("개인 최대 보유 포인트를 초과하면 적립이 실패한다")
    void grantPoint_exceedMaxBalance() {
        // given
        var first = new GrantPointRequest();
        var id = nextId();
        setPointRequest(first, id, 90_000L, PointGrantType.AUTO, null);
        pointService.grantPoint(first);

        var second = new GrantPointRequest();
        setPointRequest(second, id, 90_000L, PointGrantType.AUTO, null);

        // when / then
        assertThatThrownBy(() -> pointService.grantPoint(second))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최대 보유 포인트");
    }

    @Test
    @DisplayName("관리자 수기 지급 포인트는 다른 적립과 구분되어 저장된다")
    void grantPoint_adminGrant() {
        // given
        GrantPointRequest request = new GrantPointRequest();
        setPointRequest(request, nextId(), 5000L, PointGrantType.ADMIN_GRANTED, null);

        // when
        var response = pointService.grantPoint(request);

        // then
        assertThat(response.getGrantType()).isEqualTo(PointGrantType.ADMIN_GRANTED);
    }

    @Test
    @DisplayName("만료일을 지정하지 않으면 기본 만료일 365일이 설정된다")
    void grantPoint_defaultExpireDate() {
        // given
        var request = new GrantPointRequest();
        setPointRequest(request, nextId(), 1000L, PointGrantType.AUTO, null);
        var beforeGrant = LocalDateTime.now();

        // when
        var response = pointService.grantPoint(request);

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
        var request = new GrantPointRequest();
        var expireDate = LocalDateTime.now().plusDays(30);

        setPointRequest(request, nextId(), 1000L, PointGrantType.AUTO, expireDate);
        var beforeGrant = LocalDateTime.now();

        // when
        var response = pointService.grantPoint(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getExpireDate()).isNotNull();
        assertThat(response.getExpireDate()).isAfter(beforeGrant);
    }

    @Test
    @DisplayName("포인트의 만료일은 최소 1일 이상이어야 하며, 그렇지 않으면 예외가 발생한다")
    void grantPoint_expireDate_lessThanOneDay_throwException() {
        // given
        var request = new GrantPointRequest();
        var invalidExpireDate = LocalDateTime.now().plusHours(23);

        setPointRequest(request, nextId(), 1000L, PointGrantType.AUTO, invalidExpireDate);

        // when & then
        assertThatThrownBy(() -> pointService.grantPoint(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("만료일은 최소");
    }

    @Test
    @DisplayName("포인트의 만료일은 최대 5년 미만이어야 하며, 이를 초과하면 예외가 발생한다")
    void grantPoint_expireDate_overFiveYears_throwException() {
        // given
        var request = new GrantPointRequest();
        var invalidExpireDate = LocalDateTime.now().plusYears(5);

        setPointRequest(request, nextId(), 1000L, PointGrantType.AUTO, invalidExpireDate);

        // when & then
        assertThatThrownBy(() -> pointService.grantPoint(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("만료일은 최대");
    }

    @Test
    @DisplayName("사용되지 않은 적립은 취소 가능하다")
    void cancelGrant_success() {
        // given
        var request = new GrantPointRequest();
        setPointRequest(request, nextId(), 1000L, PointGrantType.AUTO, null);

        var response = pointService.grantPoint(request);

        // when
        var cancelRequest = new CancelGrantRequest();
        var cancelResponse = pointService.cancelGrant(response.getGrantId(), cancelRequest);

        // then
        assertThat(cancelResponse.getGrantedAmount()).isEqualTo(1000L);
        assertThat(cancelResponse.getRemainingAmount()).isEqualTo(0L);
        assertThat(cancelResponse.getStatus()).isEqualTo(PointGrantStatus.CANCELED);
    }

    @Test
    @DisplayName("이미 사용된 적립은 취소할 수 없다")
    void cancelGrant_fail_whenUsed() {
        // given
        var request = new GrantPointRequest();
        setPointRequest(request, nextId(), 1000L, PointGrantType.AUTO, null);

        var response = pointService.grantPoint(request);

        // when
        var grantEntity = pointGrantRepository.findById(response.getGrantId())
                .orElseThrow();

        var grant = grantEntity.toDomain();
        grant.use(100);

        grantEntity.apply(grant);
        pointGrantRepository.save(grantEntity);

        // then
        var cancelRequest = new CancelGrantRequest();
        assertThatThrownBy(() -> pointService.cancelGrant(response.getGrantId(), cancelRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("취소 불가능한 적립 상태입니다.");
    }

    @Test
    @DisplayName("관리자 적립 포인트가 일반 적립보다 먼저 사용된다")
    void usePoint_adminGrantFirst() {
        // given
        var userId = nextId();
        var orderId = nextId();

        // 일반 적립 (만료일 30)
        var normal = new GrantPointRequest();
        setPointRequest(
                normal,
                userId,
                500L,
                PointGrantType.AUTO,
                LocalDateTime.now().plusDays(30)
        );
        pointService.grantPoint(normal);

        // 관리자 적립 (만료일 5)
        var admin = new GrantPointRequest();
        setPointRequest(
                admin,
                userId,
                300L,
                PointGrantType.ADMIN_GRANTED,
                LocalDateTime.now().plusDays(5)
        );
        pointService.grantPoint(admin);

        // when
        var use = new UsePointRequest();
        setUseRequest(use, userId, 300L, orderId);
        pointService.usePoint(use);

        // then
        List<PointGrantEntity> grants = pointGrantRepository.findAllByUserId(userId);

        // 관리자 300이 먼저 소진되어 나머지 500남음
        assertThat(grants.size()).isEqualTo(1L);
        assertThat(grants.getFirst().getRemainingAmount()).isEqualTo(500L);
        assertThat(grants.getFirst().getGrantType()).isEqualTo(PointGrantType.AUTO);
    }

    @Test
    @DisplayName("만료된 적립에서 사용한 포인트를 취소하면 신규 적립 처리된다")
    void cancelUsage_whenGrantExpired_createNewGrant() {
        // given
        var userId = nextId();
        var orderId = nextId();

        // 적립
        GrantPointRequest grantRequest = new GrantPointRequest();
        setPointRequest(
                grantRequest,
                userId,
                1000L,
                PointGrantType.AUTO,
                LocalDateTime.now().plusDays(2) // 사용 시점에는 만료 X
        );
        PointGrantResponse grantResponse = pointService.grantPoint(grantRequest);

        // 사용
        UsePointRequest useRequest = new UsePointRequest();
        setUseRequest(useRequest, userId, 500L, orderId);
        PointUsageResponse usageResponse = pointService.usePoint(useRequest);

        // 취소시점 기준 만료 처리
        PointGrantEntity usedGrant = pointGrantRepository
                .findById(grantResponse.getGrantId())
                .orElseThrow();

        usedGrant.setExpireDate(LocalDateTime.now().minusDays(1));
        pointGrantRepository.save(usedGrant);

        // when
        CancelUsageRequest cancelRequest = new CancelUsageRequest();
        setCancelUsageRequest(cancelRequest, 500L, orderId);
        pointService.cancelUsage(usageResponse.getUsageId(), cancelRequest);

        // then
        List<PointGrantEntity> expiredGranted = pointGrantRepository.findAll().stream()
                .filter(g -> g.getGrantType() == PointGrantType.EXPIRED_GRANTED)
                .toList();
        assertThat(expiredGranted).hasSize(1);
        assertThat(expiredGranted.getFirst().getGrantedAmount()).isEqualTo(500L);
        assertThat(expiredGranted.getFirst().getRemainingAmount()).isEqualTo(500L);

        PointAllocationEntity allocation = pointAllocationRepository.findById(userId).orElseThrow();
        assertThat(allocation.getBalance()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("부분 취소 후 남은 금액만 추가 취소할 수 있다")
    void cancelUsage_partial_then_cancelRemaining() {
        // given
        var userId = nextId();
        var orderId = nextId();

        var grant = new GrantPointRequest();
        setPointRequest(grant, userId, 1000L, PointGrantType.AUTO, null);
        pointService.grantPoint(grant);

        var use = new UsePointRequest();
        setUseRequest(use, userId, 800L, orderId);
        var usage = pointService.usePoint(use);

        // 1차 부분 취소 (500)
        var cancel1 = new CancelUsageRequest();
        setCancelUsageRequest(cancel1, 500L, orderId);
        pointService.cancelUsage(usage.getUsageId(), cancel1);

        // when / then
        // 2차 취소 (남은 300)
        var cancel2 = new CancelUsageRequest();
        setCancelUsageRequest(cancel2, 300L, orderId);

        assertThatCode(() -> pointService.cancelUsage(usage.getUsageId(), cancel2))
                .doesNotThrowAnyException();
    }



    /** =========================
     *  Point Usage Tests
     *  ========================= */

    @Test
    @DisplayName("보유 포인트 내에서 포인트를 정상적으로 사용한다")
    void usePoint_success() {
        // given
        var userId = nextId();
        var orderId = nextId();

        // 적립 포인트 생성
        var grantRequest = new GrantPointRequest();
        setPointRequest(grantRequest, userId, 1000L, PointGrantType.AUTO, null);
        pointService.grantPoint(grantRequest);

        var request = new UsePointRequest();
        setUseRequest(request, userId, 300L, orderId);

        // when
        var response = pointService.usePoint(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUsageId()).isNotNull();
        assertThat(response.getUsedAmount()).isEqualTo(300L);
        assertThat(response.getReason()).isNull();

        PointAllocationEntity updated = pointAllocationRepository.findById(userId).orElseThrow();
        assertThat(updated.getBalance()).isEqualTo(700L);
    }

    @Test
    @DisplayName("보유 포인트보다 많은 포인트를 사용하면 예외가 발생한다")
    void usePoint_fail_whenInsufficientBalance() {
        // given
        var userId = nextId();
        var orderId = nextId();

        var allocation = PointAllocation.create(userId, 200L);
        pointAllocationRepository.save(PointAllocationEntity.from(allocation));

        var request = new UsePointRequest();
        setUseRequest(request, userId, 500L, orderId);

        // when & then
        assertThatThrownBy(() -> pointService.usePoint(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("가용 포인트 잔액이 부족");
    }

    @Test
    @DisplayName("포인트 정보가 없는 사용자는 포인트를 사용할 수 없다")
    void usePoint_fail_whenAllocationNotExists() {
        // given
        var userId = nextId();
        var orderId = nextId();

        var request = new UsePointRequest();
        setUseRequest(request, userId, 100L, orderId);

        // when & then
        assertThatThrownBy(() -> pointService.usePoint(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("포인트 정보가 존재하지 않습니다");
    }

    @Test
    @DisplayName("사용한 포인트를 정상적으로 전액 취소한다")
    void cancelUsage_success_whenCancelAll() {
        // given
        var userId = nextId();
        var orderId = nextId();

        // 포인트 적립
        var request = new GrantPointRequest();
        setPointRequest(request, userId, 1000L, PointGrantType.AUTO, null);
        pointService.grantPoint(request);

        // 포인트 사용
        var useRequest = new UsePointRequest();
        setUseRequest(useRequest, userId, 600L, orderId);
        var usageResponse = pointService.usePoint(useRequest);

        // when
        // 사용 취소
        var cancelRequest = new CancelUsageRequest();
        setCancelUsageRequest(cancelRequest, 600L, orderId);

        var cancelResponse = pointService.cancelUsage(
                usageResponse.getUsageId(),
                cancelRequest
        );

        // then
        assertThat(cancelResponse.getUsedAmount()).isEqualTo(600L);

        PointAllocationEntity updated = pointAllocationRepository.findById(userId).orElseThrow();
        assertThat(updated.getBalance()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("사용한 포인트를 정상적으로 부분 취소한다")
    void cancelUsage_success_whenCancelPartial() {
        // given
        var userId = nextId();
        var orderId = nextId();

        // 포인트 적립
        var request = new GrantPointRequest();
        setPointRequest(request, userId, 1000L, PointGrantType.AUTO, null);
        pointService.grantPoint(request);

        // 포인트 사용
        var useRequest = new UsePointRequest();
        setUseRequest(useRequest, userId, 600L, orderId);
        var usageResponse = pointService.usePoint(useRequest);

        // when
        // 부분 취소 400
        var cancelRequest = new CancelUsageRequest();
        setCancelUsageRequest(cancelRequest, 400L, orderId);

        var cancelResponse = pointService.cancelUsage(
                usageResponse.getUsageId(),
                cancelRequest
        );

        // then
        assertThat(cancelResponse.getUsedAmount()).isEqualTo(400L);

        PointAllocationEntity updated = pointAllocationRepository.findById(userId).orElseThrow();
        assertThat(updated.getBalance()).isEqualTo(800L); // 1000 - 600 + 400
    }

    @Test
    @DisplayName("이미 취소된 사용은 다시 취소할 수 없다")
    void cancelUsage_fail_whenAlreadyCanceled() {
        // given
        var userId = nextId();
        var orderId = nextId();

        // 사용자 포인트 적립
        var allocation = PointAllocation.create(userId, 1000L);
        pointAllocationRepository.save(PointAllocationEntity.from(allocation));

        // 포인트 적립
        var request = new GrantPointRequest();
        setPointRequest(request, userId, 1000L, PointGrantType.AUTO, null);
        pointService.grantPoint(request);

        // 포인트 사용
        var useRequest = new UsePointRequest();
        setUseRequest(useRequest, userId, 200L, orderId);
        var usageResponse = pointService.usePoint(useRequest);

        // 포인트 사용 취소
        var cancelRequest = new CancelUsageRequest();
        setCancelUsageRequest(cancelRequest, 200L, orderId);
        pointService.cancelUsage(usageResponse.getUsageId(), cancelRequest);

        // when & then
        // 포인트 사용 재취소
        assertThatThrownBy(() -> pointService.cancelUsage(usageResponse.getUsageId(), cancelRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("취소 금액 오류");
    }



    private void setPointRequest(
            GrantPointRequest request,
            Long userId,
            Long amount,
            PointGrantType grantType,
            LocalDateTime expireDate
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

    private void setUseRequest(
            UsePointRequest request,
            Long userId,
            Long amount,
            Long orderId
    ) {
        try {
            var userIdField = UsePointRequest.class.getDeclaredField("userId");
            var amountField = UsePointRequest.class.getDeclaredField("amount");
            var orderIdField = UsePointRequest.class.getDeclaredField("orderId");

            userIdField.setAccessible(true);
            amountField.setAccessible(true);
            orderIdField.setAccessible(true);

            userIdField.set(request, userId);
            amountField.set(request, amount);
            orderIdField.set(request, orderId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setCancelUsageRequest(
            CancelUsageRequest request,
            Long cancelAmount,
            Long orderId
    ) {
        try {
            var cancelAmountField = CancelUsageRequest.class.getDeclaredField("cancelAmount");
            var orderIdField = CancelUsageRequest.class.getDeclaredField("orderId");

            cancelAmountField.setAccessible(true);
            orderIdField.setAccessible(true);

            cancelAmountField.set(request, cancelAmount);
            orderIdField.set(request, orderId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private long nextId() {
        return counter.incrementAndGet();
    }

}