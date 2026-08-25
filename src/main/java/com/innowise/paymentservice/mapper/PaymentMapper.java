package com.innowise.paymentservice.mapper;

import com.innowise.paymentservice.dto.PaymentCreateDto;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.entity.Payment;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "userId", source = "userId")
  @Mapping(target = "status", constant = "PENDING")
  @Mapping(target = "timestamp", ignore = true)
  Payment toEntity(PaymentCreateDto dto, UUID userId);

  PaymentResponseDto toDto(Payment entity);

  List<PaymentResponseDto> toDtoList(List<Payment> entities);
}
