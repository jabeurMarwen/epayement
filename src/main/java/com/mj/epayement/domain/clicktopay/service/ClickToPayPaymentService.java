package com.mj.epayement.domain.clicktopay.service;

import java.util.Arrays;
import java.util.Currency;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.mj.epayement.core.common.ServiceCall;
import com.mj.epayement.core.exception.ErrorCodes;
import com.mj.epayement.core.exception.RestException;
import com.mj.epayement.core.utils.ClickToPayJsonProperties;
import com.mj.epayement.core.utils.JsonUtil;
import com.mj.epayement.core.utils.MapUtils;
import com.mj.epayement.domain.clicktopay.exception.ClickToPayRequestPayementExeption;
import com.mj.epayement.domain.clicktopay.exception.ClickToPayVerifyPayementExeption;
import com.mj.epayement.shared.entity.TransactionHistory;
import com.mj.epayement.shared.model.CheckStatusRequest;
import com.mj.epayement.shared.model.CheckStatusResponse;
import com.mj.epayement.shared.model.RequestPaymentRequest;
import com.mj.epayement.shared.model.RequestPaymentResponse;
import com.mj.epayement.shared.repository.TransactionHistoryRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Marwen JABEUR
 * date: 14/10/2022
 */
@Slf4j
@Service
public class ClickToPayPaymentService {

    public static final String CLICKTOPAY_PAIEMENT_REQUEST = "/register.do";
    public static final String CLICKTOPAY_CHECK_PAYEMENT_STATUS = "/getOrderStatus.do";
    public static final String ERROR_OCCURED_REQUEST_PAYEMENT = "An error has occurred when invoking clickToPay request Payment API";
    public static final String ERROR_OCCURED_CHECK_PAYEMENT = "An error has occurred when invoking clickToPay check Payment Status API";

    @Value("${application.feign.clickToPay.uri}")
    private String uri;

    @Autowired
    private ServiceCall serviceCall;

    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;


    /**
     * send request payment to ClickToPay system
     *
     * @param requestPaymentRequest @{@link RequestPaymentRequest}
     * @param transactionHistory    @{@link TransactionHistory}
     * @return ClickToPayPaymentResponse @{@link RequestPaymentResponse}
     */
    public RequestPaymentResponse requestPayment(RequestPaymentRequest requestPaymentRequest, TransactionHistory transactionHistory) {
        try {
            var url = String.format("%s%s", uri, CLICKTOPAY_PAIEMENT_REQUEST);
            var currency = Currency.getInstance(requestPaymentRequest.getCurrency())
                    .getNumericCodeAsString();
            var amount = requestPaymentRequest.getAmount().replace(".", "");

            var map = MapUtils.generateLinkedMultiValueMap(
                    Arrays.asList(ClickToPayJsonProperties.USERNAME, ClickToPayJsonProperties.PASSWORD, ClickToPayJsonProperties.ORDER_NUMBER, ClickToPayJsonProperties.AMOUNT,
                            ClickToPayJsonProperties.CURRENCY, ClickToPayJsonProperties.RETURN_URL),
                    Arrays.asList(requestPaymentRequest.getShopId(),
                            requestPaymentRequest.getShopPassword(), requestPaymentRequest.getPaymentId(), amount,
                            currency, requestPaymentRequest.getBackUrl()));

            var response = serviceCall.callService(url, map, HttpMethod.POST);
            log.error("responce" + response);
            var json = JsonUtil.stringToJsonObject(response);
            if (json != null) {
                if (json.has(ClickToPayJsonProperties.ERROR_CODE)) {
                    log.error(ERROR_OCCURED_REQUEST_PAYEMENT);
                    var msg = ClickToPayRequestPayementExeption.
                            getByCode(JsonUtil.getAsString(json, ClickToPayJsonProperties.ERROR_CODE)).getMessage();
                    log.error(msg);
                    throw new RestException(msg, ErrorCodes.REST_EXCEPTION);
                }

                var orderId = JsonUtil.getAsString(json, ClickToPayJsonProperties.ORDER_ID);
                if (StringUtils.isNotEmpty(orderId)) {
                    transactionHistory.setProviderTransactionId(orderId);
                    transactionHistoryRepository.save(transactionHistory);
                    return RequestPaymentResponse.builder()
                            .paymentId(requestPaymentRequest.getPaymentId())
                            .providerUrl(JsonUtil.getAsString(json, ClickToPayJsonProperties.FORM_URL))
                            .build();
                }
            }
            log.error(ERROR_OCCURED_REQUEST_PAYEMENT);
        } catch (RestClientException e) {
            log.error(ERROR_OCCURED_REQUEST_PAYEMENT);
            log.error(e.getMessage());
            throw new RestException(e.getMessage(), ErrorCodes.REST_EXCEPTION);
        }
        return null;
    }

    /**
     * @param checkStatusRequest @{@link CheckStatusRequest}
     * @param transactionHistory @{@link TransactionHistory}
     * @return @{@link CheckStatusResponse}
     */
    public CheckStatusResponse checkPaymentStatus(CheckStatusRequest checkStatusRequest, TransactionHistory transactionHistory) {
        try {
            if (transactionHistory != null &&
                    transactionHistory.getProviderTransactionId() != null) {
                var url = String.format("%s%s", uri, CLICKTOPAY_CHECK_PAYEMENT_STATUS);
                var map = MapUtils.generateLinkedMultiValueMap(
                        Arrays.asList(ClickToPayJsonProperties.USERNAME, ClickToPayJsonProperties.PASSWORD,
                                ClickToPayJsonProperties.ORDER_ID),
                        Arrays.asList(checkStatusRequest.getShopId(),
                                checkStatusRequest.getShopPassword(), transactionHistory.getProviderTransactionId()));

                var response = serviceCall.callService(url, map, HttpMethod.POST);
                var json = JsonUtil.stringToJsonObject(response);
                if (json != null) {
                    var errorCode = JsonUtil.getAsString(
                            json, ClickToPayJsonProperties.ERRORCODE);
                    if (StringUtils.isNotEmpty(errorCode) && !"0".equals(errorCode)) {
                        log.error(ERROR_OCCURED_CHECK_PAYEMENT);
                        var msg = ClickToPayVerifyPayementExeption.getByCode(errorCode).getMessage();
                        log.error(msg);
                        throw new RestException(msg, ErrorCodes.REST_EXCEPTION);
                    }
                    if (json.has(ClickToPayJsonProperties.ORDER_STATUS)) {
                        var status = ("2").equals(
                                JsonUtil.getAsString(json, ClickToPayJsonProperties.ORDER_STATUS)) ?
                                "success" : null;
                        return CheckStatusResponse.builder()
                                .paymentId(checkStatusRequest.getTransactionId())
                                .transactionStatus(status)
                                .build();
                    }
                    log.error(ERROR_OCCURED_CHECK_PAYEMENT);
                    throw new RestException("", ErrorCodes.REST_EXCEPTION);
                }
            }
            log.error(ERROR_OCCURED_CHECK_PAYEMENT);
            log.error("provider_id is null");
        } catch (RestClientException e) {
            log.error(ERROR_OCCURED_CHECK_PAYEMENT);
            log.error(e.getMessage());
            throw new RestException(e.getMessage(), ErrorCodes.REST_EXCEPTION);
        }
        return null;
    }
}
