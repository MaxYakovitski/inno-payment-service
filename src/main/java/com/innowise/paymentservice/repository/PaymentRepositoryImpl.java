package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepositoryCustom {

  private final MongoOperations mongoOperations;
  private static final String STATUS = "status";

  @Override
  public List<Payment> search(UUID orderId, UUID userId, PaymentStatus status) {
    List<Criteria> conditions = new ArrayList<>();
    if (orderId != null) {
      conditions.add(Criteria.where("orderId").is(orderId));
    }
    if (userId != null) {
      conditions.add(Criteria.where("userId").is(userId));
    }
    if (status != null) {
      conditions.add(Criteria.where(STATUS).is(status));
    }

    Criteria criteria = new Criteria();
    if (!conditions.isEmpty()) {
      criteria.andOperator(conditions.toArray(new Criteria[0]));
    }
    return mongoOperations.find(new Query(criteria), Payment.class);
  }

  @Override
  public Optional<BigDecimal> sumForUser(UUID userId, Instant from, Instant to) {
    return sum(
        Criteria.where("userId")
            .is(userId)
            .and(STATUS)
            .is(PaymentStatus.SUCCESS)
            .and("timestamp")
            .gte(from)
            .lte(to));
  }

  @Override
  public Optional<BigDecimal> sumForAllUsers(Instant from, Instant to) {
    return sum(Criteria.where(STATUS).is(PaymentStatus.SUCCESS).and("timestamp").gte(from).lte(to));
  }

  @Override
  public Optional<Payment> claimNextPending() {
    Query query = new Query(Criteria.where(STATUS).is(PaymentStatus.PENDING));
    Update update = new Update().set(STATUS, PaymentStatus.PROCESSING);
    Payment claimed =
        mongoOperations.findAndModify(
            query, update, FindAndModifyOptions.options().returnNew(true), Payment.class);
    return Optional.ofNullable(claimed);
  }

  private Optional<BigDecimal> sum(Criteria matchCriteria) {
    Aggregation aggregation =
        Aggregation.newAggregation(
            Payment.class,
            Aggregation.match(matchCriteria),
            Aggregation.group().sum("paymentAmount").as("total"));

    AggregationResults<SumResult> results =
        mongoOperations.aggregate(aggregation, Payment.class, SumResult.class);
    return Optional.ofNullable(results.getUniqueMappedResult()).map(SumResult::total);
  }
}
