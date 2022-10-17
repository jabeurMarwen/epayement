package com.mj.epayement.domain.payment.sobflous.exception;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum SobliFlousVerifyPayementExeption {

    INCORRECT_PARAMETERS("211", "Les paramètres du web service sont incorrects"),
    INCORRECT_TYPE_PARAMETERS("212", "Le type de l’un des paramètres du web service est incorrect"),
    INCORRECT_STORE_ID("213", "Identifiant boutique incorrect"),
    INCORRECT_IP("214", "Adresse ip du marchand est incorrect"),
    STORE_ID_OFF("215", "La boutique du marchand est OFF"),
    ACCOUNT_STORE_OFF("216", "Le compte marchand est OFF"),
    MERCHANT_ACCOUNT_INACTIVE_OR_BLOCKED("217", "Le compte marchand est inactif ou bloqué"),
    INCORRECT_TOKEN("218", "Le token est incorrect"),
    TIMEOUT("219", "Timeout"),
    UNKNOWN_TRANSACTION("220", "Transaction inconnue"),
    INSUFFICIENT_BALANCE("221", "Solde client insuffisant"),
    TECHNICAL_PROBLEM("222", "Problème technique"),
    UNKNOWN_EXPIRED_TRANSACTION("223", "Transaction inconnue ou expirée");


    private final String code;
    private final String message;

    SobliFlousVerifyPayementExeption(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static SobliFlousVerifyPayementExeption getByCode(String code) {
        return Arrays.stream(SobliFlousVerifyPayementExeption.values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
