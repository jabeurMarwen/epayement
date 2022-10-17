package com.mj.epayement.shared.model;

import lombok.Getter;

@Getter
public enum PaymentMethod {

    SOBFLOUS_SHOP ("SOBFLOUS_SHOP"),
    CLICKTOPAY_SHOP("CLICKTOPAY_SHOP");

    private String name;

    PaymentMethod(String name){
        this.name = name;
    }
}
