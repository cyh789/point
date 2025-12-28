package com.payment.point.application.service;

import com.payment.point.api.request.CancelGrantRequest;
import com.payment.point.api.request.GrantPointRequest;
import com.payment.point.api.response.PointGrantResponse;
import com.payment.point.domain.grant.PointGrant;
import com.payment.point.domain.policy.PointPolicyReader;
import com.payment.point.domain.policy.PointPolicyValidator;
import com.payment.point.infrastructure.persistence.grant.PointGrantEntity;
import com.payment.point.infrastructure.persistence.grant.PointGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PointCommandService {

    private final PointGrantRepository pointGrantRepository;
    private final PointPolicyValidator pointPolicyValidator;
    private final PointPolicyReader policyReader;


    @Transactional
    public PointGrantResponse grantPoint(GrantPointRequest request) {

        // 정책 검증
        pointPolicyValidator.validateGrantAmount(request.getAmount());

        int currentBalance = pointGrantRepository.sumRemainingAmountByUserId(request.getUserId());
        pointPolicyValidator.validateMaxPointBalance(currentBalance, request.getAmount());

        LocalDateTime expireDate = request.getExpireDate() != null
                ? request.getExpireDate()
                : LocalDateTime.now().plusDays(Long.parseLong(policyReader.getDefaultExpireDays()));
        pointPolicyValidator.validateExpireDate(expireDate);

        // 도메인 객체 생성
        PointGrant grant = PointGrant.create(
                request.getUserId(),
                request.getAmount(),
                request.getGrantType(),
                expireDate
        );

        // 저장
        PointGrantEntity entity = PointGrantEntity.from(grant);
        PointGrantEntity saved = pointGrantRepository.save(entity);

        return new PointGrantResponse(
                saved.getId(),
                saved.getGrantedAmount(),
                saved.getRemainingAmount(),
                saved.getGrantType(),
                saved.getExpireDate(),
                saved.getStatus(),
                saved.getReason()
        );
    }

    public PointGrantResponse cancelGrant(Long grantId, CancelGrantRequest request) {
        PointGrantEntity entity = pointGrantRepository.findById(grantId)
                .orElseThrow(() -> new IllegalArgumentException("PointGrant not found"));

        PointGrant grant = entity.toDomain();

        if (!grant.canBeCanceled()) {
            throw new IllegalStateException("이미 사용되었거나 만료된 적립입니다.");
        }

        grant.cancel(request.getReason());

        entity.cancel(request.getReason());
        PointGrantEntity saved = pointGrantRepository.save(entity);

        return new PointGrantResponse(
                saved.getId(),
                saved.getGrantedAmount(),
                saved.getRemainingAmount(),
                saved.getGrantType(),
                saved.getExpireDate(),
                saved.getStatus(),
                saved.getReason()
        );
    }

    // TODO usePoint()
    // TODO cancelUsage()
}

