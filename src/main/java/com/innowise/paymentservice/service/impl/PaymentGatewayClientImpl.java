package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.service.PaymentGatewayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class PaymentGatewayClientImpl implements PaymentGatewayClient {

  private final RestClient restClient;

  @Value("${random-org.integers-path}")
  private String integerPath;

  @Override
  public PaymentStatus determinateStatus() {
    var response = restClient.get().uri(integerPath).retrieve().body(String.class);
    if (response == null) {
      throw new IllegalStateException("random.org returned empty response body");
    }
    var number = Integer.parseInt(response);
    return number % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
  }
}
