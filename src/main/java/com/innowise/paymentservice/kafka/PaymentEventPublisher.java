package com.innowise.paymentservice.kafka;

import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.event.PaymentCompletedEvent;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

  private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;
  private final String topic;

  public PaymentEventPublisher(
      KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate,
      @Value("${payment.kafka.topic}") String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.topic = topic;
  }

  public void publishPaymentCompleted(UUID orderId, PaymentStatus status) {
    kafkaTemplate.send(topic, orderId.toString(), new PaymentCompletedEvent(orderId, status));
  }
}
