package com.innowise.paymentservice.migration.changes;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.ValidationAction;
import com.mongodb.client.model.ValidationLevel;
import com.mongodb.client.model.ValidationOptions;
import io.flamingock.api.annotations.Apply;
import io.flamingock.api.annotations.Change;
import io.flamingock.api.annotations.Rollback;
import io.flamingock.api.annotations.TargetSystem;
import java.util.List;
import org.bson.Document;

@Change(id = "001-create-payments-collection", author = "Max Yakovitski", transactional = false)
@TargetSystem(id = "mongodb-ts")
public class _001__CreatePaymentsCollection {

  private static final String COLLECTION = "payments";
  private static final String BSON_TYPE = "bsonType";

  @Apply
  public void apply(MongoDatabase database) {
    Document schema =
        new Document()
            .append(BSON_TYPE, "object")
            .append(
                "required",
                List.of("_id", "order_id", "user_id", "status", "timestamp", "payment_amount"))
            .append("additionalProperties", false)
            .append(
                "properties",
                new Document()
                    .append("_id", new Document(BSON_TYPE, "objectId"))
                    .append("order_id", new Document(BSON_TYPE, "binData"))
                    .append("user_id", new Document(BSON_TYPE, "binData"))
                    .append(
                        "status",
                        new Document(BSON_TYPE, "string")
                            .append("enum", List.of("PENDING", "SUCCESS", "FAILED")))
                    .append("timestamp", new Document(BSON_TYPE, "date"))
                    .append("payment_amount", new Document(BSON_TYPE, "decimal")));

    database.createCollection(
        COLLECTION,
        new CreateCollectionOptions()
            .validationOptions(
                new ValidationOptions()
                    .validator(new Document("$jsonSchema", schema))
                    .validationLevel(ValidationLevel.STRICT)
                    .validationAction(ValidationAction.ERROR)));
  }

  @Rollback
  public void rollback(MongoDatabase database) {
    database.getCollection(COLLECTION).drop();
  }
}
