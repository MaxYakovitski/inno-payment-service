package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepositoryCustom {

  List<Payment> search(UUID orderId, UUID userId, PaymentStatus status);

  Optional<BigDecimal> sumForUser(UUID userId, Instant from, Instant to);

  Optional<BigDecimal> sumForAllUsers(Instant from, Instant to);
}
