package com.payment.point.api.response;

import com.payment.point.domain.grant.PointGrantStatus;
import com.payment.point.domain.grant.PointGrantType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PointGrantResponse {

    private Long grantId;

    private Long userId;
    private long grantedAmount;
    private long remainingAmount;
    private PointGrantType grantType;
    private LocalDateTime expireDate;
    private PointGrantStatus status;
    private String reason;
}
