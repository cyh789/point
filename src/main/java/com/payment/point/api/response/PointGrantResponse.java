package com.payment.point.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PointGrantResponse {

    private Long grantId;
    private Long grantedAmount;
    private Long remainingAmount;
    private String grantType;
    private LocalDateTime expireDate;
    private String status;
    private String reason;
}
