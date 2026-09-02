package com.innowise.paymentservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@Configuration
public class MongoConfig {

  public MongoConfig(MappingMongoConverter mongoConverter) {
    mongoConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
  }
}
