package com.gelatinasclarissa.order_service.mapper;

import com.gelatinasclarissa.order_service.domain.Order;
import com.gelatinasclarissa.order_service.domain.OrderItem;
import com.gelatinasclarissa.order_service.dto.OrderItemRequest;
import com.gelatinasclarissa.order_service.dto.OrderItemResponse;
import com.gelatinasclarissa.order_service.dto.OrderRequest;
import com.gelatinasclarissa.order_service.dto.OrderResponse;
import com.gelatinasclarissa.order_service.dto.UpdateOrderRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public Order toEntity(OrderRequest request) {
        Order order = new Order();

        order.setCustomerName(request.customerName());
        order.setCustomerPhone(request.customerPhone());
        order.setContactSource(request.contactSource());
        order.setRequestedDeliveryAt(request.requestedDeliveryAt());
        order.setDeliveryType(request.deliveryType());
        order.setDeliveryDetails(request.deliveryDetails());
        order.setPaymentMethod(request.paymentMethod());
        order.setGeneralNotes(request.generalNotes());

        for (OrderItemRequest itemRequest : request.items()) {
            OrderItem item = toItemEntity(itemRequest);
            order.addItem(item);
        }

        return order;
    }

    private OrderItem toItemEntity(OrderItemRequest request) {
        OrderItem item = new OrderItem();

        item.setProductId(request.productId());
        item.setVariantId(request.variantId());
        item.setProductName(request.productName());
        item.setFlavor(request.flavor());
        item.setSize(request.size());
        item.setQuantity(request.quantity());
        item.setUnitPrice(request.unitPrice());
        item.setCustomizationNotes(request.customizationNotes());
        item.setTransferRequested(request.transferRequested());
        item.setTransferInstructions(request.transferInstructions());
        item.setTransferImageUrl(request.transferImageUrl());

        if (request.transferPrice() != null) {
            item.setTransferPrice(request.transferPrice());
        }

        if (request.additionalPrice() != null) {
            item.setAdditionalPrice(request.additionalPrice());
        }

        return item;
    }

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getContactSource(),
                order.getStatus(),
                order.getRequestedDeliveryAt(),
                order.getDeliveryType(),
                order.getDeliveryDetails(),
                order.getSubtotal(),
                order.getExtrasTotal(),
                order.getTotal(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getPaidAt(),
                order.getGeneralNotes(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getVariantId(),
                item.getProductName(),
                item.getFlavor(),
                item.getSize(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getCustomizationNotes(),
                item.isTransferRequested(),
                item.getTransferInstructions(),
                item.getTransferImageUrl(),
                item.getTransferPrice(),
                item.getAdditionalPrice(),
                item.getLineTotal()
        );
    }

    public void updateEntity(
            Order order,
            UpdateOrderRequest request
    ) {
        order.setCustomerName(request.customerName());
        order.setCustomerPhone(request.customerPhone());
        order.setContactSource(request.contactSource());
        order.setRequestedDeliveryAt(request.requestedDeliveryAt());
        order.setDeliveryType(request.deliveryType());
        order.setDeliveryDetails(request.deliveryDetails());
        order.setGeneralNotes(request.generalNotes());

        order.getItems().clear();

        for (OrderItemRequest itemRequest : request.items()) {
            OrderItem item = toItemEntity(itemRequest);
            order.addItem(item);
        }
    }
}