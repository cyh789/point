package com.payment.point.common.policy;

import org.springframework.stereotype.Component;

@Component
public class PointPolicy {

    private int maxGrantPerOnce = 100_000;
    private int maxBalancePerUser = 150_000;
    private int defaultExpireDays = 365;

    public void validateGrantAmount(int amount) {
        if (amount < 1 || amount > maxGrantPerOnce) {
            throw new IllegalArgumentException("1회 적립 한도를 초과했습니다.");
        }
    }

    public void validateMaxBalance(int balanceAfterGrant) {
        if (balanceAfterGrant > maxBalancePerUser) {
            throw new IllegalArgumentException("개인 최대 보유 포인트를 초과했습니다.");
        }
    }

    public int getDefaultExpireDays() {
        return defaultExpireDays;
    }
}

