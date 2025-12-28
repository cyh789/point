package com.payment.point.application;

import com.payment.point.api.request.GrantPointRequest;
import com.payment.point.api.response.PointGrantResponse;
import com.payment.point.common.policy.PointPolicy;
import com.payment.point.domain.model.PointGrant;
import com.payment.point.infrastructure.persistence.entity.PointGrantEntity;
import com.payment.point.infrastructure.persistence.repository.PointGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PointCommandService {

    private final PointGrantRepository pointGrantRepository;
    private final PointPolicy pointPolicy;

    @Transactional
    public PointGrantResponse grantPoint(GrantPointRequest request) {

        // 정책 검증
        pointPolicy.validateGrantAmount(request.getAmount());

        int currentBalance = pointGrantRepository.sumRemainingAmountByUserId(request.getUserId());
        pointPolicy.validateMaxBalance(currentBalance + request.getAmount());

        // 만료일 결정
        LocalDateTime expireAt = request.getExpireAt() != null
                ? request.getExpireAt()
                : LocalDateTime.now().plusDays(pointPolicy.getDefaultExpireDays());

        // 도메인 객체 생성
        PointGrant grant = PointGrant.create(
                request.getUserId(),
                request.getAmount(),
                request.getGrantType(),
                expireAt
        );

        // 저장
        PointGrantEntity entity = PointGrantEntity.from(grant);
        PointGrantEntity saved = pointGrantRepository.save(entity);

        return new PointGrantResponse(
                saved.getId(),
                saved.getTotalAmount(),
                saved.getRemainingAmount(),
                saved.getGrantType(),
                saved.getExpireAt()
        );
    }
}

