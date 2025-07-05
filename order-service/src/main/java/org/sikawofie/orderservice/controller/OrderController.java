package org.sikawofie.orderservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sikawofie.orderservice.Utils.SecurityUtils;
import org.sikawofie.orderservice.dto.ApiResponse;
import org.sikawofie.orderservice.dto.OrderRequestDto;
import org.sikawofie.orderservice.dto.OrderResponseDto;
import org.sikawofie.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDto>> placeOrder(
            @RequestBody @Valid OrderRequestDto request,
            ServerHttpRequest httpRequest) {

        Long customerId = SecurityUtils.getUserId(httpRequest);
        String role = SecurityUtils.getUserRole(httpRequest);

        OrderResponseDto order = orderService.placeOrder(request, customerId, role);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<OrderResponseDto>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Order placed successfully")
                        .data(order)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> customerOrders(ServerHttpRequest request) {
        Long customerId = SecurityUtils.getUserId(request);
        List<OrderResponseDto> orders = orderService.getOrdersByCustomer(customerId);

        return ResponseEntity.ok(ApiResponse.<List<OrderResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Customer orders retrieved")
                .data(orders)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/restaurant/{id}")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> ordersForRestaurant(
            @PathVariable Long id,
            ServerHttpRequest request) {

        Long userId = SecurityUtils.getUserId(request);
        String role = SecurityUtils.getUserRole(request);

        List<OrderResponseDto> orders = orderService.getOrdersByRestaurant(id, userId, role);

        return ResponseEntity.ok(ApiResponse.<List<OrderResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Orders for restaurant retrieved")
                .data(orders)
                .timestamp(LocalDateTime.now())
                .build());
    }

}

