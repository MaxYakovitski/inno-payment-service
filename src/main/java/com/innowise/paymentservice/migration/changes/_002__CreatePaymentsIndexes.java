package com.innowise.paymentservice.migration.changes;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.flamingock.api.annotations.Apply;
import io.flamingock.api.annotations.Change;
import io.flamingock.api.annotations.Rollback;
import io.flamingock.api.annotations.TargetSystem;
import java.util.List;
import org.bson.Document;

@Change(id = "002-create-payments-indexes", author = "Max Yakovitski", transactional = false)
@TargetSystem(id = "mongodb-ts")
public class _002__CreatePaymentsIndexes {

  private static final String COLLECTION = "payments";
  private static final String STATUS = "status";

  @Apply
  public void apply(MongoDatabase database) {
    var payments = database.getCollection(COLLECTION);

    payments.createIndex(
        Indexes.compoundIndex(
            Indexes.ascending("user_id"),
            Indexes.ascending(STATUS),
            Indexes.descending("timestamp")),
        new IndexOptions().name("idx_user_status_timestamp"));

    payments.createIndex(
        Indexes.compoundIndex(Indexes.ascending(STATUS), Indexes.descending("timestamp")),
        new IndexOptions().name("idx_status_timestamp"));

    payments.createIndex(
        Indexes.ascending("order_id"),
        new IndexOptions()
            .name("uq_order_id_active")
            .unique(true)
            .partialFilterExpression(
                new Document(STATUS, new Document("$in", List.of("PENDING", "SUCCESS")))));
  }

  @Rollback
  public void rollback(MongoDatabase database) {
    var payments = database.getCollection(COLLECTION);
    payments.dropIndex("idx_user_status_timestamp");
    payments.dropIndex("idx_status_timestamp");
    payments.dropIndex("uq_order_id_active");
  }
}
