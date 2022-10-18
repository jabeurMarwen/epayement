package com.mj.epayement.shared;

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
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mj.epayement.EpayementApplication;
import com.mj.epayement.core.config.EPaymentAuditorAware;
import com.mj.epayement.core.exception.RestException;
import com.mj.epayement.shared.entity.TransactionHistory;
import com.mj.epayement.shared.model.*;
import com.mj.epayement.shared.repository.TransactionHistoryRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
class DigiPayControllerTest {

    public static final String SOBLIFLOUS_PAIEMENT_REQUEST = "/demandepaiement";
    public static final String SOBLIFLOUS_CHECK_PAYEMENT_STATUS = "/statustransaction";
    public static final String CLICKTOPAY_PAIEMENT_REQUEST = "/register.do";
    public static final String CLICKTOPAY_CHECK_PAYEMENT_STATUS = "/getOrderStatus.do";
    private static final String BASE_PATH = "/digiPay";
    private static final String REQUEST_PAYEMENT = "/payement-request";
    private static final String CHECK_PAYEMENT_STATUS = "/check-payement-status";

    @Value("${application.feign.sobflous.uri}")
    private String sobflousUri;

    @Value("${application.feign.clickToPay.uri}")
    private String clickToPayUri;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected MappingJackson2HttpMessageConverter springMvcJacksonConverter;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private TransactionHistoryRepository transactionHistoryRepository;

    private MockRestServiceServer mockServer;

    private MockMvc mockMvc;
    private ObjectMapper jacksonObjectMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        jacksonObjectMapper = springMvcJacksonConverter.getObjectMapper();
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void GIVEN_call_invalid_uri_WHEN_make_call_then_should_return_not_found_request() throws Exception {
        try (MockedStatic<EPaymentAuditorAware> utilities = Mockito.mockStatic(EPaymentAuditorAware.class)) {
            utilities.when(EPaymentAuditorAware::getCurrentUsername)
                    .thenReturn("Default auditor");

            RequestPaymentRequest paymentPost = RequestPaymentRequest.builder().build();
            String paymentPostDtoAsJsonString = jacksonObjectMapper.writeValueAsString(paymentPost);

            // WHEN
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH + "/invalid-uri/no-think")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(paymentPostDtoAsJsonString)).andReturn();

