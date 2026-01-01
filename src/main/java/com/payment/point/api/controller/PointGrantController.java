package com.payment.point.api.controller;

import com.payment.point.api.request.CancelGrantRequest;
import com.payment.point.api.request.GrantPointRequest;
import com.payment.point.api.response.PointGrantResponse;
import com.payment.point.application.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/points/grants")
public class PointGrantController {

    private final PointService pointService;

    @PostMapping
    public ResponseEntity<?> grant(@RequestBody GrantPointRequest request) {
        try {
            PointGrantResponse response = pointService.grantPoint(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> body = new HashMap<>();
            body.put("code", "MAX_BALANCE_EXCEEDED");
            body.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(body);
        } catch (Exception e) {
            Map<String, String> body = new HashMap<>();
            body.put("code", "INTERNAL_ERROR");
            body.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(body);
        }
//        return ResponseEntity.ok(pointService.grantPoint(request));
    }

    @PostMapping("/{grantId}/cancel")
    public ResponseEntity<?> cancel(
            @PathVariable Long grantId,
            @RequestBody CancelGrantRequest request
    ) {

        try {
            PointGrantResponse response = pointService.cancelGrant(grantId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, String> body = new HashMap<>();
            body.put("code", "CANCEL_NOT_ALLOWED");
            body.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(body);
        } catch (Exception e) {
            Map<String, String> body = new HashMap<>();
            body.put("code", "INTERNAL_ERROR");
            body.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(body);
        }
//        PointGrantResponse response = pointService.cancelGrant(grantId, request);
//        return ResponseEntity.ok(response);
    }
}
