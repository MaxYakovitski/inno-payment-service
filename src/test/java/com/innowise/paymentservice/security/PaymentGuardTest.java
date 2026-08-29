package com.innowise.paymentservice.security;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.repository.PaymentRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentGuardTest {

  @Mock private PaymentRepository paymentRepository;

  @InjectMocks private PaymentGuard paymentGuard;

  UUID userId = UUID.randomUUID();
  Payment payment = Payment.builder().id("abc123").userId(userId).build();

  @Test
  void isOwner_returns_true_when_userId_matches() {
    when(paymentRepository.findById("abc123")).thenReturn(Optional.of(payment));
    boolean result = paymentGuard.isOwner("abc123", userId.toString());
    assertThat(result).isTrue();
  }

  @Test
  void isOwner_returns_false_when_userId_does_not_match() {
    when(paymentRepository.findById("abc123")).thenReturn(Optional.of(payment));
    boolean result = paymentGuard.isOwner("abc123", UUID.randomUUID().toString());
    assertThat(result).isFalse();
  }

  @Test
  void isOwner_returnsFalse_whenPaymentNotFound() {
    when(paymentRepository.findById("missing")).thenReturn(Optional.empty());
    boolean result = paymentGuard.isOwner("missing", UUID.randomUUID().toString());
    assertThat(result).isFalse();
  }
}
