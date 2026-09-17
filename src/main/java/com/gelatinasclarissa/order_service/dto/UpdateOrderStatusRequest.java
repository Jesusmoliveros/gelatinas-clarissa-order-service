package com.gelatinasclarissa.order_service.dto;

import com.gelatinasclarissa.order_service.domain.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(

        @NotNull(message = "El estado es obligatorio")
        OrderStatus status
) {
}