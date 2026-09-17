package com.gelatinasclarissa.order_service.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        Long variantId,
        String productName,
        String flavor,
        String size,
        Integer quantity,
        BigDecimal unitPrice,
        String customizationNotes,
        boolean transferRequested,
        String transferInstructions,
        String transferImageUrl,
        BigDecimal transferPrice,
        BigDecimal additionalPrice,
        BigDecimal lineTotal
) {
}