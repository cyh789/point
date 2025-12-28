package com.payment.point.infrastructure.persistence.entity;

import com.payment.point.domain.model.PointGrant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_grant")
@Getter
@NoArgsConstructor
public class PointGrantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private int totalAmount;
    private int remainingAmount;
    private String grantType;
    private LocalDateTime expireAt;

    public static PointGrantEntity from(PointGrant grant) {
        PointGrantEntity entity = new PointGrantEntity();
        entity.userId = grant.getUserId();
        entity.totalAmount = grant.getTotalAmount();
        entity.remainingAmount = grant.getRemainingAmount();
        entity.grantType = grant.getGrantType();
        entity.expireAt = grant.getExpireAt();
        return entity;
    }
}
