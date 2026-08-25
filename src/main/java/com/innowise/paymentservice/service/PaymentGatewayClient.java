package com.innowise.paymentservice.service;

import com.innowise.paymentservice.entity.PaymentStatus;

public interface PaymentGatewayClient {
  PaymentStatus determinateStatus();
}
