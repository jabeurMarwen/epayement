package com.mj.epayement.domain.payment.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mj.epayement.domain.payment.PaymentStrategy;
import com.mj.epayement.shared.model.PaymentMethod;

@Component
public class PaymentFactory {

    Map<PaymentMethod, PaymentStrategy> map;

    @Autowired
    public PaymentFactory(Set<PaymentStrategy> paymentMethodSet) {
        createStrategy(paymentMethodSet);
    }

    private void createStrategy(Set<PaymentStrategy> paymentMethodSet) {
        map = new HashMap<>();
        paymentMethodSet.stream().forEach(paymentStrategy -> map.put(paymentStrategy.getPayementMethod(), paymentStrategy));
    }
    public PaymentStrategy findPayementMethod(PaymentMethod paymentMethod){
        return map.get(paymentMethod);
    }

}
