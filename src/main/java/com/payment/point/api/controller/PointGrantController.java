package com.payment.point.api.controller;

import com.payment.point.api.request.CancelGrantRequest;
import com.payment.point.application.service.PointService;
import com.payment.point.api.request.GrantPointRequest;
import com.payment.point.api.response.PointGrantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/points/grants")
public class PointGrantController {

    private final PointService pointService;

    @PostMapping
    public ResponseEntity<PointGrantResponse> grant(@RequestBody GrantPointRequest request) {
        return ResponseEntity.ok(pointService.grantPoint(request));
    }

    @PostMapping("/{grantId}/cancel")
    public ResponseEntity<PointGrantResponse> cancel(
            @PathVariable Long grantId,
            @RequestBody CancelGrantRequest request
    ) {
        PointGrantResponse response = pointService.cancelGrant(grantId, request);
        return ResponseEntity.ok(response);
    }
}
