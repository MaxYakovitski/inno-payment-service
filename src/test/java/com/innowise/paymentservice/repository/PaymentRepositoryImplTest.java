package com.innowise.paymentservice.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.innowise.paymentservice.TestcontainersConfiguration;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PaymentRepositoryImplTest {

  @Autowired private PaymentRepository paymentRepository;
  @Autowired private MongoTemplate mongoTemplate;

  @AfterEach
  void cleanUp() {
    mongoTemplate.getDb().getCollection("payments").deleteMany(new Document());
  }

  @Test
  void should_save_and_find_payment_by_id() {
    Payment saved = paymentRepository.save(newPayment(PaymentStatus.PENDING, "10.00"));

    Optional<Payment> found = paymentRepository.findById(saved.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getPaymentAmount()).isEqualByComparingTo("10.00");
  }

  @Test
  void should_reject_second_active_payment_for_same_order() {
    UUID orderId = UUID.randomUUID();
    paymentRepository.save(newPayment(orderId, UUID.randomUUID(), PaymentStatus.PENDING, "10.00"));
    Payment secondPayment = newPayment(orderId, UUID.randomUUID(), PaymentStatus.SUCCESS, "5.00");

    assertThrows(DuplicateKeyException.class, () -> paymentRepository.save(secondPayment));
  }

  @Test
  void should_allow_retry_after_failed_payment() {
    UUID orderId = UUID.randomUUID();
    paymentRepository.save(newPayment(orderId, UUID.randomUUID(), PaymentStatus.FAILED, "10.00"));

    Payment retry =
        paymentRepository.save(
            newPayment(orderId, UUID.randomUUID(), PaymentStatus.PENDING, "10.00"));

    assertThat(retry.getId()).isNotNull();
  }

  @Test
  void should_search_by_user_id_and_status() {
    UUID userId = UUID.randomUUID();
    paymentRepository.save(newPayment(UUID.randomUUID(), userId, PaymentStatus.SUCCESS, "10.00"));
    paymentRepository.save(newPayment(UUID.randomUUID(), userId, PaymentStatus.FAILED, "1.00"));
    paymentRepository.save(
        newPayment(UUID.randomUUID(), UUID.randomUUID(), PaymentStatus.SUCCESS, "1.00"));

    List<Payment> results = paymentRepository.search(null, userId, PaymentStatus.SUCCESS);

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().getUserId()).isEqualTo(userId);
  }

  @Test
  void should_claim_only_pending_payments_atomically() {
    Payment pending = paymentRepository.save(newPayment(PaymentStatus.PENDING, "10.00"));
    paymentRepository.save(newPayment(PaymentStatus.SUCCESS, "5.00"));

    Optional<Payment> claimed = paymentRepository.claimNextPending();

    assertThat(claimed).isPresent();
    assertThat(claimed.get().getId()).isEqualTo(pending.getId());
    assertThat(claimed.get().getStatus()).isEqualTo(PaymentStatus.PROCESSING);
  }

  @Test
  void should_return_empty_when_no_pending_payments() {
    paymentRepository.save(newPayment(PaymentStatus.SUCCESS, "10.00"));

    Optional<Payment> claimed = paymentRepository.claimNextPending();

    assertThat(claimed).isEmpty();
  }

  @Test
  void should_sum_successful_payments_for_user_within_range() {
    UUID userId = UUID.randomUUID();
    Instant now = Instant.now();

    saveWithTimestamp(userId, PaymentStatus.SUCCESS, "10.50", now.minus(1, ChronoUnit.DAYS));
    saveWithTimestamp(userId, PaymentStatus.SUCCESS, "5.25", now.minus(2, ChronoUnit.DAYS));
    saveWithTimestamp(userId, PaymentStatus.FAILED, "100.00", now.minus(1, ChronoUnit.DAYS));
    saveWithTimestamp(
        UUID.randomUUID(), PaymentStatus.SUCCESS, "999.00", now.minus(1, ChronoUnit.DAYS));

    Optional<BigDecimal> sum =
        paymentRepository.sumForUser(userId, now.minus(3, ChronoUnit.DAYS), now);

    assertThat(sum).isPresent();
    assertThat(sum.get()).isEqualByComparingTo("15.75");
  }

  @Test
  void should_return_empty_when_no_payments_match_sum_criteria() {
    Optional<BigDecimal> sum =
        paymentRepository.sumForUser(
            UUID.randomUUID(), Instant.now().minus(1, ChronoUnit.DAYS), Instant.now());

    assertThat(sum).isEmpty();
  }

  @Test
  void should_sum_successful_payments_across_all_users() {
    Instant now = Instant.now();
    saveWithTimestamp(
        UUID.randomUUID(), PaymentStatus.SUCCESS, "20.00", now.minus(1, ChronoUnit.DAYS));
    saveWithTimestamp(
        UUID.randomUUID(), PaymentStatus.SUCCESS, "30.00", now.minus(1, ChronoUnit.DAYS));
    saveWithTimestamp(
        UUID.randomUUID(), PaymentStatus.FAILED, "999.00", now.minus(1, ChronoUnit.DAYS));

    Optional<BigDecimal> sum = paymentRepository.sumForAllUsers(now.minus(3, ChronoUnit.DAYS), now);

    assertThat(sum).isPresent();
    assertThat(sum.get()).isEqualByComparingTo("50.00");
  }

  private void saveWithTimestamp(
      UUID userId, PaymentStatus status, String amount, Instant timestamp) {
    Payment payment = newPayment(UUID.randomUUID(), userId, status, amount);
    payment.setTimestamp(timestamp);
    paymentRepository.save(payment);
  }

  private Payment newPayment(PaymentStatus status, String amount) {
    return newPayment(UUID.randomUUID(), UUID.randomUUID(), status, amount);
  }

  private Payment newPayment(UUID orderId, UUID userId, PaymentStatus status, String amount) {
    return Payment.builder()
        .orderId(orderId)
        .userId(userId)
        .status(status)
        .timestamp(Instant.now())
        .paymentAmount(new BigDecimal(amount))
        .build();
  }
}
