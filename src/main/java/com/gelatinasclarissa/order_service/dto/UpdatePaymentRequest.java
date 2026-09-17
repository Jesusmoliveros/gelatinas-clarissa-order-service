package com.gelatinasclarissa.order_service.dto;

import com.gelatinasclarissa.order_service.domain.enums.PaymentMethod;
import com.gelatinasclarissa.order_service.domain.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePaymentRequest(

        @NotNull(message = "El estado del pago es obligatorio")
        PaymentStatus paymentStatus,

        PaymentMethod paymentMethod
) {
}