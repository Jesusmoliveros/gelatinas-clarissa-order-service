package com.gelatinasclarissa.order_service.dto;

import java.math.BigDecimal;

public record MonthlySalesReportResponse(
        int year,
        int month,
        long paidOrders,
        BigDecimal totalSales,
        BigDecimal averageTicket
) {
}