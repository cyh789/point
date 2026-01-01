package com.payment.point.domain.grant;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PointGrant {

    private final Long grantId;
    private final Long userId;
    private final long grantedAmount;
    private long remainingAmount;
    private PointGrantType grantType;
    private final LocalDateTime expireDate;
    private PointGrantStatus status;
    private String reason;

    private PointGrant(
            Long grantId,
            Long userId,
            long grantedAmount,
            long remainingAmount,
            PointGrantType grantType,
            LocalDateTime expireDate,
            PointGrantStatus status,
            String reason
    ) {
        this.grantId = grantId;
        this.userId = userId;
        this.grantedAmount = grantedAmount;
        this.remainingAmount = remainingAmount;
        this.grantType = grantType;
        this.expireDate = expireDate;
        this.status = status;
        this.reason = reason;
    }

    public static PointGrant create(
            Long userId,
            long amount,
            PointGrantType grantType,
            LocalDateTime expireDate
    ) {
        if (amount <= 0) {
            throw new IllegalArgumentException("적립 금액은 0보다 커야 합니다.");
        }

        return new PointGrant(
                null,
                userId,
                amount,
                amount,
                grantType,
                expireDate,
                PointGrantStatus.GRANTED,
                null
        );
    }

    /* 복원용 (Entity → Domain) */
    public static PointGrant restore(
            Long grantId,
            Long userId,
            long grantedAmount,
            long remainingAmount,
            PointGrantType grantType,
            LocalDateTime expireDate,
            PointGrantStatus status,
            String reason
    ) {
        return new PointGrant(
                grantId,
                userId,
                grantedAmount,
                remainingAmount,
                grantType,
                expireDate,
                status,
                reason
        );
    }

    /* 적립 취소 가능 여부 */
    public boolean canCancel() {
        return status == PointGrantStatus.GRANTED && !isExpired();
    }

    public void cancel(String reason) {
        if (!canCancel()) {
            throw new IllegalStateException("취소 불가능한 적립 상태입니다.");
        }
        this.status = PointGrantStatus.CANCELED;
        this.remainingAmount = 0;
        this.reason = reason;
    }

    public void use(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("사용 금액은 0보다 커야 합니다.");
        }
        if (isExpired()) {
            throw new IllegalStateException("만료된 포인트는 사용할 수 없습니다.");
        }
        if (this.remainingAmount < amount) {
            throw new IllegalStateException("사용 가능 포인트가 부족합니다.");
        }

        this.remainingAmount -= amount;
        if (this.remainingAmount == 0) {
            this.status = PointGrantStatus.USED_ALL;
        } else {
            this.status = PointGrantStatus.USED_PARTIAL;
        }
    }

    public void restoreAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("복원 금액은 0보다 커야 합니다.");
        }
        if (this.status == PointGrantStatus.CANCELED) {
            throw new IllegalStateException("취소된 적립은 복원할 수 없습니다.");
        }

        this.remainingAmount += amount;
        if (this.remainingAmount == this.grantedAmount) {
            this.status = PointGrantStatus.GRANTED;
        } else {
            this.status = PointGrantStatus.USED_PARTIAL;
        }
    }

    public boolean isExpired() {
        return expireDate.isBefore(LocalDateTime.now());
    }
}
