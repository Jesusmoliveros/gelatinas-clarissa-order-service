package com.gelatinasclarissa.order_service.repository;

import com.gelatinasclarissa.order_service.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import com.gelatinasclarissa.order_service.domain.enums.PaymentStatus;
import java.time.LocalDateTime;
import com.gelatinasclarissa.order_service.domain.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);

    List<Order> findAllByOrderByCreatedAtDesc();

    boolean existsByOrderNumber(String orderNumber);

    List<Order> findByPaymentStatusAndPaidAtGreaterThanEqualAndPaidAtLessThan(
        PaymentStatus paymentStatus,
        LocalDateTime start,
        LocalDateTime end
);

    List<Order> findByRequestedDeliveryAtGreaterThanEqualAndRequestedDeliveryAtLessThanAndStatusNotOrderByRequestedDeliveryAtAsc(
            LocalDateTime start,
            LocalDateTime end,
            OrderStatus excludedStatus
    );

}