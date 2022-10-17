package com.mj.epayement.core.utils;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class JsonUtilTest {

    @Test
    void GIVEN_string_THEN_should_return_gson() {
        var str1 = "{\"orderId\":\"6ffb5689-f21a-7559-aaaa-950100c2bb94\",\"formUrl\":\"https://test.clictopay.com/payment/merchants/CLICTOPAY/payment_fr.html?mdOrder=6ffb5689-f21a-7559-aaaa-950100c2bb94\"}";
        var str = "{\n" +
                "  \"amount\": \"100\",\n" +
                "  \"applicationId\": \"100\",\n" +
                "  \"backUrl\": \"https://int.klubkissa.com/auth/login\",\n" +
                "  \"bonusVariable\": null,\n" +
                "  \"currency\": \"TND\",\n" +
                "  \"customerId\": \"5\",\n" +
                "  \"paymentId\": \"5\",\n" +
                "  \"service\": \"CLICKTOPAY_SHOP\",\n" +
                "  \"shopId\": \"0402522023\",\n" +
                "  \"shopPassword\": \"XSc74s2c\"\n" +
                "}";
        var json = JsonUtil.stringToJsonObject(str);
        assertThat(json.isJsonObject(), is(true));
        var amount = JsonUtil.getAsString(json, "amount");
        assertThat(amount, is(notNullValue()));
    }
    @Test
    void GIVEN_clicktoPay_responce_string_THEN_should_return_gson1() {
        var str = "{\"orderId\":" +
                "\"6ffb5689-f21a-7559-aaaa-950100c2bb94\"," +
                "\"formUrl\":" +
                "\"https://test.clictopay.com/payment/merchants/CLICTOPAY/payment_fr.html?mdOrder=6ffb5689-f21a-7559-aaaa-950100c2bb94\"" +
                "}";
        var json = JsonUtil.stringToJsonObject(str);
        assertThat(json.isJsonObject(), is(true));
        var amount = JsonUtil.getAsString(json, "orderId");
        assertThat(amount, is(notNullValue()));
    }
}