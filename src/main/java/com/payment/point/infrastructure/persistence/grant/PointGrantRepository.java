package com.payment.point.infrastructure.persistence.grant;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PointGrantRepository extends JpaRepository<PointGrantEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT pg
        FROM PointGrantEntity pg
        WHERE pg.userId = :userId
          AND pg.remainingAmount > 0
          AND pg.status IN (
                com.payment.point.domain.grant.PointGrantStatus.GRANTED,
                com.payment.point.domain.grant.PointGrantStatus.USED_PARTIAL
          )
          AND pg.expireDate > CURRENT_TIMESTAMP
        ORDER BY CASE WHEN grantType = com.payment.point.domain.grant.PointGrantType.ADMIN_GRANTED THEN 0 ELSE 1 END,
           pg.expireDate ASC,
           pg.grantId ASC
    """)
    List<PointGrantEntity> findAllByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pg FROM PointGrantEntity pg WHERE pg.grantId = :grantId")
    Optional<PointGrantEntity> findByIdForUpdate(Long grantId);
}
