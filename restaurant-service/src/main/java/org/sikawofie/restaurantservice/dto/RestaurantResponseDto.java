package org.sikawofie.restaurantservice.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;



@Data
@Builder
public class RestaurantResponseDto {
    private Long id;
    private String name;
    private String description;
    private String location;
    private Long ownerId;
    private List<MenuItemResponseDto> menuItems;
}
