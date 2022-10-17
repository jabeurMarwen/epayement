package com.mj.epayement.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * @author Marwen JABEUR
 * date: 14/10/2022
 */
@Configuration
public class EPaymentAuditorAware implements AuditorAware<String> {


    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(getCurrentUsername());
    }

    public static String getCurrentUsername() {
        //return SecurityContextHolder.getContext().getAuthentication().getName();
        return "";
    }
}
