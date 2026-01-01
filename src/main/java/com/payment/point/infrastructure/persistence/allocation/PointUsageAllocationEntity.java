package com.payment.point.infrastructure.persistence.allocation;

import com.payment.point.domain.allocation.PointUsageAllocation;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_usage_allocation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointUsageAllocationEntity {

    @Id
    @GeneratedValue
    private Long id;

    private Long usageId;
    private Long grantId;
    private long amount;
    private long canceledAmount;

    private LocalDateTime createdAt;

    public static PointUsageAllocationEntity from(PointUsageAllocation allocation) {
        if (allocation.getUsageId() == null || allocation.getGrantId() == null) {
            throw new IllegalArgumentException("필수 ID 값 누락");
        }
        if (allocation.getAmount() <= 0) {
            throw new IllegalArgumentException("할당 금액은 0보다 커야 합니다.");
        }

        PointUsageAllocationEntity entity = new PointUsageAllocationEntity();
        entity.usageId = allocation.getUsageId();
        entity.grantId = allocation.getGrantId();
        entity.amount = allocation.getAmount();
        entity.canceledAmount = allocation.getCanceledAmount();
        entity.createdAt = allocation.getCreatedAt() != null ? allocation.getCreatedAt() : LocalDateTime.now();
        return entity;
    }

    public void cancel(long amount) {
        if (this.canceledAmount + amount > this.amount) {
            throw new IllegalStateException("취소 초과");
        }
        this.canceledAmount += amount;
    }
}
