package com.payment.point.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PointGrantResponse {

    private Long grantId;
    private int totalAmount;
    private int remainingAmount;
    private String grantType;
    private LocalDateTime expireAt;
}
