package com.mj.epayement.core.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Marwen JABEUR
 * date: 15/07/2021
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SobflousUtils {

    /**
     * Generates access token for Sobflous API Calls
     * @param parameters @{@link String}
     * @return @{@link String}
     */
    public static String generateSobflousToken(String... parameters) {
        var sb = new StringBuilder();
        for (String parameter : parameters) {
            sb.append(Hex.encodeHexString(DigestUtils.md5(parameter)));
        }
        byte[] token = DigestUtils.sha512(sb.toString());
        return Hex.encodeHexString(token);
    }
}
