package com.payment.point.api.controller;

import com.payment.point.application.PointCommandService;
import com.payment.point.api.request.GrantPointRequest;
import com.payment.point.api.response.PointGrantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/points/grants")
@RequiredArgsConstructor
public class PointGrantController {

    private final PointCommandService pointCommandService;

    @PostMapping
    public ResponseEntity<PointGrantResponse> grantPoint(@RequestBody GrantPointRequest request) {
        PointGrantResponse response = pointCommandService.grantPoint(request);
        return ResponseEntity.ok(response);
    }
}
