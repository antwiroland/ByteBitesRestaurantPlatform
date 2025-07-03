package org.sikawofie.restaurantservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MenuItemResponseDto {
    private Long id;
    private String name;
    private String description;
    private double price;
}
