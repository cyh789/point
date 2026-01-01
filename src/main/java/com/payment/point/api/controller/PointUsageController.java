package com.payment.point.api.controller;

import com.payment.point.api.request.CancelUsageRequest;
import com.payment.point.api.request.UsePointRequest;
import com.payment.point.api.response.PointUsageResponse;
import com.payment.point.application.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/points/usages")
public class PointUsageController {

    private final PointService pointService;

    @PostMapping
    public ResponseEntity<?> use(@RequestBody UsePointRequest request) {
        try {
            PointUsageResponse response = pointService.usePoint(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> body = new HashMap<>();
            body.put("code", "INSUFFICIENT_BALANCE");
            body.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(body);
        } catch (Exception e) {
            Map<String, String> body = new HashMap<>();
            body.put("code", "INTERNAL_ERROR");
            body.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(body);
        }
//        PointUsageResponse response = pointService.usePoint(request);
//        return ResponseEntity.ok(response);
    }

    @PostMapping("/{usageId}/cancel")
    public ResponseEntity<?> cancel(
            @PathVariable Long usageId,
            @RequestBody CancelUsageRequest request
    ) {
        try {
            PointUsageResponse response = pointService.cancelUsage(usageId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> body = new HashMap<>();
            body.put("code", "CANCEL_AMOUNT_ERROR");
            body.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(body);
        } catch (Exception e) {
            Map<String, String> body = new HashMap<>();
            body.put("code", "INTERNAL_ERROR");
            body.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(body);
        }
//        PointUsageResponse response = pointService.cancelUsage(usageId, request);
//        return ResponseEntity.ok(response);
    }
}
