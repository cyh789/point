package com.payment.point.domain.usage;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PointUsage {

    private final Long usageId;
    private final Long userId;
    private final long amount;
    private long canceledAmount;
    private final LocalDateTime usedAt;
    private LocalDateTime canceledAt;
    private PointUsageStatus status;
    private String reason;
    private Long orderId;

    private PointUsage(
            Long usageId,
            Long userId,
            long amount,
            long canceledAmount,
            LocalDateTime usedAt,
            Long orderId
    ) {
        this.usageId = usageId;
        this.userId = userId;
        this.amount = amount;
        this.canceledAmount = canceledAmount;
        this.usedAt = usedAt;
        this.orderId = orderId;
    }

    public static PointUsage create(Long userId, long amount, Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("주문번호는 필수입니다.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("사용 포인트는 0보다 커야 합니다.");
        }

        return new PointUsage(
                null,
                userId,
                amount,
                0L,
                LocalDateTime.now(),
                orderId
        );
    }
}
