package org.sikawofie.restaurantservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class MenuItemRequestDto {

    @NotBlank(message = "Menu item name is required")
    private String name;

    @NotBlank(message = "Menu item description is required")
    private String description;

    @Positive(message = "Price must be positive")
    private double price;
}
