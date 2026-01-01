package com.payment.point.domain.allocation;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PointUsageAllocation {

    @Id
    @GeneratedValue
    private Long id;

    private Long usageId;
    private Long grantId;
    private long amount;
    private long canceledAmount;

    private LocalDateTime createdAt;

    private PointUsageAllocation(
            Long usageId,
            Long grantId,
            long amount
    ) {
        if (amount <= 0) {
            throw new IllegalArgumentException("할당 금액 오류");
        }

        this.usageId = usageId;
        this.grantId = grantId;
        this.amount = amount;
        this.canceledAmount = 0L;
        this.createdAt = LocalDateTime.now();
    }

    public static PointUsageAllocation create(
            Long usageId,
            Long grantId,
            long amount
    ) {
        return new PointUsageAllocation(usageId, grantId, amount);
    }
}
