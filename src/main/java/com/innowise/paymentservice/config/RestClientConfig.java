package com.innowise.paymentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  public RestClient randomOrgRestClient(
      @Value("${random-org.base-url}") String baseUrl,
      @Value("${random-org.timeout-ms}") long timeoutMs) {
    var requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout((int) timeoutMs);
    requestFactory.setReadTimeout((int) timeoutMs);

    return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
  }
}
