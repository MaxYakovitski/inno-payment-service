package com.innowise.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.innowise.paymentservice.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponseDto(
    String id,
    @JsonProperty("order_id") UUID orderId,
    @JsonProperty("user_id") UUID userId,
    PaymentStatus paymentStatus,
    Instant timestamp,
    @JsonProperty("payment_amount") BigDecimal paymentAmount) {}
