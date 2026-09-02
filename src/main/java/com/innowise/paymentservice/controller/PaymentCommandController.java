package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.dto.PaymentCreateDto;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.security.CurrentUserId;
import com.innowise.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentCommandController {

  private final PaymentService paymentService;

  @PostMapping
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  public ResponseEntity<PaymentResponseDto> create(
      @Valid @RequestBody PaymentCreateDto dto, @CurrentUserId String userId) {
    PaymentResponseDto payment = paymentService.create(dto, UUID.fromString(userId));
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(payment);
  }
}
