package com.innowise.paymentservice.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.innowise.paymentservice.config.SecurityConfig;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.security.PaymentGuard;
import com.innowise.paymentservice.service.PaymentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentQueryController.class)
@Import(SecurityConfig.class)
class PaymentQueryControllerTest {

  private static final String USER_ID = "11111111-1111-1111-1111-111111111111";
  private static final String OTHER_USER_ID = "22222222-2222-2222-2222-222222222222";

  private static final String FROM = "from";
  private static final String FROM_DATE = "2026-01-01T00:00:00Z";
  private static final String TO = "to";
  private static final String TO_DATE = "2026-12-31T23:59:59Z";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private PaymentService paymentService;

  @MockitoBean(name = "paymentGuard")
  private PaymentGuard paymentGuard;

  PaymentResponseDto response =
      new PaymentResponseDto(
          "abc123",
          UUID.randomUUID(),
          UUID.fromString(USER_ID),
          PaymentStatus.SUCCESS,
          Instant.now(),
          BigDecimal.TEN);

  @Test
  void getById_returns_payment_when_owner() throws Exception {
    when(paymentGuard.isOwner("abc123", USER_ID)).thenReturn(true);
    when(paymentService.getById("abc123")).thenReturn(response);

    mockMvc
        .perform(
            get("/api/v1/payments/abc123")
                .with(
                    jwt()
                        .jwt(j -> j.subject(USER_ID))
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("abc123"));
  }

  @Test
  void getById_returns_403_when_not_owner_and_not_admin() throws Exception {
    when(paymentGuard.isOwner("abc123", USER_ID)).thenReturn(false);

    mockMvc
        .perform(
            get("/api/v1/payments/abc123")
                .with(
                    jwt()
                        .jwt(j -> j.subject(USER_ID))
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void getById_allows_access_for_admin_regardless_of_ownership() throws Exception {
    when(paymentService.getById("abc123")).thenReturn(response);

    mockMvc
        .perform(
            get("/api/v1/payments/abc123")
                .with(
                    jwt()
                        .jwt(j -> j.subject(USER_ID))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk());
  }

  @Test
  void search_overrides_requested_userId_for_non_admin() throws Exception {
    UUID requestedUserId = UUID.fromString(OTHER_USER_ID);
    when(paymentService.search(isNull(), eq(UUID.fromString(USER_ID)), isNull()))
        .thenReturn(List.of());

    mockMvc
        .perform(
            get("/api/v1/payments")
                .param("userId", requestedUserId.toString())
                .with(
                    jwt()
                        .jwt(j -> j.subject(USER_ID))
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isOk());

    verify(paymentService).search(isNull(), eq(UUID.fromString(USER_ID)), isNull());
  }

  @Test
  void search_keeps_requested_userId_for_admin() throws Exception {
    UUID requestedUserId = UUID.fromString(OTHER_USER_ID);
    when(paymentService.search(isNull(), eq(requestedUserId), isNull())).thenReturn(List.of());

    mockMvc
        .perform(
            get("/api/v1/payments")
                .param("userId", requestedUserId.toString())
                .with(
                    jwt()
                        .jwt(j -> j.subject(USER_ID))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk());

    verify(paymentService).search(isNull(), eq(requestedUserId), isNull());
  }

  @Test
  void getUserSum_returns_zero_when_no_payments() throws Exception {
    when(paymentService.sumForUser(eq(UUID.fromString(USER_ID)), any(), any()))
        .thenReturn(Optional.empty());
    mockMvc
        .perform(
            get("/api/v1/payments/users/{user_id}/summary", USER_ID)
                .param(FROM, FROM_DATE)
                .param(TO, TO_DATE)
                .with(
                    jwt()
                        .jwt(j -> j.subject(USER_ID))
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(0));
  }

  @Test
  void getUserSum_returns_403_when_requesting_other_users_sum() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/payments/users/{user_id}/summary", OTHER_USER_ID)
                .param(FROM, FROM_DATE)
                .param(TO, TO_DATE)
                .with(
                    jwt()
                        .jwt(j -> j.subject(USER_ID))
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void getAppSum_returns_403_for_non_admin() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/payments/summary")
                .param(FROM, FROM_DATE)
                .param(TO, TO_DATE)
                .with(
                    jwt()
                        .jwt(j -> j.subject(USER_ID))
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void getAppSum_returns_total_for_admin() throws Exception {
    when(paymentService.sumForAllUsers(any(), any()))
        .thenReturn(Optional.of(new BigDecimal("150.00")));

    mockMvc
        .perform(
            get("/api/v1/payments/summary")
                .param(FROM, FROM_DATE)
                .param(TO, TO_DATE)
                .with(
                    jwt()
                        .jwt(j -> j.subject(USER_ID))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(150.00));
  }
}
