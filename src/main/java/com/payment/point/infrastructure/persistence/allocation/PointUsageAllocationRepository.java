package com.payment.point.infrastructure.persistence.allocation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PointUsageAllocationRepository extends JpaRepository<PointUsageAllocationEntity, Long> {

    List<PointUsageAllocationEntity> findByUsageIdOrderByCreatedAtAsc(Long usageId);

    @Query("""
        SELECT COUNT(pua) > 0
        FROM PointUsageAllocationEntity pua
        WHERE pua.grantId = :grantId
          AND pua.amount > pua.canceledAmount
    """)
    boolean existsByGrantId(Long grantId);
}
