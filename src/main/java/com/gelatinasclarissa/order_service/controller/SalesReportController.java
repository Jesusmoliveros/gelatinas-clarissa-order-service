package com.gelatinasclarissa.order_service.controller;

import com.gelatinasclarissa.order_service.dto.MonthlySalesReportResponse;
import com.gelatinasclarissa.order_service.service.SalesReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class SalesReportController {

    private final SalesReportService salesReportService;

    @GetMapping("/sales/monthly")
    public MonthlySalesReportResponse getMonthlySales(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return salesReportService.getMonthlyReport(year, month);
    }
}