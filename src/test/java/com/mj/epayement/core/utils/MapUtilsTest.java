package com.mj.epayement.core.utils;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;


class MapUtilsTest {

    @Test
    void GIVEN_parameters_THEN_should_return_linkedMultiValueMap() {
        var map = MapUtils.generateLinkedMultiValueMap(Collections.singletonList(ClickToPayJsonProperties.ORDER_ID),
                Collections.singletonList("111"));
        assertNotNull(map);
    }

}