package com.mj.epayement.domain.payment.sobflous.service;

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
import com.mj.epayement.domain.payment.sobflous.mapper.SobflousMapper;
import com.mj.epayement.shared.entity.TransactionHistory;
import com.mj.epayement.shared.model.CheckStatusRequest;
import com.mj.epayement.shared.model.PaymentMethod;
import com.mj.epayement.shared.model.RequestPaymentRequest;
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
class SobflousPaymentServiceTest {

    public static final String SOBLIFLOUS_PAIEMENT_REQUEST = "/demandepaiement";
    public static final String SOBLIFLOUS_CHECK_PAYEMENT_STATUS = "/statustransaction";
    ;

    @Value("${application.feign.sobflous.uri}")
    private String sobflousUri;

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
    private SobflousPaymentService sobflousPaymentService;

    @Autowired
    private SobflousMapper sobflousMapper;

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
                    .backUrl("")
                    .bonusVariable("")
                    .currency("")
                    .customerId("100")
                    .shopPassword("2pNJDYE31S")
                    .shopId("168")
                    .paymentId("26")
                    .service(PaymentMethod.SOBFLOUS_SHOP)
                    .build();

            TransactionHistory transactionHistory = TransactionHistory.builder()
                    .shopId("168")
                    .shopPassword("2pNJDYE31S")
                    .appTransactionId("26")
                    .providerTransactionId("")
                    .providerPayementId("").build();
            when(transactionHistoryRepository.save(any()))
                    .thenReturn(transactionHistory);
            mockSobliflouRequestApi();

            var startSobflousPaymentResponse = sobflousPaymentService.requestPayment(paymentPost, transactionHistory);
            assertNotNull(startSobflousPaymentResponse);
            assertNotNull(startSobflousPaymentResponse.getProviderUrl());
            assertNotNull(startSobflousPaymentResponse.getProviderMobileUrl());
        }
    }

    @Test
    void GIVEN_call_check_payement_WHEN_sobflous_payement_should_return_reqonce() throws Exception {
        try (MockedStatic<EPaymentAuditorAware> utilities = Mockito.mockStatic(EPaymentAuditorAware.class)) {
            utilities.when(EPaymentAuditorAware::getCurrentUsername)
                    .thenReturn("Default auditor");

            MockitoAnnotations.openMocks(this);
            TransactionHistory transactionHistory = TransactionHistory.builder()
                    .shopId("168")
                    .shopPassword("2pNJDYE31S")
                    .appTransactionId("26")
                    .providerTransactionId("10965")
                    .providerPayementId("10965").build();
            when(transactionHistoryRepository.findTransactionHistoryByAppTransactionIdAndEpaimentProvider(anyString(), any(PaymentMethod.class)))
                    .thenReturn(transactionHistory);
            when(transactionHistoryRepository.findTransactionHistoryByAppTransactionId(anyString()))
                    .thenReturn(transactionHistory);
            CheckStatusRequest checkStatusRequest = CheckStatusRequest.builder()
                    .shopPassword("2pNJDYE31S")
                    .shopId("168")
                    .service(PaymentMethod.SOBFLOUS_SHOP)
                    .transactionId("26")
                    .build();

            mockSobliflouCheckPayementApi();

            var sobflousCheckStatusResponse = sobflousPaymentService.checkPaymentStatus(checkStatusRequest, transactionHistory);

            assertNotNull(sobflousCheckStatusResponse);
            assertNotNull(sobflousCheckStatusResponse.getTransactionStatus());
        }
    }

    private void mockSobliflouRequestApi() throws URISyntaxException {
        String apiResponce =
                "{\"result\":{\"TRANSM_ID\":\"26\",\"AMOUNT\":\"29.000\",\"DISCOUNT\":0,\"DISCOUNT_AMOUNT\":29,\"TRANSS_ID\":\"10965\",\"URL\":\"https:\\/\\/www.sobflous.online\\/fr\\/admin\\/marchand\\/payementmarchand\\/e93a27593927c43ba9726a69a29afa7d7a691872fcad555773a6214ccd4e69441d9dc70db0ab3eb74b1b221dea13ed27b403c85607097f1f6151328fce13fdf7\",\"TOKEN\":\"c5882e27adbe146f9167f2d4c8dade081f6dc758f84d7013cda841f888d6cf7b863c07f54dd85655603dfc12dcd455fcf91d10150d1125c86fa1f8aba5b18013\",\"CODE_COMMANDE\":\"VTYNL23704M\",\"URL_MOBILE\":\"sobflous:\\/\\/codeCommande?c=VTYNL23704M\"}}";
        mockServer.expect(ExpectedCount.once(), requestTo(new URI(String.format("%s%s", sobflousUri, SOBLIFLOUS_PAIEMENT_REQUEST))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .body(apiResponce));
    }

    private void mockSobliflouCheckPayementApi() throws URISyntaxException {
        String apiResponce = "{\"result\":{\"MERCHANT_TRANS_ID\":\"26\",\"AMOUNT\":\"29\",\"SOBFLOUS_TRANS_ID\":\"10965\",\"TRANSACTION_STATE\":\"waiting\"}}";
        mockServer.expect(ExpectedCount.once(), requestTo(new URI(String.format("%s%s", sobflousUri, SOBLIFLOUS_CHECK_PAYEMENT_STATUS))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .body(apiResponce));
    }
}
