package com.mj.epayement.domain.payment.sobflous.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mj.epayement.domain.payment.sobflous.service.SobflousPaymentService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Marwen JABEUR
 * date: 14/10/2022
 */
@Slf4j
@RestController
@AllArgsConstructor
public class SobflousController {

    private final SobflousPaymentService sobflousPaymentService;

    @GetMapping
    public ResponseEntity<Void> confirmPayment(String transmId, String state) {
        log.info("CONFIRM PAYEMENT FOR TRANSACTION : " + transmId + " State : " + state);
        sobflousPaymentService.confirmationPayement(transmId, state);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
