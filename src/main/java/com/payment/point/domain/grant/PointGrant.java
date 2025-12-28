package com.payment.point.domain.grant;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PointGrant {

    private final Long userId;
    private final long grantedAmount;
    private long remainingAmount;
    private final String grantType;
    private final LocalDateTime expireDate;
    private PointGrantStatus status;
    private String reason;

    private PointGrant(
            Long userId,
            long grantedAmount,
            String grantType,
            LocalDateTime expireDate,
            PointGrantStatus status
    ) {
        this.userId = userId;
        this.grantedAmount = grantedAmount;
        this.remainingAmount = grantedAmount;
        this.grantType = grantType;
        this.expireDate = expireDate;
        this.status = status;
    }

    public static PointGrant create(
            Long userId,
            long amount,
            String grantType,
            LocalDateTime expireDate
    ) {
        return new PointGrant(userId, amount, grantType, expireDate, PointGrantStatus.GRANTED);
    }

    public static PointGrant create(
            Long userId,
            long amount,
            String grantType,
            LocalDateTime expireDate,
            String status
    ) {
        return new PointGrant(userId, amount, grantType, expireDate, status == null ? PointGrantStatus.GRANTED : PointGrantStatus.valueOf(status));
    }

    public boolean canBeCanceled() {
        return this.status == PointGrantStatus.GRANTED;
    }

    public void cancel(String reason) {
        if (!canBeCanceled()) {
            throw new IllegalStateException("취소 불가능한 적립 상태");
        }
        this.status = PointGrantStatus.CANCELED;
        this.remainingAmount = 0;
        this.reason = reason;
    }

    public void use(long amount) {
        if (this.remainingAmount < amount) {
            throw new IllegalArgumentException("사용 가능 포인트 부족");
        }

        this.remainingAmount -= amount;

        if (remainingAmount == 0) {
            this.status = PointGrantStatus.USED_ALL;
        } else {
            this.status = PointGrantStatus.USED_PARTIAL;
        }
    }
}
