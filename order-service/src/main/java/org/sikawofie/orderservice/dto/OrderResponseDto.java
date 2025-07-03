package org.sikawofie.orderservice.dto;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderResponseDto {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private String status;
    private List<OrderItemResponseDto> items;
}
