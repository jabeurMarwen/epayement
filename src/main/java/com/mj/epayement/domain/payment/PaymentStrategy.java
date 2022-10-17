package com.mj.epayement.domain.payment;

import com.mj.epayement.shared.entity.TransactionHistory;
import com.mj.epayement.shared.model.*;

public interface PaymentStrategy {

    RequestPaymentResponse requestPayment(RequestPaymentRequest requestPaymentRequest, TransactionHistory transactionHistory);

    CheckStatusResponse checkPaymentStatus(CheckStatusRequest checkStatusRequest, TransactionHistory transactionHistory);

    PaymentMethod getPayementMethod();

}
