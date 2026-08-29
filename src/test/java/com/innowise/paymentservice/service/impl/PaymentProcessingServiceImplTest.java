package com.innowise.paymentservice.service.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.kafka.PaymentEventPublisher;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentGatewayClient;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentProcessingServiceImplTest {

  @Mock private PaymentRepository paymentRepository;
  @Mock private PaymentGatewayClient gatewayClient;
  @Mock private PaymentEventPublisher eventPublisher;

  @InjectMocks private PaymentProcessingServiceImpl processingService;

  UUID orderId = UUID.randomUUID();
  Payment payment =
      Payment.builder().id("abc123").orderId(orderId).status(PaymentStatus.PROCESSING).build();

  @Test
  void processPendingPayments_does_nothing_when_no_pending_payment() {
    when(paymentRepository.claimNextPending()).thenReturn(Optional.empty());

    processingService.processPendingPayments();
    verifyNoInteractions(gatewayClient, eventPublisher);
  }

  @Test
  void processPendingPayments_saves_resolved_status_and_publishes_on_gateway_success() {
    when(paymentRepository.claimNextPending()).thenReturn(Optional.of(payment));
    when(gatewayClient.determinateStatus()).thenReturn(PaymentStatus.SUCCESS);

    processingService.processPendingPayments();

    ArgumentCaptor<Payment> savedCaptor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository).save(savedCaptor.capture());
    assertThat(savedCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.SUCCESS);

    verify(eventPublisher).publishPaymentCompleted(orderId, PaymentStatus.SUCCESS);
  }

  @Test
  void processPendingPayments_reverts_to_pending_when_gateway_throws() {
    when(paymentRepository.claimNextPending()).thenReturn(Optional.of(payment));
    when(gatewayClient.determinateStatus()).thenThrow(new RuntimeException("gateway down"));

    processingService.processPendingPayments();

    ArgumentCaptor<Payment> savedCaptor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository, times(1)).save(savedCaptor.capture());
    assertThat(savedCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.PENDING);

    verify(eventPublisher, never()).publishPaymentCompleted(any(), any());
  }
}
