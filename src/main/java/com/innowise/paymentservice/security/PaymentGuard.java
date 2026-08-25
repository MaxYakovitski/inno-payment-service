package com.innowise.paymentservice.security;

import com.innowise.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("paymentGuard")
@RequiredArgsConstructor
public class PaymentGuard {

    private final PaymentRepository paymentRepository;

    public boolean isOwner(String id, String userId) {
        return paymentRepository.findById(id).
                map(payment -> payment.getUserId().toString().equals(userId))
                .orElse(false);
    }
}
