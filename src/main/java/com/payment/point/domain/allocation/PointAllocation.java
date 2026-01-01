package com.payment.point.domain.allocation;

import jakarta.persistence.Id;
import lombok.Getter;

@Getter
public class PointAllocation {

    @Id
    private Long userId;

    private long balance;

    public static PointAllocation create(Long userId, long balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("초기 잔액은 0 이상이어야 합니다.");
        }
        PointAllocation entity = new PointAllocation();
        entity.userId = userId;
        entity.balance = balance;
        return entity;
    }
}
