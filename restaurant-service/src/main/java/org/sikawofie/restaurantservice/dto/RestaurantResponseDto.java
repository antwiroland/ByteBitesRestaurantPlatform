package org.sikawofie.restaurantservice.dto;

import lombok.Builder;
import lombok.Data;
import org.sikawofie.restaurantservice.enums.RestaurantStatus;

import java.util.List;

@Data
@Builder
public class RestaurantResponseDto {
    private Long id;
    private String name;
    private String description;
    private String location;
    private Long ownerId;
    private RestaurantStatus status;
    private List<MenuItemResponseDto> menuItems;
}
