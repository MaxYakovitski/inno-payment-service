package com.innowise.paymentservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.innowise.paymentservice.config.SecurityConfig;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.security.PaymentGuard;
import com.innowise.paymentservice.service.PaymentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentCommandController.class)
@Import(SecurityConfig.class)
class PaymentCommandControllerTest {

  private static final String USER_ID = "11111111-1111-1111-1111-111111111111";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private PaymentService paymentService;

  @MockitoBean private PaymentGuard paymentGuard;

  @Test
  void create_returns_202_accepted_for_authenticated_user() throws Exception {
    UUID orderId = UUID.randomUUID();
    PaymentResponseDto response =
        new PaymentResponseDto(
            "abc123",
            orderId,
            UUID.fromString(USER_ID),
            PaymentStatus.PENDING,
            Instant.now(),
            new BigDecimal("10.00"));

    when(paymentService.create(any(), any())).thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/payments")
                .with(
                    jwt()
                        .jwt(j -> j.subject(USER_ID))
                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"order_id\": \"%s\", \"payment_amount\": 10.00}".formatted(orderId)))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  void create_returns_401_for_unauthenticated_user() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"order_id\": \"%s\", \"payment_amount\": 10.00}"
                        .formatted(UUID.randomUUID())))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void create_returns_400_when_payment_amount_missing() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/payments")
                .with(
                    jwt()
                        .jwt(j -> j.subject(USER_ID))
                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"order_id\": \"%s\"}".formatted(UUID.randomUUID())))
        .andExpect(status().isBadRequest());
  }
}
