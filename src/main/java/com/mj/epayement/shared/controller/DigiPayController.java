package com.mj.epayement.shared.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mj.epayement.shared.model.CheckStatusRequest;
import com.mj.epayement.shared.model.CheckStatusResponse;
import com.mj.epayement.shared.model.RequestPaymentRequest;
import com.mj.epayement.shared.model.RequestPaymentResponse;
import com.mj.epayement.shared.service.EpayementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DigiPayController {

    private final EpayementService epayementService;

    @PostMapping("/request-to-payement")
    public ResponseEntity<RequestPaymentResponse> requestToPayement(@RequestBody RequestPaymentRequest requestPaymentRequest) {
        return ResponseEntity.ok(epayementService.requestToPayement(requestPaymentRequest));
    }

    @GetMapping("/check-payment-status")
    public ResponseEntity<CheckStatusResponse> checkPaymentStatus(@RequestBody CheckStatusRequest checkStatusRequest) {
        return ResponseEntity.ok(epayementService.checkPaymentStatus(checkStatusRequest));
    }
}
