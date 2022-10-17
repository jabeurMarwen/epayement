package com.mj.epayement.shared.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RequestPaymentRequest {

    private String currency;
    private String applicationId;
    private PaymentMethod service;
    private String customerId;
    private String bonusVariable;
    private String amount;
    private String shopId;
    private String shopPassword;
    private String paymentId;
    private String backUrl;
    private String providerTransactionId;

}
