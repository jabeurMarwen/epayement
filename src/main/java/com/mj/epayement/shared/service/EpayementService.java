package com.mj.epayement.shared.service;

import org.springframework.stereotype.Service;

import com.mj.epayement.domain.payment.factory.PaymentFactory;
import com.mj.epayement.shared.entity.TransactionHistory;
import com.mj.epayement.shared.model.CheckStatusRequest;
import com.mj.epayement.shared.model.CheckStatusResponse;
import com.mj.epayement.shared.model.RequestPaymentRequest;
import com.mj.epayement.shared.model.RequestPaymentResponse;
import com.mj.epayement.shared.repository.TransactionHistoryRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class EpayementService {

    private final TransactionHistoryRepository transactionHistoryRepository;

    private final PaymentFactory factory;

    /**
     * request To Payement
     *
     * @param requestPaymentRequest @{@link RequestPaymentRequest}
     * @return @{@link RequestPaymentResponse}
     */
    public RequestPaymentResponse requestToPayement(RequestPaymentRequest requestPaymentRequest) {
        var transactionHistory =
                transactionHistoryRepository.findTransactionHistoryByAppTransactionIdAndEpaimentProvider(
                        requestPaymentRequest.getPaymentId(),
                        requestPaymentRequest.getService());
        if (transactionHistory == null) {
            transactionHistory = initTransactionHistory(requestPaymentRequest);
            if (transactionHistory != null) {
                transactionHistory = transactionHistoryRepository.save(transactionHistory);
                var result= factory.findPayementMethod(requestPaymentRequest.getService())
                        .requestPayment(requestPaymentRequest, transactionHistory);
                transactionHistory.setProviderTransactionId(result.getProviderTransactionId());
                transactionHistoryRepository.save(transactionHistory);
                return result;
            }
        }
        return RequestPaymentResponse.builder()
                .existTransaction(Boolean.TRUE).build();
    }

    /**
     * check Payment Status
     *
     * @param checkStatusRequest @{@link CheckStatusRequest}
     * @return @{@link CheckStatusResponse}
     */
    public CheckStatusResponse checkPaymentStatus(CheckStatusRequest checkStatusRequest) {
        var transactionHistory =
                transactionHistoryRepository.findTransactionHistoryByAppTransactionIdAndEpaimentProvider(
                        checkStatusRequest.getTransactionId(),
                        checkStatusRequest.getService());
        if (transactionHistory != null) {
            var result = factory.findPayementMethod(checkStatusRequest.getService())
                    .checkPaymentStatus(checkStatusRequest, transactionHistory);
            transactionHistory.setProviderTransactionStatus(result.getTransactionStatus());
            transactionHistoryRepository.save(transactionHistory);
            return result;
        }
        return null;
    }

    /**
     * @param requestPaymentRequest @{@link RequestPaymentRequest}
     * @return @{@link TransactionHistory}
     */
    private TransactionHistory initTransactionHistory(RequestPaymentRequest requestPaymentRequest) {
        if (requestPaymentRequest == null)
            return null;

        return TransactionHistory.builder()
                .application(requestPaymentRequest.getApplicationId())
                .shopId(requestPaymentRequest.getShopId())
                .shopPassword(requestPaymentRequest.getShopPassword())
                .appTransactionId(requestPaymentRequest.getPaymentId())
                .userId(requestPaymentRequest.getCustomerId())
                .epaimentProvider(requestPaymentRequest.getService())
                .build();
    }
}
