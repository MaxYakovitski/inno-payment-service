package com.innowise.paymentservice.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Document(collation = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

  @Id private String id;

  @Field("order_id")
  private UUID orderId;

  @Field("user_id")
  private UUID userId;

  @Field("status")
  private PaymentStatus status;

  @Field("timestamp")
  private Instant timestamp;

  @Field(name = "payment_amount", targetType = FieldType.DECIMAL128)
  private BigDecimal paymentAmount;
}
