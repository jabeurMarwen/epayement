package com.mj.epayement.shared.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CheckStatusRequest {

    private String shopId;
    private String shopPassword;
    private String transactionId;
    private PaymentMethod service;
}
