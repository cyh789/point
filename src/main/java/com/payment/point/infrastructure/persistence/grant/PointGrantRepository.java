package com.payment.point.infrastructure.persistence.grant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PointGrantRepository extends JpaRepository<PointGrantEntity, Long> {

    @Query("""
        SELECT COALESCE(SUM(pg.remainingAmount), 0)
        FROM PointGrantEntity pg
        WHERE pg.userId = :userId
          AND pg.expireDate > CURRENT_TIMESTAMP
    """)
    int sumRemainingAmountByUserId(Long userId);
}
