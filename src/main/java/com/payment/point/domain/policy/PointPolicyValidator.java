package com.payment.point.domain.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PointPolicyValidator {

    private final PointPolicyReader policyReader;

    public void validateGrantAmount(long grantAmount) {
        long maxGrantAmount = Long.parseLong(policyReader.getMaxGrantAmount());

        if (grantAmount <= 0 || grantAmount > maxGrantAmount) {
            throw new IllegalArgumentException("1회 적립 한도를 초과했습니다.");
        }
    }

    public void validateMaxPointBalance(long currentBalance, long grantAmount) {
        long maxBalance = Long.parseLong(policyReader.getMaxPointBalance());

        if (currentBalance + grantAmount > maxBalance) {
            throw new IllegalArgumentException("개인 최대 보유 포인트를 초과했습니다.");
        }
    }

    public void validateExpireAt(LocalDateTime expireAt) {
        if (expireAt == null) {
            throw new IllegalArgumentException("만료일은 필수입니다.");
        }

        LocalDateTime now = LocalDateTime.now();

        int minDays = Integer.parseInt(policyReader.getMinExpireDays());
        int maxDays = Integer.parseInt(policyReader.getMaxExpireDays());

        LocalDateTime minExpireAt = now.plusDays(minDays);
        LocalDateTime maxExpireAt = now.plusDays(maxDays);

        if (expireAt.isBefore(minExpireAt)) {
            throw new IllegalArgumentException(
                    "만료일은 최소 " + minDays + "일 이상이어야 합니다."
            );
        }

        if (!expireAt.isBefore(maxExpireAt)) {
            throw new IllegalArgumentException(
                    "만료일은 최대 " + maxDays + "일 미만이어야 합니다."
            );
        }
    }
}
