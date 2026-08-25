package com.innowise.paymentservice.security;

import java.util.Collection;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class JwtRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  public static final String ROLE_CLAIM = "role";
  private static final String ROLE_PREFIX = "ROLE_";

  @Override
  public AbstractAuthenticationToken convert(Jwt source) {
    String role = source.getClaimAsString(ROLE_CLAIM);
    Collection<GrantedAuthority> authorities =
        role == null ? List.of() : AuthorityUtils.createAuthorityList(ROLE_PREFIX + role);
    return new JwtAuthenticationToken(source, authorities);
  }
}
