package com.payment.point.api.request;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class GrantPointRequest {

    private Long userId;
    private int amount;
    private String grantType; // AUTO, MANUAL
    private LocalDateTime expireAt; // optional
}
