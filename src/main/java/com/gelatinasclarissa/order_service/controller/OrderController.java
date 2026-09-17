package com.gelatinasclarissa.order_service.controller;

import com.gelatinasclarissa.order_service.dto.OrderRequest;
import com.gelatinasclarissa.order_service.dto.OrderResponse;
import com.gelatinasclarissa.order_service.service.OrderService;
import com.gelatinasclarissa.order_service.dto.UpdateOrderStatusRequest;
import com.gelatinasclarissa.order_service.dto.UpdateOrderRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.gelatinasclarissa.order_service.dto.UpdatePaymentRequest;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;


import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        return orderService.create(request);
    }

    @GetMapping
    public List<OrderResponse> findAll() {
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @GetMapping("/number/{orderNumber}")
    public OrderResponse findByOrderNumber(
            @PathVariable String orderNumber
    ) {
        return orderService.findByOrderNumber(orderNumber);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return orderService.updateStatus(id, request);
    }

    @PatchMapping("/{id}/payment")
    public OrderResponse updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentRequest request
    ) {
        return orderService.updatePayment(id, request);
    }

    @GetMapping("/schedule")
    public List<OrderResponse> findScheduleByDate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return orderService.findScheduleByDate(date);
    }

    @PutMapping("/{id}")
    public OrderResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequest request
    ) {
        return orderService.update(id, request);
    }




}