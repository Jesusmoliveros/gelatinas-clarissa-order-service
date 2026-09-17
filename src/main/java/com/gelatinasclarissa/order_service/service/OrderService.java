package com.gelatinasclarissa.order_service.service;

import com.gelatinasclarissa.order_service.domain.Order;
import com.gelatinasclarissa.order_service.domain.OrderItem;
import com.gelatinasclarissa.order_service.domain.enums.OrderStatus;
import com.gelatinasclarissa.order_service.domain.enums.PaymentStatus;
import com.gelatinasclarissa.order_service.dto.OrderRequest;
import com.gelatinasclarissa.order_service.dto.OrderResponse;
import com.gelatinasclarissa.order_service.mapper.OrderMapper;
import com.gelatinasclarissa.order_service.repository.OrderRepository;
import com.gelatinasclarissa.order_service.dto.UpdatePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gelatinasclarissa.order_service.exception.OrderNotFoundException;
import com.gelatinasclarissa.order_service.dto.UpdateOrderStatusRequest;
import com.gelatinasclarissa.order_service.dto.UpdateOrderRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.List;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Value("${gelatinas.transfer.default-price}")
    private BigDecimal defaultTransferPrice;

    @Transactional
    public OrderResponse create(OrderRequest request) {
        Order order = orderMapper.toEntity(request);

        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.CONFIRMED);

        configurePayment(order, request);
        calculateTotals(order);

        Order savedOrder = orderRepository.save(order);

        return orderMapper.toResponse(savedOrder);
    }

    private void configurePayment(Order order, OrderRequest request) {
        PaymentStatus paymentStatus = request.paymentStatus() == null
                ? PaymentStatus.PENDING
                : request.paymentStatus();

        order.setPaymentStatus(paymentStatus);

        if (paymentStatus == PaymentStatus.PAID) {
            if (request.paymentMethod() == null) {
                throw new IllegalArgumentException(
                        "Un pedido pagado debe indicar el método de pago"
                );
            }

            order.setPaidAt(LocalDateTime.now());
        } else {
            order.setPaidAt(null);
        }
    }

    private void calculateTotals(Order order) {
        BigDecimal subtotal = ZERO;
        BigDecimal extrasTotal = ZERO;

        for (OrderItem item : order.getItems()) {
            BigDecimal baseAmount = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            BigDecimal transferPrice = calculateTransferPrice(item);
            BigDecimal additionalPrice = valueOrZero(item.getAdditionalPrice());

            BigDecimal lineTotal = baseAmount
                    .add(transferPrice)
                    .add(additionalPrice);

            item.setTransferPrice(transferPrice);
            item.setAdditionalPrice(additionalPrice);
            item.setLineTotal(lineTotal);

            subtotal = subtotal.add(baseAmount);
            extrasTotal = extrasTotal
                    .add(transferPrice)
                    .add(additionalPrice);
        }

        order.setSubtotal(subtotal);
        order.setExtrasTotal(extrasTotal);
        order.setTotal(subtotal.add(extrasTotal));
    }

    private BigDecimal calculateTransferPrice(OrderItem item) {
        if (!item.isTransferRequested()) {
            return ZERO;
        }

        if (item.getTransferPrice() == null
                || item.getTransferPrice().compareTo(ZERO) == 0) {
            return defaultTransferPrice;
        }

        return item.getTransferPrice();
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private String generateOrderNumber() {
        String orderNumber;

        do {
            String date = LocalDate.now()
                    .format(DateTimeFormatter.BASIC_ISO_DATE);

            String randomPart = UUID.randomUUID()
                    .toString()
                    .substring(0, 6)
                    .toUpperCase(Locale.ROOT);

            orderNumber = "GC-" + date + "-" + randomPart;
        } while (orderRepository.existsByOrderNumber(orderNumber));

        return orderNumber;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "No se encontró el pedido con id: " + id
                        )
                );

        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse findByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "No se encontró el pedido: " + orderNumber
                        )
                );

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(
            Long id,
            UpdateOrderStatusRequest request
    ) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "No se encontró el pedido con id: " + id
                        )
                );

        if (request.status() == OrderStatus.CANCELLED
                && order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException(
                    "No se puede cancelar un pedido pagado sin registrar un reembolso"
            );
        }

        validateStatusTransition(order.getStatus(), request.status());

        order.setStatus(request.status());

        Order updatedOrder = orderRepository.save(order);

        return orderMapper.toResponse(updatedOrder);
    }

    private void validateStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {
        if (currentStatus == newStatus) {
            return;
        }

        boolean validTransition = switch (currentStatus) {
            case CONFIRMED ->
                    newStatus == OrderStatus.IN_PREPARATION
                            || newStatus == OrderStatus.READY
                            || newStatus == OrderStatus.CANCELLED;

            case IN_PREPARATION ->
                    newStatus == OrderStatus.READY
                            || newStatus == OrderStatus.CANCELLED;

            case READY ->
                    newStatus == OrderStatus.OUT_FOR_DELIVERY
                            || newStatus == OrderStatus.DELIVERED
                            || newStatus == OrderStatus.CANCELLED;

            case OUT_FOR_DELIVERY ->
                    newStatus == OrderStatus.DELIVERED
                            || newStatus == OrderStatus.CANCELLED;

            case DELIVERED, CANCELLED -> false;
        };

        if (!validTransition) {
            throw new IllegalArgumentException(
                    "No se puede cambiar el pedido de "
                            + currentStatus
                            + " a "
                            + newStatus
            );
        }
    }
    @Transactional
    public OrderResponse updatePayment(
            Long id,
            UpdatePaymentRequest request
    ) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "No se encontró el pedido con id: " + id
                        )
                );

        if (order.getStatus() == OrderStatus.CANCELLED
                && request.paymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException(
                    "No se puede registrar como pagado un pedido cancelado"
            );
        }

        if (request.paymentStatus() == PaymentStatus.PAID) {
            if (request.paymentMethod() == null) {
                throw new IllegalArgumentException(
                        "Debe indicar el método de pago"
                );
            }

            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPaymentMethod(request.paymentMethod());

            if (order.getPaidAt() == null) {
                order.setPaidAt(LocalDateTime.now());
            }
        } else {
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setPaymentMethod(request.paymentMethod());
            order.setPaidAt(null);
        }

        Order updatedOrder = orderRepository.save(order);

        return orderMapper.toResponse(updatedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findScheduleByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return orderRepository
                .findByRequestedDeliveryAtGreaterThanEqualAndRequestedDeliveryAtLessThanAndStatusNotOrderByRequestedDeliveryAtAsc(
                        start,
                        end,
                        OrderStatus.CANCELLED
                )
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse update(
            Long id,
            UpdateOrderRequest request
    ) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "No se encontró el pedido con id: " + id
                        )
                );

        validateOrderCanBeEdited(order);

        orderMapper.updateEntity(order, request);

        calculateTotals(order);

        Order updatedOrder = orderRepository.save(order);

        return orderMapper.toResponse(updatedOrder);
    }

    private void validateOrderCanBeEdited(Order order) {
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException(
                    "No se puede editar un pedido entregado"
            );
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "No se puede editar un pedido cancelado"
            );
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException(
                    "No se puede editar un pedido pagado"
            );
        }
    }


}