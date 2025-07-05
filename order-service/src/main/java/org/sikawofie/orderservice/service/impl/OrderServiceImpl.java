package org.sikawofie.orderservice.service.impl;


import lombok.RequiredArgsConstructor;
import org.sikawofie.orderservice.dto.OrderItemResponseDto;
import org.sikawofie.orderservice.dto.OrderRequestDto;
import org.sikawofie.orderservice.dto.OrderResponseDto;
import org.sikawofie.orderservice.entity.Order;
import org.sikawofie.orderservice.entity.OrderItem;
import org.sikawofie.orderservice.events.OrderPlacedEvent;
import org.sikawofie.orderservice.producer.OrderEventPublisher;
import org.sikawofie.orderservice.repository.OrderRepository;
import org.sikawofie.orderservice.service.OrderService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final OrderEventPublisher publisher;

    @Override
    public OrderResponseDto placeOrder(OrderRequestDto request, Long customerId, String role) {
        if (!"CUSTOMER".equals(role)) {
            throw new AccessDeniedException("Only customers can place orders.");
        }

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setRestaurantId(request.getRestaurantId());
        order.setStatus("PENDING");

        List<OrderItem> items = request.getItems().stream().map(dto -> {
            OrderItem item = new OrderItem();
            item.setItemName(dto.getItemName());
            item.setPrice(dto.getPrice());
            item.setQuantity(dto.getQuantity());
            item.setOrder(order);
            return item;
        }).toList();

        order.setItems(items);
        Order saved = orderRepo.save(order);

        publisher.publish(new OrderPlacedEvent(
                saved.getId(), saved.getRestaurantId(), saved.getCustomerId(), saved.getStatus()));

        return mapToDto(saved);
    }

    @Override
    public List<OrderResponseDto> getOrdersByCustomer(Long customerId) {
        return orderRepo.findByCustomerId(customerId)
                .stream().map(this::mapToDto).toList();
    }

    @Override
    public List<OrderResponseDto> getOrdersByRestaurant(Long restaurantId, Long userId, String role) {
        if (!"ROLE_RESTAURANT_OWNER".equals(role)) {
            throw new AccessDeniedException("Only restaurant owners can view orders.");
        }

        return orderRepo.findByRestaurantId(restaurantId)
                .stream().map(this::mapToDto).toList();
    }

    private OrderResponseDto mapToDto(Order order) {
        return OrderResponseDto.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .restaurantId(order.getRestaurantId())
                .status(order.getStatus())
                .items(order.getItems().stream().map(this::mapItemToDto).toList())
                .build();
    }

    private OrderItemResponseDto mapItemToDto(OrderItem item) {
        return OrderItemResponseDto.builder()
                .itemName(item.getItemName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .build();
    }
}
