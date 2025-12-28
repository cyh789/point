package com.payment.point.api.controller;

import com.payment.point.api.request.CancelGrantRequest;
import com.payment.point.api.response.PointGrantResponse;
import com.payment.point.application.service.PointCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/points/grants")
public class PointGrantCancelController {

    private final PointCommandService pointCommandService;

    @PostMapping("/{grantId}/cancel")
    public ResponseEntity<PointGrantResponse> cancelGrant(
            @PathVariable Long grantId,
            @RequestBody CancelGrantRequest request
    ) {
        PointGrantResponse response = pointCommandService.cancelGrant(grantId, request);
        return ResponseEntity.ok(response);
    }
}
