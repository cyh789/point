package com.payment.point.infrastructure.persistence.allocation;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PointAllocationRepository extends JpaRepository<PointAllocationEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pa FROM PointAllocationEntity pa WHERE pa.userId = :userId")
    Optional<PointAllocationEntity> findByUserIdForUpdate(Long userId);
}
