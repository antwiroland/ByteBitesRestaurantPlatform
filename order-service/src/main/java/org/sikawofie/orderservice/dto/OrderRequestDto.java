package org.sikawofie.orderservice.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderRequestDto {
    @NotNull
    private Long restaurantId;

    @NotNull
    private List<OrderItemRequestDto> items;
}
