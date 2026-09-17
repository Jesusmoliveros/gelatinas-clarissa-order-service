package com.gelatinasclarissa.order_service.dto;

import com.gelatinasclarissa.order_service.domain.enums.ContactSource;
import com.gelatinasclarissa.order_service.domain.enums.DeliveryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateOrderRequest(

        @NotBlank(message = "El nombre del cliente es obligatorio")
        @Size(max = 120)
        String customerName,

        @NotBlank(message = "El teléfono del cliente es obligatorio")
        @Size(max = 20)
        String customerPhone,

        @NotNull(message = "El origen del contacto es obligatorio")
        ContactSource contactSource,

        @NotNull(message = "La fecha de entrega es obligatoria")
        LocalDateTime requestedDeliveryAt,

        @NotNull(message = "El tipo de entrega es obligatorio")
        DeliveryType deliveryType,

        @Size(max = 500)
        String deliveryDetails,

        @Size(max = 1000)
        String generalNotes,

        @NotEmpty(message = "El pedido debe contener al menos un producto")
        List<@Valid OrderItemRequest> items
) {
}