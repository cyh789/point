package com.payment.point.infrastructure.persistence.usage;

import com.payment.point.domain.usage.PointUsage;
import com.payment.point.domain.usage.PointUsageStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_usage")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointUsageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usageId;

    private Long userId;
    private long amount;
    private long canceledAmount;
    private LocalDateTime usedAt;
    private LocalDateTime canceledAt;

    @Enumerated(EnumType.STRING)
    private PointUsageStatus status;

    private String reason;
    private Long orderId;

    public static PointUsageEntity from(PointUsage usage) {
        PointUsageEntity entity = new PointUsageEntity();
        entity.userId = usage.getUserId();
        entity.amount = usage.getAmount();
        entity.canceledAmount = 0;
        entity.orderId = usage.getOrderId();
        entity.usedAt = LocalDateTime.now();
        entity.status = usage.getStatus();
        return entity;
    }

    public void cancel(long cancelAmount, String reason) {
        if (cancelAmount <= 0) {
            throw new IllegalArgumentException("취소 금액은 0보다 커야 합니다.");
        }
        this.canceledAmount += cancelAmount;
        this.canceledAt = LocalDateTime.now();
        this.reason = reason;
        if (canceledAmount == amount) {
            this.status = PointUsageStatus.CANCELED;
        } else {
            this.status = PointUsageStatus.PARTIALLY_CANCELED;
        }
    }

    public void complete() {
        this.status = PointUsageStatus.USED;
        this.usedAt = LocalDateTime.now();
    }
}
