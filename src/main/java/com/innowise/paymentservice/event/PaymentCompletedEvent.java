package com.innowise.paymentservice.event;

import com.innowise.paymentservice.entity.PaymentStatus;
import java.util.UUID;

public record PaymentCompletedEvent(UUID orderId, PaymentStatus status) {}
