package org.sikawofie.orderservice.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponseDto {
    private String itemName;
    private int quantity;
    private double price;
}
