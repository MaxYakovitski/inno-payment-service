package com.innowise.paymentservice.service.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.innowise.paymentservice.entity.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@WireMockTest
class PaymentGatewayClientImplTest {

  private static final String INTEGERS_PATH =
      "/integers?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new";

  private PaymentGatewayClientImpl gatewayClient;

  @BeforeEach
  void setUp(WireMockRuntimeInfo wireMockRuntimeInfo) {
    RestClient restClient =
        RestClient.builder().baseUrl(wireMockRuntimeInfo.getHttpBaseUrl()).build();
    gatewayClient = new PaymentGatewayClientImpl(restClient);
    setField(gatewayClient, "integerPath", INTEGERS_PATH);
  }

  @Test
  void determinateStatus_returns_success_when_number_is_even() {
    stubFor(
        get(urlPathEqualTo("/integers")).willReturn(aResponse().withStatus(200).withBody("58\n")));
    PaymentStatus result = gatewayClient.determinateStatus();
    assertThat(result).isEqualTo(PaymentStatus.SUCCESS);
  }

  @Test
  void determinateStatus_returns_failed_when_number_is_odd() {
    stubFor(
        get(urlPathEqualTo("/integers")).willReturn(aResponse().withStatus(200).withBody("57\n")));
    PaymentStatus result = gatewayClient.determinateStatus();
    assertThat(result).isEqualTo(PaymentStatus.FAILED);
  }

  @Test
  void determinateStatus_throws_when_gateway_returns_server_error() {
    stubFor(get(urlPathEqualTo("/integers")).willReturn(aResponse().withStatus(500)));
    assertThrows(Exception.class, () -> gatewayClient.determinateStatus());
  }
}
