package com.innowise.paymentservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class PaymentExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException e) {
    log.warn("Resource not found: {}", e.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    problem.setProperty("code", "not_found");
    return problem;
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ProblemDetail handleNoResource(NoResourceFoundException e) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Resource not found");
    problem.setProperty("code", "not_found");
    return problem;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(f -> f.getField() + ":" + f.getDefaultMessage())
            .orElse("Validation failed");
    log.warn("Validation failed: {}", e.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
    problem.setProperty("code", "validation_failed");
    return problem;
  }

  @ExceptionHandler(AuthenticationException.class)
  public ProblemDetail handleUnauthenticated(AuthenticationException e) {
    log.warn("Unauthenticated request: {}", e.getMessage());
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Authentication required");
    problem.setProperty("code", "unauthorized");
    return problem;
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDenied(AccessDeniedException e) {
    log.warn("Access denied: {}", e.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied");
    problem.setProperty("code", "access_denied");
    return problem;
  }

  @ExceptionHandler(DuplicateKeyException.class)
  public ProblemDetail handleDuplicateKey(DuplicateKeyException e) {
    log.warn("Duplicate key: {}", e.getMessage());
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, "An active payment already exists for this order");
    problem.setProperty("code", "conflict");
    return problem;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception e) {
    log.error("unexpected error", e);
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error");
    problem.setProperty("code", "internal_error");
    return problem;
  }
}
