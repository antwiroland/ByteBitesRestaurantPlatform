package org.sikawofie.restaurantservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class MenuItemResponseDto {
    private Long id;
    private String name;
    private String description;
    private double price;
}
