package org.sikawofie.orderservice.service;


import org.sikawofie.orderservice.dto.OrderRequestDto;
import org.sikawofie.orderservice.dto.OrderResponseDto;

import java.util.List;

public interface OrderService {

    OrderResponseDto placeOrder(OrderRequestDto request, Long customerId, String role);

    List<OrderResponseDto> getOrdersByCustomer(Long customerId);

    List<OrderResponseDto> getOrdersByRestaurant(Long restaurantId, Long userId, String role);
}
