package com.mj.epayement.core.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class SobflousUtilsTest {

    @Test
    void GIVEN_string_THEN_should_return_sobflous_token() {
        var token = SobflousUtils.generateSobflousToken("2pNJDYE31S",
                "168",
                "200");
        String md5 = "2bd0e03187314cc8cd3e2d344047be49a102068cd6a1cb46ca3beda03d9092f177d40229bab0439d5a9290fe4be9deb8d9de9dd6cdf2c21df0ba5fa2373282df";
        assertEquals(token, md5);
    }

}