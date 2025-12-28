package com.payment.point.infrastructure.persistence.policy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_policy")
@Getter
@NoArgsConstructor
public class PointPolicyEntity {

    @Id
    @Column(name = "policy_key", nullable = false)
    private String policyKey;

    @Column(name = "policy_value", nullable = false)
    private String policyValue;
}
