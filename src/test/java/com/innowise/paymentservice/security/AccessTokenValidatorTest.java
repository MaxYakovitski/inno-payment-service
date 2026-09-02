package com.innowise.paymentservice.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class AccessTokenValidatorTest {

  private final AccessTokenValidator validator = new AccessTokenValidator();

  @Mock private Jwt jwt;

  @Test
  void validate_succeeds_for_AccessToken() {
    when(jwt.getClaimAsString("type")).thenReturn("access");
    var result = validator.validate(jwt);
    assertThat(result.hasErrors()).isFalse();
  }

  @Test
  void validate_fails_for_RefreshToken() {
    when(jwt.getClaimAsString("type")).thenReturn("refresh");
    var result = validator.validate(jwt);
    assertThat(result.hasErrors()).isTrue();
  }
}
