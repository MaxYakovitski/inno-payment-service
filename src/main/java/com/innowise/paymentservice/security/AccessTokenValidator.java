package com.innowise.paymentservice.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class AccessTokenValidator implements OAuth2TokenValidator<Jwt> {

  private static final String TOKEN_TYPE_CLAIM = "type";
  private static final String ACCESS_TOKEN_TYPE = "access";

  @Override
  public OAuth2TokenValidatorResult validate(Jwt jwt) {
    String tokenType = jwt.getClaimAsString(TOKEN_TYPE_CLAIM);
    if (!ACCESS_TOKEN_TYPE.equals(tokenType)) {
      OAuth2Error error =
          new OAuth2Error(
              "invalid_token_type", "Expected access token but got: " + tokenType, null);
      return OAuth2TokenValidatorResult.failure(error);
    }
    return OAuth2TokenValidatorResult.success();
  }
}
