package com.payment.point.infrastructure.persistence.grant;

import com.payment.point.domain.grant.PointGrant;
import com.payment.point.domain.grant.PointGrantStatus;
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
    private long grantedAmount;
    private long remainingAmount;
    private String grantType;
    private LocalDateTime expireDate;
    private String status;
    private String reason;

    public static PointGrantEntity from(PointGrant grant) {
        PointGrantEntity entity = new PointGrantEntity();
        entity.userId = grant.getUserId();
        entity.grantedAmount = grant.getGrantedAmount();
        entity.remainingAmount = grant.getRemainingAmount();
        entity.grantType = grant.getGrantType();
        entity.expireDate = grant.getExpireDate();
        return entity;
    }

    public PointGrant toDomain() {
        PointGrant grant = PointGrant.create(
                userId,
                grantedAmount,
                grantType,
                expireDate,
                status
        );
        return grant;
    }

    public void cancel(String reason) {
        this.status = String.valueOf(PointGrantStatus.CANCELED);
        this.remainingAmount = 0;
        this.reason = reason;
    }

    public void use(long amount) {
        this.remainingAmount -= amount;

        if (this.remainingAmount == 0) {
            this.status = String.valueOf(PointGrantStatus.USED_ALL);
        } else {
            this.status = String.valueOf(PointGrantStatus.USED_PARTIAL);
        }
    }
}
