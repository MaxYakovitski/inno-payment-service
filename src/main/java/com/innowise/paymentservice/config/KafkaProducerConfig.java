package com.innowise.paymentservice.config;

import com.innowise.paymentservice.event.PaymentCompletedEvent;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

@Configuration
public class KafkaProducerConfig {

  @Bean
  public ProducerFactory<String, PaymentCompletedEvent> paymentEventProducerFactory(
      @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

    Map<String, Object> props =
        Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            JacksonJsonSerializer.class,
            JacksonJsonSerializer.ADD_TYPE_INFO_HEADERS,
            false);
    return new DefaultKafkaProducerFactory<>(props);
  }

  @Bean
  public KafkaTemplate<String, PaymentCompletedEvent> paymentEventKafkaTemplate(
      ProducerFactory<String, PaymentCompletedEvent> paymentEventProducerFactory) {
    return new KafkaTemplate<>(paymentEventProducerFactory);
  }
}