            // THEN
            int status = mvcResult.getResponse().getStatus();
            assertThat(status, is(HttpStatus.NOT_FOUND.value()));
        }
    }

    @Test
    void GIVEN_call_invalid_uri_WHEN_make_call_then_should_return_bad_request() throws Exception {
        try (MockedStatic<EPaymentAuditorAware> utilities = Mockito.mockStatic(EPaymentAuditorAware.class)) {
            utilities.when(EPaymentAuditorAware::getCurrentUsername)
                    .thenReturn("Default auditor");
            RequestPaymentRequest paymentPost = RequestPaymentRequest.builder()
                    .amount("100")
                    .applicationId("100")
                    .shopPassword("XSc74s2c")
                    .shopId("0402522023")
                    .paymentId("5")
                    .service(PaymentMethod.CLICKTOPAY_SHOP)
                    .build();
            String paymentPostDtoAsJsonString = jacksonObjectMapper.writeValueAsString(paymentPost);
            // WHEN
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH + REQUEST_PAYEMENT)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(paymentPostDtoAsJsonString)).andReturn();

            // THEN
            int status = mvcResult.getResponse().getStatus();
            assertThat(status, is(HttpStatus.BAD_REQUEST.value()));
        }
    }

    @Test
    void GIVEN_call_valid_uri_and_bad_shop_WHEN_make_call_then_should_throw_error() throws Exception {
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
                    .shopPassword("XSc74s2c")
                    .shopId("0402522023")
                    .paymentId("5")
                    .service(PaymentMethod.SOBFLOUS_SHOP)
                    .build();
            String paymentPostDtoAsJsonString = jacksonObjectMapper.writeValueAsString(paymentPost);

            mockServer.expect(ExpectedCount.once(), requestTo(new URI(String.format("%s%s", sobflousUri, SOBLIFLOUS_PAIEMENT_REQUEST))))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond((response) -> {
                        throw new RestClientException("error");
                    });
            // WHEN
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH + REQUEST_PAYEMENT)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(paymentPostDtoAsJsonString)).andReturn();

            // THEN
            MockHttpServletResponse response = mvcResult.getResponse();
            String responseContentAsString = response.getContentAsString();

            RestException restException = jacksonObjectMapper.readValue(responseContentAsString, RestException.class);
            assertNotNull(restException);
            assertEquals("error", restException.getMessage());
        }
    }

    @Test
    void GIVEN_call_valid_uri_and_bad_shop_WHEN_make_call_then_should_return_reqonce() throws Exception {
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
                    .shopPassword("XSc74s2c")
                    .shopId("0402522023")
                    .paymentId("5")
                    .service(PaymentMethod.SOBFLOUS_SHOP)
                    .build();
            String paymentPostDtoAsJsonString = jacksonObjectMapper.writeValueAsString(paymentPost);
            String apiResponce = "{\"result\":\"205\"}";
            mockServer.expect(ExpectedCount.once(), requestTo(new URI(String.format("%s%s", sobflousUri, SOBLIFLOUS_PAIEMENT_REQUEST))))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.OK)
                            .body(apiResponce));
            // WHEN
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH + REQUEST_PAYEMENT)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(paymentPostDtoAsJsonString)).andReturn();

            // THEN
            MockHttpServletResponse response = mvcResult.getResponse();
            String responseContentAsString = response.getContentAsString();

            RestException restException = jacksonObjectMapper.readValue(responseContentAsString, RestException.class);

            assertEquals("Identifiant boutique incorrect", restException.getMessage());
        }
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
            String paymentPostDtoAsJsonString = jacksonObjectMapper.writeValueAsString(paymentPost);
            TransactionHistory transactionHistory = TransactionHistory.builder()
                    .shopId("168")
                    .shopPassword("2pNJDYE31S")
                    .appTransactionId("26")
                    .providerTransactionId("")
                    .providerPayementId("").build();
            when(transactionHistoryRepository.save(any()))
                    .thenReturn(transactionHistory);
            mockSobliflouRequestApi();
            // WHEN
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH + REQUEST_PAYEMENT)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(paymentPostDtoAsJsonString)).andReturn();

            // THEN
            MockHttpServletResponse response = mvcResult.getResponse();
            String responseContentAsString = response.getContentAsString();

            RequestPaymentResponse requestPaymentResponse = jacksonObjectMapper.readValue(responseContentAsString, RequestPaymentResponse.class);

            assertNotNull(requestPaymentResponse);
            assertNotNull(requestPaymentResponse.getProviderUrl());
            assertNotNull(requestPaymentResponse.getProviderMobileUrl());
        }
    }

    @Test
    void GIVEN_call_check_payement_WHEN_sobflous_payement_should_throw_exeption() throws Exception {
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
            when(transactionHistoryRepository.findTransactionHistoryByAppTransactionIdAndEpaimentProvider(anyString(), any(PaymentMethod.class)))
                    .thenReturn(transactionHistory);
            CheckStatusRequest checkStatusRequest = CheckStatusRequest.builder()
                    .shopPassword("2pNJDYE31S")
                    .shopId("168")
                    .service(PaymentMethod.SOBFLOUS_SHOP)
                    .transactionId("26")
                    .build();
            String checkStatusRequestAsJsonString = jacksonObjectMapper.writeValueAsString(checkStatusRequest);

            mockServer.expect(ExpectedCount.once(), requestTo(new URI(String.format("%s%s", sobflousUri, SOBLIFLOUS_CHECK_PAYEMENT_STATUS))))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond((response) -> {
                        throw new RestClientException("error");
                    });

            MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH + CHECK_PAYEMENT_STATUS)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(checkStatusRequestAsJsonString)).andReturn();

            MockHttpServletResponse response1 = mvcResult2.getResponse();
            String responseContentAsString1 = response1.getContentAsString();

            RestException restException = jacksonObjectMapper.readValue(responseContentAsString1, RestException.class);
            assertNotNull(restException);
            assertEquals("error", restException.getMessage());
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
            when(transactionHistoryRepository.findTransactionHistoryByAppTransactionIdAndEpaimentProvider(anyString(), any(PaymentMethod.class)))
                    .thenReturn(transactionHistory);
            CheckStatusRequest checkStatusRequest = CheckStatusRequest.builder()
                    .shopPassword("2pNJDYE31S")
                    .shopId("168")
                    .service(PaymentMethod.SOBFLOUS_SHOP)
                    .transactionId("26")
                    .build();
            String checkStatusRequestAsJsonString = jacksonObjectMapper.writeValueAsString(checkStatusRequest);

            mockSobliflouCheckPayementApi();

            MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH + CHECK_PAYEMENT_STATUS)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(checkStatusRequestAsJsonString)).andReturn();

            MockHttpServletResponse response1 = mvcResult2.getResponse();
            String responseContentAsString1 = response1.getContentAsString();

            CheckStatusResponse checkStatusResponse = jacksonObjectMapper.readValue(responseContentAsString1, CheckStatusResponse.class);

            assertNotNull(checkStatusResponse);
            assertNotNull(checkStatusResponse.getTransactionStatus());
        }
    }

    @Test
    void GIVEN_requestToPayement_WHEN_clickToPay_payement_should_return_errer() throws Exception {
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
            String paymentPostAsJsonString = jacksonObjectMapper.writeValueAsString(paymentPost);

            String apiResponce =
                    "{\"errorCode\":\"1\",\"errorMessage\":\"Order number is duplicated, order with given order number is processed already\"}";
            mockServer.expect(ExpectedCount.once(), requestTo(new URI(String.format("%s%s", clickToPayUri, CLICKTOPAY_PAIEMENT_REQUEST))))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.OK)
                            .body(apiResponce));

            TransactionHistory transactionHistory = TransactionHistory.builder()
                    .shopId("168")
                    .shopPassword("2pNJDYE31S")
                    .appTransactionId("26")
                    .providerTransactionId("")
                    .providerPayementId("").build();
            when(transactionHistoryRepository.save(any()))
                    .thenReturn(transactionHistory);
            // WHEN
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH + REQUEST_PAYEMENT)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(paymentPostAsJsonString)).andReturn();

            // THEN
            MockHttpServletResponse response = mvcResult.getResponse();
            String responseContentAsString = response.getContentAsString();

            RestException restException = jacksonObjectMapper.readValue(responseContentAsString, RestException.class);
            assertNotNull(restException);
            assertEquals("NumÃ©ro de commande dupliquÃ©, commande avec le numÃ©ro de commande donner est dÃ©jÃ  traitÃ©e", restException.getMessage());
        }
    }

    @Test
    void GIVEN_requestToPayement_WHEN_clickToPay_payement_should_throw_exeption() throws Exception {

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
            String paymentPostAsJsonString = jacksonObjectMapper.writeValueAsString(paymentPost);

            String apiResponce =
                    "{\"errorCode\":\"1\",\"errorMessage\":\"Order number is duplicated, order with given order number is processed already\"}";
            mockServer.expect(ExpectedCount.once(), requestTo(new URI(String.format("%s%s", clickToPayUri, CLICKTOPAY_PAIEMENT_REQUEST))))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond((response) -> {
                        throw new RestClientException("error");
                    });

            TransactionHistory transactionHistory = TransactionHistory.builder()
                    .shopId("168")
                    .shopPassword("2pNJDYE31S")
                    .appTransactionId("26")
                    .providerTransactionId("")
                    .providerPayementId("").build();
            when(transactionHistoryRepository.save(any()))
                    .thenReturn(transactionHistory);
            // WHEN
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH + REQUEST_PAYEMENT)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(paymentPostAsJsonString)).andReturn();

            // THEN
            MockHttpServletResponse response = mvcResult.getResponse();
            String responseContentAsString = response.getContentAsString();

            RestException restException = jacksonObjectMapper.readValue(responseContentAsString, RestException.class);
            assertNotNull(restException);
            assertEquals("error", restException.getMessage());
        }
    }

    @Test
    void GIVEN_requestToPayement_WHEN_clickToPay_payement_should_return_reqonce() throws Exception {
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
            String paymentPostAsJsonString = jacksonObjectMapper.writeValueAsString(paymentPost);
            mockClickToPayRequestApi();
            TransactionHistory transactionHistory = TransactionHistory.builder()
                    .shopId("168")
                    .shopPassword("2pNJDYE31S")
                    .appTransactionId("26")
                    .providerTransactionId("")
                    .providerPayementId("").build();
            when(transactionHistoryRepository.save(any()))
                    .thenReturn(transactionHistory);
            // WHEN
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH + REQUEST_PAYEMENT)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(paymentPostAsJsonString)).andReturn();

            // THEN
            MockHttpServletResponse response = mvcResult.getResponse();
            String responseContentAsString = response.getContentAsString();

            RequestPaymentResponse requestPaymentResponse = jacksonObjectMapper.readValue(responseContentAsString, RequestPaymentResponse.class);

            assertNotNull(requestPaymentResponse);
            assertNotNull(requestPaymentResponse.getProviderUrl());
        }
    }

    @Test
    void GIVEN_call_check_payement_WHEN_clickToPay_payement_should_throw_exeption() throws Exception {
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
            String checkStatusRequestAsJsonString = jacksonObjectMapper.writeValueAsString(checkStatusRequest);
            mockServer.expect(ExpectedCount.once(), requestTo(new URI(String.format("%s%s", clickToPayUri, CLICKTOPAY_CHECK_PAYEMENT_STATUS))))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond((response) -> {
                        throw new RestClientException("error");
                    });
            // WHEN
            MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH + CHECK_PAYEMENT_STATUS)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(checkStatusRequestAsJsonString)).andReturn();

            // THEN
            MockHttpServletResponse response = mvcResult2.getResponse();
            String responseContentAsString = response.getContentAsString();

            RestException restException = jacksonObjectMapper.readValue(responseContentAsString, RestException.class);
            assertNotNull(restException);
            assertEquals("error", restException.getMessage());
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
            String checkStatusRequestAsJsonString = jacksonObjectMapper.writeValueAsString(checkStatusRequest);
            mockClickToPayCheckPayementApi();
            // WHEN
            MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH + CHECK_PAYEMENT_STATUS)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(checkStatusRequestAsJsonString)).andReturn();

            // THEN
            MockHttpServletResponse response = mvcResult2.getResponse();
            String responseContentAsString = response.getContentAsString();

            CheckStatusResponse requestPaymentResponse = jacksonObjectMapper.readValue(responseContentAsString, CheckStatusResponse.class);

            assertNotNull(requestPaymentResponse);
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

    private void mockClickToPayRequestApi() throws URISyntaxException {
        String apiResponce =
                "{\"orderId\":\"8ae1ac24-0c8b-767d-93c1-804400c2bb94\",\"formUrl\":\"https://test.clictopay.com/payment/merchants/CLICTOPAY/payment_fr.html?mdOrder=8ae1ac24-0c8b-767d-93c1-804400c2bb94\"}";
        mockServer.expect(ExpectedCount.once(), requestTo(new URI(String.format("%s%s", clickToPayUri, CLICKTOPAY_PAIEMENT_REQUEST))))
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

    private void mockClickToPayCheckPayementApi() throws URISyntaxException {
        String apiResponce =
                "{\"depositAmount\":0,\"currency\":\"788\",\"authCode\":2,\"ErrorCode\":\"2\",\"ErrorMessage\":\"Payment is declined\",\"OrderStatus\":6,\"OrderNumber\":\"140\",\"Amount\":900}";
        mockServer.expect(ExpectedCount.once(), requestTo(new URI(String.format("%s%s", clickToPayUri, CLICKTOPAY_CHECK_PAYEMENT_STATUS))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .body(apiResponce));
    }
}