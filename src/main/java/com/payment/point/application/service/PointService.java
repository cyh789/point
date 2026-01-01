package com.payment.point.application.service;

import com.payment.point.api.request.CancelGrantRequest;
import com.payment.point.api.request.CancelUsageRequest;
import com.payment.point.api.request.GrantPointRequest;
import com.payment.point.api.request.UsePointRequest;
import com.payment.point.api.response.PointGrantResponse;
import com.payment.point.api.response.PointUsageResponse;
import com.payment.point.domain.allocation.PointUsageAllocation;
import com.payment.point.domain.grant.PointGrant;
import com.payment.point.domain.grant.PointGrantType;
import com.payment.point.domain.policy.PointPolicyReader;
import com.payment.point.domain.policy.PointPolicyValidator;
import com.payment.point.domain.usage.PointUsage;
import com.payment.point.infrastructure.persistence.allocation.PointAllocationEntity;
import com.payment.point.infrastructure.persistence.allocation.PointAllocationRepository;
import com.payment.point.infrastructure.persistence.allocation.PointUsageAllocationEntity;
import com.payment.point.infrastructure.persistence.allocation.PointUsageAllocationRepository;
import com.payment.point.infrastructure.persistence.grant.PointGrantEntity;
import com.payment.point.infrastructure.persistence.grant.PointGrantRepository;
import com.payment.point.infrastructure.persistence.usage.PointUsageEntity;
import com.payment.point.infrastructure.persistence.usage.PointUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final PointGrantRepository pointGrantRepository;
    private final PointPolicyValidator pointPolicyValidator;
    private final PointPolicyReader policyReader;
    private final PointUsageRepository pointUsageRepository;
    private final PointAllocationRepository pointAllocationRepository;
    private final PointUsageAllocationRepository pointUsageAllocationRepository;

    public PointGrantResponse grantPoint(GrantPointRequest request) {

        // 정책 검증
        pointPolicyValidator.validateGrantAmount(request.getAmount());

        // 개인 최대 보유 포인트
        PointAllocationEntity allocation =
                pointAllocationRepository.findByUserIdForUpdate(request.getUserId())
                        .orElseGet(() ->
                                pointAllocationRepository.save(PointAllocationEntity.from(request.getUserId(), 0))
                        );
        pointPolicyValidator.validateMaxPointBalance(
                allocation.getBalance(),
                request.getAmount()
        );

        LocalDateTime expireDate = request.getExpireDate() != null
                ? request.getExpireDate()
                : LocalDateTime.now().plusDays(Long.parseLong(policyReader.getDefaultExpireDays()));
        pointPolicyValidator.validateExpireDate(expireDate);

        PointGrant grant = PointGrant.create(
                request.getUserId(),
                request.getAmount(),
                request.getGrantType(),
                expireDate
        );

        // 적립
        PointGrantEntity savedGrant = pointGrantRepository.save(PointGrantEntity.from(grant));

        // 적립 후 잔액 증가
        allocation.increase(request.getAmount());
        pointAllocationRepository.save(allocation);

        return new PointGrantResponse(
                savedGrant.getGrantId(),
                savedGrant.getUserId(),
                savedGrant.getGrantedAmount(),
                savedGrant.getRemainingAmount(),
                savedGrant.getGrantType(),
                savedGrant.getExpireDate(),
                savedGrant.getStatus(),
                savedGrant.getReason()
        );
    }

    public PointGrantResponse cancelGrant(Long grantId, CancelGrantRequest request) {

        PointGrantEntity grantEntity = pointGrantRepository.findByIdForUpdate(grantId)
                .orElseThrow(() -> new IllegalArgumentException("적립 내역이 없습니다."));

        // 사용 이력 존재 여부 확인
        boolean used = pointUsageAllocationRepository.existsByGrantId(grantId);
        if (used) {
            throw new IllegalStateException("취소 불가 거래");
        }

        PointGrant grant = grantEntity.toDomain();
        grant.cancel(request.getReason());

        grantEntity.apply(grant);
        PointGrantEntity saved = pointGrantRepository.save(grantEntity);

        // 잔액 차감
        PointAllocationEntity allocation =
                pointAllocationRepository.findByUserIdForUpdate(grant.getUserId())
                        .orElseThrow();
        allocation.decrease(grant.getGrantedAmount());
        pointAllocationRepository.save(allocation);

        return new PointGrantResponse(
                saved.getGrantId(),
                saved.getUserId(),
                saved.getGrantedAmount(),
                saved.getRemainingAmount(),
                saved.getGrantType(),
                saved.getExpireDate(),
                saved.getStatus(),
                saved.getReason()
        );
    }

    public PointUsageResponse usePoint(UsePointRequest request) {

        // 사용자 잔액 조회
        PointAllocationEntity allocation = pointAllocationRepository.findByUserIdForUpdate(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("포인트 정보가 존재하지 않습니다."));

        // 정책 검증
        pointPolicyValidator.validateUseAmount(request.getAmount(), allocation.getBalance());

        PointUsage usage = PointUsage.create(
                request.getUserId(),
                request.getAmount(),
                request.getOrderId()
        );

        // 포인트 사용내역 저장
        PointUsageEntity usageEntity = pointUsageRepository.save(PointUsageEntity.from(usage));

        // 사용 가능한 적립 조회
        List<PointGrantEntity> grants = pointGrantRepository.findAllByUserId(request.getUserId());

        long remaining = request.getAmount();
        for (PointGrantEntity grantEntity : grants) {
            if (remaining == 0) break;

            PointGrant grant = grantEntity.toDomain();
            long used = Math.min(grant.getRemainingAmount(), remaining);

            grant.use(used);
            grantEntity.apply(grant);

            PointUsageAllocation usageAllocation = PointUsageAllocation.create(
                    usageEntity.getUsageId(),
                    grantEntity.getGrantId(),
                    used
            );

            pointUsageAllocationRepository.save(PointUsageAllocationEntity.from(usageAllocation));

            remaining -= used;
        }

        if (remaining > 0) {
            throw new IllegalStateException("취소 불가 거래");
        }

        // 포인트 차감
        allocation.use(request.getAmount());
        pointAllocationRepository.save(allocation);

        usageEntity.complete();
        pointUsageRepository.save(usageEntity);

        return new PointUsageResponse(
                usageEntity.getUsageId(),
                usageEntity.getUserId(),
                request.getAmount(),
                usageEntity.getStatus(),
                null
        );
    }

    public PointUsageResponse cancelUsage(Long usageId, CancelUsageRequest request) {

        // 사용 내역 조회
        PointUsageEntity usageEntity = pointUsageRepository.findById(usageId)
                .orElseThrow(() -> new IllegalArgumentException("포인트 정보가 존재하지 않습니다."));

        long cancelAmount = request.getCancelAmount();
        long remainingAmount = usageEntity.getAmount() - usageEntity.getCanceledAmount();
        if (cancelAmount <= 0 || cancelAmount > remainingAmount) {
            throw new IllegalArgumentException("취소 금액 오류");
        }

        // 사용 시 할당된 적립 내역 조회
        List<PointUsageAllocationEntity> allocations = pointUsageAllocationRepository.findByUsageIdOrderByCreatedAtAsc(usageId);

        // 사용자 포인트 잔액 조회
        PointAllocationEntity allocation = pointAllocationRepository.findByUserIdForUpdate(usageEntity.getUserId())
                .orElseThrow();

        //적립 단위로 포인트 복원
        long remaining = cancelAmount;
        for (PointUsageAllocationEntity allocationEntity : allocations) {
            if (remaining == 0) break;

            // 동일 allocation에 대해 중복 취소 확인
            long allocRemain = allocationEntity.getAmount() - allocationEntity.getCanceledAmount();
            if (allocRemain <= 0) continue;


            long restoreAmount = Math.min(allocRemain, remaining);
            PointGrantEntity grantEntity = pointGrantRepository.findByIdForUpdate(allocationEntity.getGrantId())
                    .orElseThrow();

            PointGrant grant = grantEntity.toDomain();

            //만료 여부 판단
            if (grant.isExpired()) {
                // 만료된 포인트인 경우, 신규 적립 처리
                pointPolicyValidator.validateMaxPointBalance(
                        allocation.getBalance(),
                        restoreAmount
                );

                PointGrant restored = PointGrant.create(
                        usageEntity.getUserId(),
                        restoreAmount,
                        PointGrantType.EXPIRED_GRANTED,
                        LocalDateTime.now().plusDays(
                                Long.parseLong(policyReader.getDefaultExpireDays())
                        )
                );
                pointGrantRepository.save(PointGrantEntity.from(restored));
            } else {
                // 만료되지 않은 적립 → 기존 적립 복원
                grant.restoreAmount(restoreAmount);
                grantEntity.apply(grant);
                pointGrantRepository.save(grantEntity);
            }

            allocationEntity.cancel(restoreAmount);
            remaining -= restoreAmount;
        }

        //사용자 전체 포인트 잔액 복원
        allocation.increase(cancelAmount);
        pointAllocationRepository.save(allocation);

        //사용 도메인 & 엔티티 취소 처리
        usageEntity.cancel(cancelAmount, request.getReason());
        pointUsageRepository.save(usageEntity);

        return new PointUsageResponse(
                usageId,
                usageEntity.getUserId(),
                cancelAmount,
                usageEntity.getStatus(),
                request.getReason()
        );
    }
}
