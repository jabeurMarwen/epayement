package com.mj.epayement.domain.clicktopay.exception;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum ClickToPayRequestPayementExeption {

    NO_SYSTEM_ERROR("0", "Pas d'erreur système"),
    DUPLICATE_ORDER_NUMBER("1", "Numéro de commande dupliqué, commande avec le numéro de commande donner est déjà traitée"),
    UNKNOWN_CURRENCY("3", "Monnaie inconnue"),
    MANDATORY_PARAMETER_WAS_NOT_SPECIFIED("4", "Paramètre obligatoire n'a pas été spécifié"),
    WRONG_VALUE_OF_QUERY_PARAMETER("5", "Valeur erronée d'un paramètre de la requête"),
    SYSTEM_ERROR("7", "Erreur système.");

    private final String code;
    private final String message;

    ClickToPayRequestPayementExeption(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ClickToPayRequestPayementExeption getByCode(String code) {
        return Arrays.stream(ClickToPayRequestPayementExeption.values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
