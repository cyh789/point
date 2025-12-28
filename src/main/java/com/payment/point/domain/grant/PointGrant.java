package com.payment.point.domain.grant;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PointGrant {

    private final Long userId;
    private final int totalAmount;
    private int remainingAmount;
    private final String grantType;
    private final LocalDateTime expireAt;

    private PointGrant(
            Long userId,
            int amount,
            String grantType,
            LocalDateTime expireAt
    ) {
        this.userId = userId;
        this.totalAmount = amount;
        this.remainingAmount = amount;
        this.grantType = grantType;
        this.expireAt = expireAt;
    }

    public static PointGrant create(
            Long userId,
            int amount,
            String grantType,
            LocalDateTime expireAt
    ) {
        return new PointGrant(userId, amount, grantType, expireAt);
    }

    public boolean isExpired(LocalDateTime now) {
        return expireAt.isBefore(now);
    }
}
