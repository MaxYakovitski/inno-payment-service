package com.innowise.paymentservice.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.paymentservice.dto.PaymentCreateDto;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.exception.ResourceNotFoundException;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

  @Mock private PaymentRepository paymentRepository;
  @Mock private PaymentMapper paymentMapper;

  @InjectMocks private PaymentServiceImpl paymentService;

  private UUID userId;
  private UUID orderId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    orderId = UUID.randomUUID();
  }

  @Test
  void create_saves_payment_and_returns_dto() {
    PaymentCreateDto request = new PaymentCreateDto(orderId, new BigDecimal("10.00"));
    Payment entity =
        Payment.builder().orderId(orderId).userId(userId).status(PaymentStatus.PENDING).build();
    Payment saved =
        Payment.builder()
            .id("abc123")
            .orderId(orderId)
            .userId(userId)
            .status(PaymentStatus.PENDING)
            .timestamp(Instant.now())
            .paymentAmount(new BigDecimal("10.00"))
            .build();
    PaymentResponseDto expectedResponse =
        new PaymentResponseDto(
            "abc123",
            orderId,
            userId,
            PaymentStatus.PENDING,
            saved.getTimestamp(),
            new BigDecimal("10.00"));

    when(paymentMapper.toEntity(request, userId)).thenReturn(entity);
    when(paymentRepository.save(any(Payment.class))).thenReturn(saved);
    when(paymentMapper.toDto(saved)).thenReturn(expectedResponse);

    PaymentResponseDto result = paymentService.create(request, userId);

    assertThat(result).isEqualTo(expectedResponse);
    verify(paymentRepository).save(any(Payment.class));
  }

  @Test
  void getById_returns_dto_when_found() {
    Payment payment = Payment.builder().id("abc123").orderId(orderId).userId(userId).build();
    PaymentResponseDto expected =
        new PaymentResponseDto(
            "abc123",
            orderId,
            userId,
            PaymentStatus.SUCCESS,
            Instant.now(),
            new BigDecimal("10.00"));

    when(paymentRepository.findById("abc123")).thenReturn(Optional.of(payment));
    when(paymentMapper.toDto(payment)).thenReturn(expected);

    PaymentResponseDto result = paymentService.getById("abc123");
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void getById_throws_when_not_found() {
    when(paymentRepository.findById("missing")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> paymentService.getById("missing"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void search_delegatesToRepositoryAndMapsResults() {
    PaymentStatus status = PaymentStatus.SUCCESS;
    List<Payment> payments = List.of(Payment.builder().orderId(orderId).userId(userId).build());
    List<PaymentResponseDto> expected =
        List.of(
            new PaymentResponseDto(null, orderId, userId, status, Instant.now(), BigDecimal.ONE));

    when(paymentRepository.search(orderId, userId, status)).thenReturn(payments);
    when(paymentMapper.toDtoList(payments)).thenReturn(expected);

    List<PaymentResponseDto> result = paymentService.search(orderId, userId, status);
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void sumForUser_returns_repository_result() {
    Instant from = Instant.now().minusSeconds(3600);
    Instant to = Instant.now();
    when(paymentRepository.sumForUser(userId, from, to)).thenReturn(Optional.of(BigDecimal.TEN));

    Optional<BigDecimal> result = paymentService.sumForUser(userId, from, to);
    assertThat(result).contains(BigDecimal.TEN);
  }

  @Test
  void sumForAllUsers_returns_empty_when_no_matches() {
    Instant from = Instant.now().minusSeconds(3600);
    Instant to = Instant.now();
    when(paymentRepository.sumForAllUsers(from, to)).thenReturn(Optional.empty());

    Optional<BigDecimal> result = paymentService.sumForAllUsers(from, to);
    assertThat(result).isEmpty();
  }
}
