package com.innowise.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCreateDto(
    @JsonProperty("order_id") @NotNull UUID orderId,
    @JsonProperty("payment_amount") @NotNull @Positive BigDecimal paymentAmount) {}
