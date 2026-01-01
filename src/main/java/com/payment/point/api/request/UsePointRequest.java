package com.payment.point.api.request;

import lombok.Getter;

@Getter
public class UsePointRequest {

    private Long userId;
    private long amount;
    private String reason;
    private Long orderId;
}
