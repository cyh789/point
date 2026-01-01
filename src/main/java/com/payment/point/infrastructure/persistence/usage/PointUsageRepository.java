package com.payment.point.infrastructure.persistence.usage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PointUsageRepository extends JpaRepository<PointUsageEntity, Long> {
}
