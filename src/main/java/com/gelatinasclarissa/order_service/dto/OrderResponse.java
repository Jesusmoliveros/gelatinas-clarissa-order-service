package com.gelatinasclarissa.order_service.dto;

import com.gelatinasclarissa.order_service.domain.enums.ContactSource;
import com.gelatinasclarissa.order_service.domain.enums.DeliveryType;
import com.gelatinasclarissa.order_service.domain.enums.OrderStatus;
import com.gelatinasclarissa.order_service.domain.enums.PaymentMethod;
import com.gelatinasclarissa.order_service.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        String customerName,
        String customerPhone,
        ContactSource contactSource,
        OrderStatus status,
        LocalDateTime requestedDeliveryAt,
        DeliveryType deliveryType,
        String deliveryDetails,
        BigDecimal subtotal,
        BigDecimal extrasTotal,
        BigDecimal total,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        LocalDateTime paidAt,
        String generalNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResponse> items
) {
}