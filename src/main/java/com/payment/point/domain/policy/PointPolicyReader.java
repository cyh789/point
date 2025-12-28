package com.payment.point.domain.policy;

import com.payment.point.infrastructure.persistence.policy.PointPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointPolicyReader {

    private final PointPolicyRepository repository;

    public String getMaxPointBalance() {
        return getPolicyValue(PointPolicyType.MAX_POINT_BALANCE);
    }

    public String getMaxGrantAmount() {
        return getPolicyValue(PointPolicyType.MAX_GRANT_AMOUNT);
    }

    public String getDefaultExpireDays() {
        return getPolicyValue(PointPolicyType.DEFAULT_EXPIRE_DAYS);
    }

    public String getMinExpireDays() {
        return getPolicyValue(PointPolicyType.MIN_EXPIRE_DAYS);
    }

    public String getMaxExpireDays() {
        return getPolicyValue(PointPolicyType.MAX_EXPIRE_DAYS);
    }

    private String getPolicyValue(PointPolicyType type) {
        return repository.findByPolicyKey(type.name())
                .orElseThrow(() ->
                        new IllegalStateException("포인트 정책이 존재하지 않습니다: " + type.name())
                )
                .getPolicyValue();
    }

}

