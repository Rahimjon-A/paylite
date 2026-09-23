package com.paylite.mapper;

import com.paylite.domain.Payment;
import com.paylite.domain.dto.CreatePaymentRequest;
import com.paylite.domain.dto.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "commissionAmount", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "agent", ignore = true)
    @Mapping(target = "accountNumber", source = "account")
    Payment toEntity(CreatePaymentRequest request);

    @Mapping(target = "account", source = "accountNumber")
    PaymentResponse toResponse(Payment payment);
}
