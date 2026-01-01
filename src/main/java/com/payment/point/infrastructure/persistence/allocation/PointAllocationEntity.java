package com.payment.point.infrastructure.persistence.allocation;

import com.payment.point.domain.allocation.PointAllocation;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_allocation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointAllocationEntity {

    @Id
    private Long userId;

    private long balance;

    public void increase(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("금액 오류");
        this.balance += amount;
    }

    public void decrease(long amount) {
        if (amount <= 0 || balance < amount) throw new IllegalStateException("금액 오류");
        this.balance -= amount;
    }

    public void use(long amount) {
        if (this.balance < amount) {
            throw new IllegalStateException("가용 포인트 잔액이 부족합니다.");
        }
        this.balance -= amount;
    }

    public static PointAllocationEntity from(Long userId, long balance) {
        PointAllocationEntity entity = new PointAllocationEntity();
        entity.userId = userId;
        entity.balance = balance;
        return entity;
    }

    public static PointAllocationEntity from(PointAllocation allocation) {
        return from(allocation.getUserId(), allocation.getBalance());
    }
}
