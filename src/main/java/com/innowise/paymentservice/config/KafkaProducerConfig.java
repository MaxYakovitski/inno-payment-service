package com.innowise.paymentservice.config;

import com.innowise.paymentservice.event.PaymentCompletedEvent;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

  @Bean
  public ProducerFactory<String, PaymentCompletedEvent> paymentEventProducerFactory(
      KafkaProperties kafkaProperties) {
    return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
  }

  @Bean
  public KafkaTemplate<String, PaymentCompletedEvent> paymentEventKafkaTemplate(
      ProducerFactory<String, PaymentCompletedEvent> paymentEventProducerFactory) {
    return new KafkaTemplate<>(paymentEventProducerFactory);
  }
}
