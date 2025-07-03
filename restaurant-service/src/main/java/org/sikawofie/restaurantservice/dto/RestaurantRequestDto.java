package org.sikawofie.restaurantservice.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Data
@Builder
public class RestaurantRequestDto {

    @NotBlank(message = "Restaurant name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    private List<MenuItemRequestDto> menuItems;
}
