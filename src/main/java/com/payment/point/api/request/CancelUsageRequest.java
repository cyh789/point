package com.payment.point.api.request;

import lombok.Getter;

@Getter
public class CancelUsageRequest {

    private Long cancelAmount;
    private String reason;
    private Long orderId;
}
