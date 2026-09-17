package com.gelatinasclarissa.order_service.service;

import com.gelatinasclarissa.order_service.domain.Order;
import com.gelatinasclarissa.order_service.domain.enums.ContactSource;
import com.gelatinasclarissa.order_service.domain.enums.DeliveryType;
import com.gelatinasclarissa.order_service.domain.enums.PaymentMethod;
import com.gelatinasclarissa.order_service.domain.enums.PaymentStatus;
import com.gelatinasclarissa.order_service.dto.OrderItemRequest;
import com.gelatinasclarissa.order_service.dto.OrderRequest;
import com.gelatinasclarissa.order_service.mapper.OrderMapper;
import com.gelatinasclarissa.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import com.gelatinasclarissa.order_service.domain.enums.OrderStatus;
import com.gelatinasclarissa.order_service.dto.UpdateOrderStatusRequest;
import com.gelatinasclarissa.order_service.dto.UpdatePaymentRequest;

import java.util.Optional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);

        OrderMapper orderMapper = new OrderMapper();

        orderService = new OrderService(
                orderRepository,
                orderMapper
        );

        ReflectionTestUtils.setField(
                orderService,
                "defaultTransferPrice",
                new BigDecimal("80.00")
        );
    }

    @Test
    void shouldCalculateOrderTotals() {
        OrderRequest request = createOrderRequest();

        when(orderRepository.existsByOrderNumber(anyString()))
                .thenReturn(false);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.create(request);

        ArgumentCaptor<Order> captor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(captor.capture());

        Order savedOrder = captor.getValue();

        assertEquals(
                new BigDecimal("290.00"),
                savedOrder.getSubtotal()
        );

        assertEquals(
                new BigDecimal("80.00"),
                savedOrder.getExtrasTotal()
        );

        assertEquals(
                new BigDecimal("370.00"),
                savedOrder.getTotal()
        );

        assertEquals(2, savedOrder.getItems().size());

        assertTrue(savedOrder.getOrderNumber().startsWith("GC-"));
    }

    private OrderRequest createOrderRequest() {
        OrderItemRequest regularItem = new OrderItemRequest(
                2L,
                5L,
                "Gelatina de fresa",
                "Fresa",
                "Individual",
                2,
                new BigDecimal("35.00"),
                null,
                false,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        OrderItemRequest customItem = new OrderItemRequest(
                null,
                null,
                "Gelatina personalizada",
                "Limón con durazno",
                "Grande",
                1,
                new BigDecimal("220.00"),
                "Decoración de Pokémon",
                true,
                "La administradora elegirá la imagen",
                null,
                null,
                BigDecimal.ZERO
        );

        return new OrderRequest(
                "María López",
                "2281234567",
                ContactSource.WHATSAPP,
                LocalDateTime.now().plusDays(2),
                DeliveryType.MEETING_POINT,
                "Entrada de la colonia",
                PaymentMethod.CASH,
                PaymentStatus.PENDING,
                "Avisar cuando esté listo",
                List.of(regularItem, customItem)
        );
    }
    @Test
    void shouldRejectInvalidStatusTransition() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        UpdateOrderStatusRequest request =
                new UpdateOrderStatusRequest(
                        OrderStatus.IN_PREPARATION
                );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateStatus(1L, request)
        );

        assertEquals(
                "No se puede cambiar el pedido de DELIVERED a IN_PREPARATION",
                exception.getMessage()
        );

        verify(orderRepository, never()).save(any(Order.class));
    }
    @Test
    void shouldRejectPaidOrderWithoutPaymentMethod() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setPaymentStatus(PaymentStatus.PENDING);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        UpdatePaymentRequest request =
                new UpdatePaymentRequest(
                        PaymentStatus.PAID,
                        null
                );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updatePayment(1L, request)
        );

        assertEquals(
                "Debe indicar el método de pago",
                exception.getMessage()
        );

        assertNull(order.getPaidAt());

        verify(orderRepository, never()).save(any(Order.class));
    }
}