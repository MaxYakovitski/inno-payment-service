package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.kafka.PaymentEventPublisher;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentGatewayClient;
import com.innowise.paymentservice.service.PaymentProcessingService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingServiceImpl implements PaymentProcessingService {

  private final PaymentRepository paymentRepository;
  private final PaymentGatewayClient gatewayClient;
  private final PaymentEventPublisher eventPublisher;

  @Override
  @Scheduled(fixedRateString = "${payment.processing.interval-ms:3000}")
  public void processPendingPayments() {
    Optional<Payment> claimed = paymentRepository.claimNextPending();
    claimed.ifPresent(this::processOne);
  }

  private void processOne(Payment payment) {
    try {
      PaymentStatus resolvedStatus = gatewayClient.determinateStatus();
      payment.setStatus(resolvedStatus);
      paymentRepository.save(payment);
      eventPublisher.publishPaymentCompleted(payment.getOrderId(), resolvedStatus);
    } catch (Exception e) {
      log.error("Error processing payment", e);
      revertToPending(payment);
    }
  }

  private void revertToPending(Payment payment) {
    payment.setStatus(PaymentStatus.PENDING);
    paymentRepository.save(payment);
  }
}
