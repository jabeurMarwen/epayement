package com.mj.epayement.shared.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CheckStatusResponse {
    private String paymentId;
    private String transactionStatus;
}
