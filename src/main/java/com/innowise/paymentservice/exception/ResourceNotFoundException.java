package com.innowise.paymentservice.exception;

public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String message) {
    super(message);
  }

  public static ResourceNotFoundException payment(String id) {
    return new ResourceNotFoundException("Payment not found: " + id);
  }
}
