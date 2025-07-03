package org.sikawofie.orderservice.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemRequestDto {
    @NotBlank
    private String itemName;

    @Min(1)
    private int quantity;

    private double price;
}
