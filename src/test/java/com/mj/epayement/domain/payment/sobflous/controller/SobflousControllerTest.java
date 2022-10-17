package com.mj.epayement.domain.payment.sobflous.controller;

import java.net.URI;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mj.epayement.EpayementApplication;
import com.mj.epayement.core.config.EPaymentAuditorAware;
import com.mj.epayement.core.exception.RestException;
import com.mj.epayement.shared.entity.TransactionHistory;
import com.mj.epayement.shared.model.RequestPaymentRequest;
import com.mj.epayement.shared.repository.TransactionHistoryRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = EpayementApplication.class)
@ActiveProfiles("test")
class SobflousControllerTest {

    public static final String SOBLIFLOUS_VERIFY_PAIEMENT = "/verificationtransaction";
    private static final String SOBFLOUS = "/sobflous";
    private static final String CONFIRM_PAYMENT = "/confirm-payment";

    @Value("${application.feign.sobflous.uri}")
    private String sobflousUri;

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
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(SOBFLOUS + "/invalid-uri/no-think")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(paymentPostDtoAsJsonString)).andReturn();

            // THEN
            int status = mvcResult.getResponse().getStatus();
            assertEquals(status, HttpStatus.NOT_FOUND.value());
        }
    }

    @Test
    void GIVEN_call_invalid_uri_WHEN_make_call_then_should_return_bad_request() throws Exception {
        try (MockedStatic<EPaymentAuditorAware> utilities = Mockito.mockStatic(EPaymentAuditorAware.class)) {
            utilities.when(EPaymentAuditorAware::getCurrentUsername)
                    .thenReturn("Default auditor");

            // WHEN
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(SOBFLOUS + CONFIRM_PAYMENT)).andReturn();

            // THEN
            int status = mvcResult.getResponse().getStatus();
            assertEquals(status, HttpStatus.NO_CONTENT.value());
        }
    }

    @Test
    void GIVEN_call_valid_uri_and_bad_shop_WHEN_make_call_then_should_return_reqonce() throws Exception {
        try (MockedStatic<EPaymentAuditorAware> utilities = Mockito.mockStatic(EPaymentAuditorAware.class)) {
            utilities.when(EPaymentAuditorAware::getCurrentUsername)
                    .thenReturn("Default auditor");
            TransactionHistory transactionHistory = TransactionHistory.builder()
                    .shopId("168")
                    .shopPassword("2pNJDYE31S")
                    .appTransactionId("26")
                    .providerTransactionId("")
                    .providerPayementId("").build();
            when(transactionHistoryRepository.findTransactionHistoryByAppTransactionId(anyString()))
                    .thenReturn(transactionHistory);

            String apiResponce = "{\"result\":\"213\"}";
            mockServer.expect(ExpectedCount.once(), requestTo(new URI(String.format("%s%s", sobflousUri, SOBLIFLOUS_VERIFY_PAIEMENT))))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.OK)
                            .body(apiResponce));
            // WHEN
            var uri = String.format("%s%s?transm_id=%s&state=s", SOBFLOUS, CONFIRM_PAYMENT, 26);
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(uri)).andReturn();

            // THEN
            MockHttpServletResponse response = mvcResult.getResponse();
            String responseContentAsString = response.getContentAsString();

            RestException restException = jacksonObjectMapper.readValue(responseContentAsString, RestException.class);

            assertEquals("Identifiant boutique incorrect", restException.getMessage());
        }
    }

}