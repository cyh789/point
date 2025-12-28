package com.payment.point.infrastructure.persistence.policy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointPolicyRepository
        extends JpaRepository<PointPolicyEntity, String> {

    Optional<PointPolicyEntity> findByPolicyKey(String policyKey);
}
