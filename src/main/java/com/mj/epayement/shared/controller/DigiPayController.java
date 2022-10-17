package com.mj.epayement.shared.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping
    public ResponseEntity<RequestPaymentResponse> requestToPayement(RequestPaymentRequest requestPaymentRequest) {
        return ResponseEntity.ok(epayementService.requestToPayement(requestPaymentRequest));
    }

    @GetMapping
    public ResponseEntity<CheckStatusResponse> checkPaymentStatus(CheckStatusRequest checkStatusRequest) {
        return ResponseEntity.ok(epayementService.checkPaymentStatus(checkStatusRequest));
    }
}
