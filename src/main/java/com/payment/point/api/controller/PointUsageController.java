package com.payment.point.api.controller;

import com.payment.point.api.request.CancelUsageRequest;
import com.payment.point.api.request.UsePointRequest;
import com.payment.point.api.response.PointUsageResponse;
import com.payment.point.application.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/points/usages")
public class PointUsageController {

    private final PointService pointService;

    @PostMapping
    public ResponseEntity<PointUsageResponse> use(@RequestBody UsePointRequest request) {
        PointUsageResponse response = pointService.usePoint(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{usageId}/cancel")
    public ResponseEntity<PointUsageResponse> cancel(
            @PathVariable Long usageId,
            @RequestBody CancelUsageRequest request
    ) {
        PointUsageResponse response = pointService.cancelUsage(usageId, request);
        return ResponseEntity.ok(response);
    }
}
