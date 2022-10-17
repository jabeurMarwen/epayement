package com.mj.epayement.domain.clicktopay.exception;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum ClickToPayVerifyPayementExeption {

    NO_SYSTEM_ERROR("0", "Aucune erreur système"),
    ORDER_REFUSED("2", "L'ordre est refusé en raison d'une erreur dans les informations d'identification de paiement"),
    DENIED_ACCESS("5", "Accès refusé"),
    ORDERID_NOT_REGISTERED("6", "OrderId non enregistré"),
    SYSTEM_ERROR("7", "Erreur système.");


    private final String code;
    private final String message;

    ClickToPayVerifyPayementExeption(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ClickToPayVerifyPayementExeption getByCode(String code) {
        return Arrays.stream(ClickToPayVerifyPayementExeption.values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
