package com.innowise.paymentservice.migration;

import com.mongodb.client.MongoClient;
import io.flamingock.api.annotations.EnableFlamingock;
import io.flamingock.api.annotations.Stage;
import io.flamingock.store.mongodb.sync.MongoDBSyncAuditStore;
import io.flamingock.targetsystem.mongodb.sync.MongoDBSyncTargetSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFlamingock(stages = {@Stage(location = "com.innowise.paymentservice.migration.changes")})
public class FlamingockConfig {

  @Bean
  public MongoDBSyncTargetSystem mongoDBSyncTargetSystem(
      MongoClient mongoClient, @Value("${spring.mongodb.database}") String database) {
    return new MongoDBSyncTargetSystem("mongodb-ts", mongoClient, database);
  }

  @Bean
  public MongoDBSyncAuditStore auditStore(MongoDBSyncTargetSystem mongoDBSyncTargetSystem) {
    return MongoDBSyncAuditStore.from(mongoDBSyncTargetSystem);
  }
}
