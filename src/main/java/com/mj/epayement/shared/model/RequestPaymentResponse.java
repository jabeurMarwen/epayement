package com.mj.epayement.shared.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RequestPaymentResponse {

    private String paymentId;
    private String providerUrl;
    private Boolean existTransaction;
    private String providerMobileUrl;
    private String providerTransactionId;

}
