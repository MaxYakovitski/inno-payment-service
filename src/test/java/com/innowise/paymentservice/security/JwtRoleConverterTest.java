package com.innowise.paymentservice.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class JwtRoleConverterTest {

  private final JwtRoleConverter converter = new JwtRoleConverter();

  @Mock private Jwt jwt;

  @Test
  void convert_maps_role_claim_to_prefixed_authority() {
    when(jwt.getClaimAsString("role")).thenReturn("ADMIN");
    var result = converter.convert(jwt);
    assertThat(result).isInstanceOf(JwtAuthenticationToken.class);
    assertThat(result.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_ADMIN");
  }

  @Test
  void convert_returns_empty_authorities_when_role_claim_missing() {
    when(jwt.getClaimAsString("role")).thenReturn(null);
    var result = converter.convert(jwt);
    assertThat(result.getAuthorities()).isEmpty();
  }
}
