package com.gelatinasclarissa.order_service.service;

import com.gelatinasclarissa.order_service.domain.Order;
import com.gelatinasclarissa.order_service.domain.enums.PaymentStatus;
import com.gelatinasclarissa.order_service.dto.MonthlySalesReportResponse;
import com.gelatinasclarissa.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesReportService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public MonthlySalesReportResponse getMonthlyReport(
            int year,
            int month
    ) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "El mes debe encontrarse entre 1 y 12"
            );
        }

        YearMonth period = YearMonth.of(year, month);

        LocalDateTime start = period
                .atDay(1)
                .atStartOfDay();

        LocalDateTime end = period
                .plusMonths(1)
                .atDay(1)
                .atStartOfDay();

        List<Order> paidOrders =
                orderRepository
                        .findByPaymentStatusAndPaidAtGreaterThanEqualAndPaidAtLessThan(
                                PaymentStatus.PAID,
                                start,
                                end
                        );

        BigDecimal totalSales = paidOrders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageTicket = paidOrders.isEmpty()
                ? BigDecimal.ZERO
                : totalSales.divide(
                        BigDecimal.valueOf(paidOrders.size()),
                        2,
                        RoundingMode.HALF_UP
                );

        return new MonthlySalesReportResponse(
                year,
                month,
                paidOrders.size(),
                totalSales,
                averageTicket
        );
    }
}