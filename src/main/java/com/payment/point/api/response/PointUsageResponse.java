package com.payment.point.api.response;

import com.payment.point.domain.usage.PointUsageStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PointUsageResponse {

    private Long usageId;

    private Long userId;
    private long usedAmount;
    private PointUsageStatus status;
    private String reason;
}
