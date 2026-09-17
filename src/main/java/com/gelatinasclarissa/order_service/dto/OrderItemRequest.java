package com.gelatinasclarissa.order_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OrderItemRequest(

        Long productId,

        Long variantId,

        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(max = 120)
        String productName,

        @Size(max = 150)
        String flavor,

        @Size(max = 80)
        String size,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor que cero")
        Integer quantity,

        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal unitPrice,

        @Size(max = 1000)
        String customizationNotes,

        boolean transferRequested,

        @Size(max = 500)
        String transferInstructions,

        @Size(max = 500)
        String transferImageUrl,

        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal transferPrice,

        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal additionalPrice
) {
}