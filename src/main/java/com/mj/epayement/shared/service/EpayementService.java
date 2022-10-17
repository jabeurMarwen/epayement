package com.mj.epayement.shared.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mj.epayement.domain.clicktopay.service.ClickToPayPaymentService;
import com.mj.epayement.domain.sobflous.mapper.SobflousMapper;
import com.mj.epayement.domain.sobflous.service.SobflousPaymentService;
import com.mj.epayement.shared.entity.TransactionHistory;
import com.mj.epayement.shared.model.CheckStatusRequest;
import com.mj.epayement.shared.model.CheckStatusResponse;
import com.mj.epayement.shared.model.RequestPaymentRequest;
import com.mj.epayement.shared.model.RequestPaymentResponse;
import com.mj.epayement.shared.repository.TransactionHistoryRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EpayementService {

    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;

    @Autowired
    private SobflousPaymentService sobflousPaymentService;

    @Autowired
    private SobflousMapper sobflousMapper;

    @Autowired
    private ClickToPayPaymentService clickToPayPaymentService;

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
                switch (requestPaymentRequest.getService()) {
                    case SOBFLOUS_SHOP:
                        return sobflousRequestToPayement(requestPaymentRequest, transactionHistory);
                    case CLICKTOPAY_SHOP:
                        return clickToPayPaymentService.requestPayment(requestPaymentRequest, transactionHistory);
                    default:
                        return null;
                }
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
            switch (checkStatusRequest.getService()) {
                case SOBFLOUS_SHOP:
                    return sobflousCheckPaymentStatus(checkStatusRequest, transactionHistory);
                case CLICKTOPAY_SHOP:
                    return clickToPayCheckPaymentStatus(checkStatusRequest, transactionHistory);
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * sobflous Request To Payement
     *
     * @param requestPaymentRequest @{@link RequestPaymentRequest}
     * @param transactionHistory    @{@link TransactionHistory}
     * @return @{@link RequestPaymentResponse}
     */
    private RequestPaymentResponse sobflousRequestToPayement(RequestPaymentRequest requestPaymentRequest, TransactionHistory transactionHistory) {
        var startSobflousPaymentResponse = sobflousPaymentService.requestPayment(requestPaymentRequest);
        if (startSobflousPaymentResponse != null) {
            transactionHistory.setProviderTransactionId(startSobflousPaymentResponse.getProviderTransactionId());
            transactionHistoryRepository.save(transactionHistory);
            return startSobflousPaymentResponse;
        }
        return null;
    }

    /**
     * sobflous Check Payment Status
     *
     * @param checkStatusRequest @{@link CheckStatusRequest}
     * @param transactionHistory @{@link TransactionHistory}
     * @return @{@link CheckStatusResponse}
     */
    private CheckStatusResponse sobflousCheckPaymentStatus(CheckStatusRequest checkStatusRequest,
                                                           TransactionHistory transactionHistory) {
        var sobflousCheckStatusResponse = sobflousPaymentService.checkPaymentStatus(checkStatusRequest);
        if (sobflousCheckStatusResponse != null) {
            transactionHistory.setProviderTransactionStatus(sobflousCheckStatusResponse.getTransactionStatus());
            transactionHistoryRepository.save(transactionHistory);
            return sobflousCheckStatusResponse;
        }
        return null;
    }

    /**
     * click To Pay Check Payment Status
     *
     * @param checkStatusRequest @{@link CheckStatusRequest}
     * @param transactionHistory @{@link TransactionHistory}
     * @return @{@link CheckStatusResponse}
     */
    private CheckStatusResponse clickToPayCheckPaymentStatus(CheckStatusRequest checkStatusRequest, TransactionHistory transactionHistory) {
        var checkStatusResponse = clickToPayPaymentService.checkPaymentStatus(checkStatusRequest, transactionHistory);
        if (checkStatusResponse != null) {
            transactionHistory.setProviderTransactionStatus(checkStatusResponse.getTransactionStatus());
            transactionHistoryRepository.save(transactionHistory);
            return checkStatusResponse;
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
