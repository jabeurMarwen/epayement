package com.mj.epayement.core.handler;

import java.util.ArrayList;
import java.util.List;

import com.mj.epayement.core.exception.ErrorCodes;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
class ErrorDto {
    private Integer httpCode;
    private ErrorCodes code;
    private String message;
    private List<String> errors=new ArrayList<>();
}
