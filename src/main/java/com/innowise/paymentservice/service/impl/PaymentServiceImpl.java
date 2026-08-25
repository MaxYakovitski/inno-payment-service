package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.dto.PaymentCreateDto;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.exception.ResourceNotFoundException;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentMapper paymentMapper;

  @Override
  public PaymentResponseDto create(PaymentCreateDto request, UUID userId) {
    Payment payment = paymentMapper.toEntity(request, userId);
    payment.setTimestamp(Instant.now());
    Payment saved = paymentRepository.save(payment);
    return paymentMapper.toDto(saved);
  }

  @Override
  public PaymentResponseDto getById(String id) {
    Payment payment =
        paymentRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.payment(id));
    return paymentMapper.toDto(payment);
  }

  @Override
  public List<PaymentResponseDto> search(UUID orderId, UUID userId, PaymentStatus status) {
    List<Payment> payments = paymentRepository.search(orderId, userId, status);
    return paymentMapper.toDtoList(payments);
  }

  @Override
  public Optional<BigDecimal> sumForUser(UUID userId, Instant from, Instant to) {
    return paymentRepository.sumForUser(userId, from, to);
  }

  @Override
  public Optional<BigDecimal> sumForAllUsers(Instant from, Instant to) {
    return paymentRepository.sumForAllUsers(from, to);
  }
}
