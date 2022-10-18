package com.mj.epayement.domain.payment.sobflous.service;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.mj.epayement.core.common.ServiceCall;
import com.mj.epayement.core.exception.ErrorCodes;
import com.mj.epayement.core.exception.RestException;
import com.mj.epayement.core.utils.JsonUtil;
import com.mj.epayement.core.utils.MapUtils;
import com.mj.epayement.core.utils.SobflousJsonProperties;
import com.mj.epayement.core.utils.SobflousUtils;
import com.mj.epayement.domain.payment.PaymentStrategy;
import com.mj.epayement.domain.payment.sobflous.exception.SobliFlousRequestPayementExeption;
import com.mj.epayement.domain.payment.sobflous.exception.SobliFlousVerifyPayementExeption;
import com.mj.epayement.shared.entity.TransactionHistory;
import com.mj.epayement.shared.model.*;
import com.mj.epayement.shared.repository.TransactionHistoryRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Marwen JABEUR
 * date: 15/07/2021
 */
@Slf4j
@Service
@AllArgsConstructor
public class SobflousPaymentService implements PaymentStrategy {

    public static final String SOBLIFLOUS_PAIEMENT_REQUEST = "/demandepaiement";
    public static final String SOBLIFLOUS_VERIFY_PAIEMENT = "/verificationtransaction";
    public static final String SOBLIFLOUS_CHECK_PAYEMENT_STATUS = "/statustransaction";
    public static final String ERROR_OCCURED_REQUEST_PAYEMENT = "An error has occurred when invoking sobflous request Payment API";
    public static final String ERROR_OCCURED_CHECK_PAYEMENT = "An error has occurred when invoking sobflous check Payment Status API";
    public static final String ERROR_OCCURED_VERIFY_PAYEMENT = "An error has occurred when invoking sobflous Verify PAYEMENT API";

    @Value("${application.feign.sobflous.uri}")
    private String uri;

    private final ServiceCall serviceCall;

    private final TransactionHistoryRepository transactionHistoryRepository;

    /**
     * @param transmId @{@link String}
     * @param state    @{@link String}
     */
    public void confirmationPayement(String transmId, String state) {
        if (StringUtils.isNotEmpty(transmId) && "s".equals(state)) {
            var transactionHistory =
                    transactionHistoryRepository.findTransactionHistoryByAppTransactionIdAndEpaimentProvider(transmId,
                            PaymentMethod.SOBFLOUS_SHOP);
            var startPaymentRequest = RequestPaymentRequest.builder()
                    .paymentId(transmId)
                    .shopId(transactionHistory.getShopId())
                    .shopPassword(transactionHistory.getShopPassword())
                    .build();
            verifyPayment(startPaymentRequest);
        }
    }

    /**
     * Creates payment request in sobflous system
     *
     * @param request @{@link RequestPaymentRequest}
     * @return RequestSobflousPaymentResponse @{@link RequestPaymentResponse}
     */
    @Override
    public RequestPaymentResponse requestPayment(RequestPaymentRequest request, TransactionHistory transactionHistory) {
        try {
            var url = String.format("%s%s", uri, SOBLIFLOUS_PAIEMENT_REQUEST);
            //prepare parameter
            var token = SobflousUtils.generateSobflousToken(request.getShopPassword(),
                    request.getShopId(), request.getPaymentId(), request.getAmount());
            var map = MapUtils.generateLinkedMultiValueMap(
                    Arrays.asList(SobflousJsonProperties.SHOP_ID, SobflousJsonProperties.ID_CLIENT_M,
                            SobflousJsonProperties.TRANSM_ID, SobflousJsonProperties.AMOUNT,
                            SobflousJsonProperties.TOKEN),
                    Arrays.asList(request.getShopId(), request.getCustomerId(),
                            request.getPaymentId(), request.getAmount(), token));

            //send request
            var response = serviceCall.callService(url, map, HttpMethod.POST);
            //change response to Json
            var json = JsonUtil.stringToJsonObject(response);
            if (json != null && json.has(SobflousJsonProperties.RESULT)) {
                var result = json.get(SobflousJsonProperties.RESULT);
                if (result.isJsonObject()){
                    var jsonObject = result.getAsJsonObject();
                    return RequestPaymentResponse.builder()
                            .providerTransactionId(JsonUtil.getAsString(jsonObject, SobflousJsonProperties.TRANSM_ID))
                            .providerUrl(JsonUtil.getAsString(jsonObject, SobflousJsonProperties.URL))
                            .providerMobileUrl(JsonUtil.getAsString(jsonObject, SobflousJsonProperties.URL_MOBILE))
                            .build();
                }
                var e = SobliFlousRequestPayementExeption.getByCode(result.getAsString());
                if (e != null) {
                    log.error("SOBLIFLOUS REQUEST OF TRANSACTION : " + request.getPaymentId() + " EXEPTION : " + e.getMessage());
                    throw new RestException(e.getMessage(), ErrorCodes.CLIENT_EXEPTION);
                }

            }
            log.error(ERROR_OCCURED_REQUEST_PAYEMENT);
            throw new RestException(response, ErrorCodes.REST_EXCEPTION);
        } catch (RestClientException e) {
            log.error(ERROR_OCCURED_REQUEST_PAYEMENT);
            log.error(e.getMessage());
            throw new RestException(e.getMessage(), ErrorCodes.REST_EXCEPTION);
        }
    }

