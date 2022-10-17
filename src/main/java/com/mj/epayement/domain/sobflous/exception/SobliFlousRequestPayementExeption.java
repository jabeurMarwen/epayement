package com.mj.epayement.domain.sobflous.exception;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum SobliFlousRequestPayementExeption {

    INCORRECT_PARAMETERS("201", "Les paramètres du web service sont incorrects"),
    INCORRECT_TYPE_PARAMETERS("202", "Le type de l’un des paramètres du web service est incorrect"),
    AMOUNT_OUT_RANGE("203", "Le montant hors plage (lemontant doit être entre montant_min et montant_max)"),
    TRANSACTION_EXCEEDED("204",
            "Date transaction du client dépassée(le clientne peut passerqu’une seule transaction toutes les 2 minutes pour éviter le spam)"),
    INCORRECT_STORE_ID("205", "Identifiant boutique incorrect"),
    INCORRECT_IP("206", "Adresse ip du marchand est incorrect"),
    STORE_ID_OFF("207", "La boutique du marchand est OFF"),
    ACCOUNT_STORE_OFF("208", "Le compte marchand est OFF"),
    MERCHANT_ACCOUNT_INACTIVE_OR_BLOCKED("209", "Le compte marchand est inactif ou bloqué"),
    INCORRECT_TOKEN("210", "Le token est incorrect"),
    MERCHANT_SERVICE_UNAVAILABLE("211", "Service marchand indisponible"),
    TRANSACTION_ID_EXISTS("212", "Id transaction marchand existe déjà"),
    INVALID_BONUS("213", "Pourcentage Bonus variable invalide");


    private final String code;
    private final String message;

    SobliFlousRequestPayementExeption(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static SobliFlousRequestPayementExeption getByCode(String code) {
        return Arrays.stream(SobliFlousRequestPayementExeption.values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
