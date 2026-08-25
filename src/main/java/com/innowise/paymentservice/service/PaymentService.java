package com.innowise.paymentservice.service;

import com.innowise.paymentservice.dto.PaymentCreateDto;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentService {

  PaymentResponseDto create(PaymentCreateDto request, UUID userId);

  PaymentResponseDto getById(String id);

  List<PaymentResponseDto> search(UUID orderId, UUID userId, PaymentStatus status);

  Optional<BigDecimal> sumForUser(UUID userId, Instant from, Instant to);

  Optional<BigDecimal> sumForAllUsers(Instant from, Instant to);
}
