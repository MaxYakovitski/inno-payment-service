package com.innowise.paymentservice.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;

class PaymentExceptionHandlerTest {

  private final PaymentExceptionHandler handler = new PaymentExceptionHandler();

  @Test
  void handleResourceNotFoundException_returns_404_with_code() {
    ProblemDetail result =
        handler.handleResourceNotFoundException(new ResourceNotFoundException("not found"));

    assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(result.getProperties()).containsEntry("code", "not_found");
  }

  @Test
  void handleAccessDenied_returns_403_with_code() {
    ProblemDetail result = handler.handleAccessDenied(new AccessDeniedException("denied"));

    assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(result.getProperties()).containsEntry("code", "access_denied");
  }

  @Test
  void handleDuplicateKey_returns_409_with_code() {
    ProblemDetail result = handler.handleDuplicateKey(new DuplicateKeyException("duplicate"));

    assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    assertThat(result.getProperties()).containsEntry("code", "conflict");
  }

  @Test
  void handleUnexpected_returns_500_with_code() {
    ProblemDetail result = handler.handleUnexpected(new RuntimeException("boom"));

    assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(result.getProperties()).containsEntry("code", "internal_error");
  }
}