    /**
     * verify payment to soblsous system
     *
     * @param request @{@link RequestPaymentRequest}
     */
    private void verifyPayment(RequestPaymentRequest request) {
        try {
            log.info("Verify PAYEMENT FOR TRANSACTION : " + request.getPaymentId());
            var transactionHistory =
                    transactionHistoryRepository.findTransactionHistoryByAppTransactionIdAndEpaimentProvider(request.getPaymentId(),
                            PaymentMethod.SOBFLOUS_SHOP);
            var token = SobflousUtils.generateSobflousToken(request.getShopPassword(),
                    request.getShopId(),
                    request.getPaymentId());
            var url = String.format("%s%s", uri, SOBLIFLOUS_VERIFY_PAIEMENT);
            //prepare parameter
            var map = MapUtils.generateLinkedMultiValueMap(
                    Arrays.asList(SobflousJsonProperties.SHOP_ID,
                            SobflousJsonProperties.TRANSM_ID, SobflousJsonProperties.TOKEN),
                    Arrays.asList(request.getShopId(), request.getPaymentId(), token));

            //send request
            var response = serviceCall.callService(url, map, HttpMethod.POST);
            //change response to Json
            var json = JsonUtil.stringToJsonObject(response);
            if (json != null && json.has(SobflousJsonProperties.RESULT)) {
                var result = json.get(SobflousJsonProperties.RESULT);
                if (result.isJsonObject()) {
                    var jsonObject = result.getAsJsonObject();
                    var state = JsonUtil.getAsString(jsonObject, SobflousJsonProperties.TRANSACTION_STATE);
                    log.info("VERIFICATION OF TRANSACTION : " + request.getPaymentId() + " STATE :" + state);
                    transactionHistory.setProviderPayementId(JsonUtil.getAsString(jsonObject, SobflousJsonProperties.ID_PAIEMENT_CLIENT));
                    transactionHistory.setProviderTransactionStatus(state);
                    transactionHistoryRepository.save(transactionHistory);
                } else {
                    var e = SobliFlousVerifyPayementExeption.getByCode(result.getAsString());
                    if (e != null) {
                        log.error("SOBLIFLOUS VERIFICATION OF TRANSACTION : " + request.getPaymentId() + " EXEPTION : " + e.getMessage());
                        throw new RestException(e.getMessage(), ErrorCodes.CLIENT_EXEPTION);
                    }
                    log.error(ERROR_OCCURED_VERIFY_PAYEMENT);
                    throw new RestException(response, ErrorCodes.REST_EXCEPTION);
                }
            }
        } catch (RestClientException e) {
            log.error(ERROR_OCCURED_VERIFY_PAYEMENT);
            log.error(e.getMessage());
            throw new RestException(e.getMessage(), ErrorCodes.REST_EXCEPTION);
        }
    }

    /**
     * check Payment Status from sobliflous
     *
     * @param sobflousCheckStatusRequest @{@link CheckStatusRequest}
     * @return @{@link CheckStatusResponse}
     */
    @Override
    public CheckStatusResponse checkPaymentStatus(CheckStatusRequest sobflousCheckStatusRequest, TransactionHistory transactionHistory) {
        try {
            if (transactionHistory != null &&
                    StringUtils.isNotEmpty(transactionHistory.getProviderTransactionId())) {
                var url = String.format("%s%s", uri, SOBLIFLOUS_CHECK_PAYEMENT_STATUS);
                var token = SobflousUtils.generateSobflousToken(sobflousCheckStatusRequest.getShopPassword(),
                        sobflousCheckStatusRequest.getShopId(), transactionHistory.getProviderTransactionId());
                //prepare parameter
                var map = MapUtils.generateLinkedMultiValueMap(
                        Arrays.asList(SobflousJsonProperties.SHOP_ID, SobflousJsonProperties.SOBFLOUS_TRANS_ID, SobflousJsonProperties.TOKEN),
                        Arrays.asList(sobflousCheckStatusRequest.getShopId(),
                                transactionHistory.getProviderTransactionId(), token));

                //send request
                var response = serviceCall.callService(url, map, HttpMethod.POST);
                //change response to Json
                var json = JsonUtil.stringToJsonObject(response);
                if (json != null && json.has(SobflousJsonProperties.RESULT)) {
                    var result = json.get(SobflousJsonProperties.RESULT);
                    if (result.isJsonObject()) {
                        var jsonObject = result.getAsJsonObject();
                        return CheckStatusResponse.builder()
                                .paymentId(JsonUtil.getAsString(jsonObject, SobflousJsonProperties.MERCHANT_TRANS_ID))
                                .transactionStatus(JsonUtil.getAsString(jsonObject, SobflousJsonProperties.TRANSACTION_STATE))
                                .build();
                    }

                    var e = SobliFlousVerifyPayementExeption.getByCode(result.getAsString());
                    if (e != null) {
                        log.error(ERROR_OCCURED_CHECK_PAYEMENT);
                        log.error(e.getMessage());
                        throw new RestException(e.getMessage(), ErrorCodes.CLIENT_EXEPTION);
                    }
                }
                log.error(ERROR_OCCURED_CHECK_PAYEMENT);
                log.error(response);
                throw new RestException(response, ErrorCodes.REST_EXCEPTION);
            }
            log.error(ERROR_OCCURED_CHECK_PAYEMENT + ": Not Found TRANSACTION HISTORY");
            throw new RestException("Not Found TRANSACTION HISTORY", ErrorCodes.REST_EXCEPTION);
        } catch (RestClientException e) {
            log.error(ERROR_OCCURED_CHECK_PAYEMENT);
            log.error(e.getMessage());
            throw new RestException(e.getMessage(), ErrorCodes.REST_EXCEPTION);
        }
    }

    @Override
    public PaymentMethod getPayementMethod() {
        return PaymentMethod.SOBFLOUS_SHOP;
    }

}