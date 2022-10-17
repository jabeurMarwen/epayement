package com.mj.epayement.domain.payment.clicktopay.service;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.mj.epayement.EpayementApplication;
import com.mj.epayement.core.config.EPaymentAuditorAware;
import com.mj.epayement.shared.entity.TransactionHistory;
import com.mj.epayement.shared.model.*;
import com.mj.epayement.shared.repository.TransactionHistoryRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = EpayementApplication.class)
@ActiveProfiles("test")
class ClickToPayPaymentServiceTest {

    public static final String CLICKTOPAY_PAIEMENT_REQUEST = "/register.do";
    public static final String CLICKTOPAY_CHECK_PAYEMENT_STATUS = "/getOrderStatus.do";

    @Value("${application.feign.clickToPay.uri}")
    private String clickToPayUri;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private TransactionHistoryRepository transactionHistoryRepository;

    private MockRestServiceServer mockServer;


    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected MappingJackson2HttpMessageConverter springMvcJacksonConverter;

    @Autowired
    private ClickToPayPaymentService clickToPayPaymentService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void GIVEN_call_request_payement_WHEN_request_ok_should_return_reqonce() throws Exception {
        try (MockedStatic<EPaymentAuditorAware> utilities = Mockito.mockStatic(EPaymentAuditorAware.class)) {
            utilities.when(EPaymentAuditorAware::getCurrentUsername)
                    .thenReturn("Default auditor");
            RequestPaymentRequest paymentPost = RequestPaymentRequest.builder()
                    .amount("29.000")
                    .applicationId("KLUB_KISSA")
                    .backUrl("https://int.klubkissa.com/auth/login")
                    .bonusVariable("")
                    .currency("TND")
                    .customerId("100")
                    .shopPassword("XSc74s2c")
                    .shopId("0402522023")
                    .paymentId("5")
                    .service(PaymentMethod.CLICKTOPAY_SHOP)
                    .build();

            TransactionHistory transactionHistory = TransactionHistory.builder()
                    .shopId("168")
                    .shopPassword("2pNJDYE31S")
                    .appTransactionId("26")
                    .providerTransactionId("")
                    .providerPayementId("").build();
            when(transactionHistoryRepository.save(any()))
                    .thenReturn(transactionHistory);


            mockClickToPayRequestApi();
            RequestPaymentResponse requestPaymentResponse = clickToPayPaymentService.requestPayment(paymentPost,transactionHistory);

            assertNotNull(requestPaymentResponse);
            assertNotNull(requestPaymentResponse.getProviderUrl());
        }
    }

    @Test
    void GIVEN_call_check_payement_WHEN_clickToPay_payement_should_return_reqonce() throws Exception {
        try (MockedStatic<EPaymentAuditorAware> utilities = Mockito.mockStatic(EPaymentAuditorAware.class)) {
            utilities.when(EPaymentAuditorAware::getCurrentUsername)
                    .thenReturn("Default auditor");
            TransactionHistory transactionHistory = TransactionHistory.builder()
                    .shopId("168")
                    .shopPassword("2pNJDYE31S")
                    .appTransactionId("26")
                    .providerTransactionId("10965")
                    .providerPayementId("10965").build();
            when(transactionHistoryRepository.findTransactionHistoryByAppTransactionIdAndEpaimentProvider(anyString(), any(PaymentMethod.class)))
                    .thenReturn(transactionHistory);
            CheckStatusRequest checkStatusRequest = CheckStatusRequest.builder()
                    .shopPassword("XSc74s2c")
                    .shopId("0402522023")
                    .service(PaymentMethod.CLICKTOPAY_SHOP)
                    .transactionId("115")
                    .build();
            mockClickToPayCheckPayementApi();
            CheckStatusResponse requestPaymentResponse = clickToPayPaymentService.checkPaymentStatus(checkStatusRequest, transactionHistory);

            assertNotNull(requestPaymentResponse);
        }
    }

    private void mockClickToPayRequestApi() throws URISyntaxException {
        String apiResponce =
                "{\"orderId\":\"8ae1ac24-0c8b-767d-93c1-804400c2bb94\",\"formUrl\":\"https://test.clictopay.com/payment/merchants/CLICTOPAY/payment_fr.html?mdOrder=8ae1ac24-0c8b-767d-93c1-804400c2bb94\"}";
        mockServer.expect(ExpectedCount.once(), requestTo(new URI(String.format("%s%s", clickToPayUri, CLICKTOPAY_PAIEMENT_REQUEST))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .body(apiResponce));
    }

    private void mockClickToPayCheckPayementApi() throws URISyntaxException {
        String apiResponce =
                "{\"depositAmount\":0,\"currency\":\"788\",\"authCode\":2,\"ErrorCode\":\"\",\"ErrorMessage\":\"Payment is declined\",\"OrderStatus\":6,\"OrderNumber\":\"140\",\"Amount\":900}";
        mockServer.expect(ExpectedCount.once(), requestTo(new URI(String.format("%s%s", clickToPayUri, CLICKTOPAY_CHECK_PAYEMENT_STATUS))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .body(apiResponce));
    }
}
