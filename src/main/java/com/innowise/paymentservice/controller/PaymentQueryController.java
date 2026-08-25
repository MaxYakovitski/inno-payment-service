package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.dto.PaymentSumResponse;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.security.CurrentUserId;
import com.innowise.paymentservice.service.PaymentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentQueryController {

  private final PaymentService paymentService;

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN' or @paymentGuard.isOwner(#id, authentication.name))")
  public ResponseEntity<PaymentResponseDto> getById(@PathVariable String id) {
    return ResponseEntity.ok(paymentService.getById(id));
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<PaymentResponseDto>> search(
      @RequestParam(required = false) UUID orderId,
      @RequestParam(required = false) UUID userId,
      @RequestParam(required = false) PaymentStatus status,
      @CurrentUserId String currentUserId,
      Authentication authentication) {

    UUID effectiveUserId = isAdmin(authentication) ? userId : UUID.fromString(currentUserId);
    return ResponseEntity.ok(paymentService.search(orderId, effectiveUserId, status));
  }

  @GetMapping("/users/{user_id}/summary")
  @PreAuthorize("hasRole('ADMIN') or #userId == authentication.name")
  public ResponseEntity<PaymentSumResponse> getUserSum(
      @PathVariable("user_id") UUID userId, @RequestParam Instant from, @RequestParam Instant to) {
    BigDecimal total = paymentService.sumForUser(userId, from, to).orElse(BigDecimal.ZERO);
    return ResponseEntity.ok(new PaymentSumResponse(total));
  }

  @GetMapping("/summary")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PaymentSumResponse> getAppSum(
      @RequestParam Instant from, @RequestParam Instant to) {
    BigDecimal total = paymentService.sumForAllUsers(from, to).orElse(BigDecimal.ZERO);
    return ResponseEntity.ok(new PaymentSumResponse(total));
  }

  private boolean isAdmin(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
  }
}
