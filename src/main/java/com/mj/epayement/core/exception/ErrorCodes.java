package com.mj.epayement.core.exception;


public enum ErrorCodes {

    REST_EXCEPTION(1001),
    CLIENT_EXEPTION(1002);


    private final int code;

    ErrorCodes(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
