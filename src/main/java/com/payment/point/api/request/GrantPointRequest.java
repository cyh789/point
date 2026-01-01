package com.payment.point.api.request;

import com.payment.point.domain.grant.PointGrantType;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class GrantPointRequest {

    private Long userId;
    private Long amount;
    private PointGrantType grantType;
    private LocalDateTime expireDate;
    private String reason;
}
