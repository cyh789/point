package com.payment.point.infrastructure.persistence.grant;

import com.payment.point.domain.grant.PointGrant;
import com.payment.point.domain.grant.PointGrantStatus;
import com.payment.point.domain.grant.PointGrantType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_grant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointGrantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long grantId;

    private Long userId;
    private long grantedAmount;
    private long remainingAmount;
    private PointGrantType grantType;
    private LocalDateTime expireDate;
    private PointGrantStatus status;
    private String reason;

    public static PointGrantEntity from(PointGrant grant) {
        PointGrantEntity entity = new PointGrantEntity();
        entity.userId = grant.getUserId();
        entity.grantedAmount = grant.getGrantedAmount();
        entity.remainingAmount = grant.getRemainingAmount();
        entity.grantType = grant.getGrantType();
        entity.expireDate = grant.getExpireDate();
        entity.status = grant.getStatus();
        entity.reason = grant.getReason();
        return entity;
    }

    public PointGrant toDomain() {
        return PointGrant.restore(
                grantId,
                userId,
                grantedAmount,
                remainingAmount,
                grantType,
                expireDate,
                status,
                reason
        );
    }

    public void apply(PointGrant grant) {
        this.remainingAmount = grant.getRemainingAmount();
        this.status = grant.getStatus();
        this.reason = grant.getReason();
    }

    public void setExpireDate(LocalDateTime expireDate) {
        this.expireDate = expireDate;
    }
}
