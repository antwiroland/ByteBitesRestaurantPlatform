package org.sikawofie.restaurantservice.mappers;

import org.sikawofie.restaurantservice.dto.RestaurantDTO;
import org.sikawofie.restaurantservice.entity.Restaurant;
import org.springframework.stereotype.Component;

@Component
public class RestaurantToDtoMapper {
    public RestaurantDTO toDTO(Restaurant restaurant) {
        return RestaurantDTO.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .location(restaurant.getLocation())
                .phoneNumber(restaurant.getPhoneNumber())
                .email(restaurant.getEmail())
                .imageUrl(restaurant.getImageUrl())
                .status(restaurant.getStatus())
                .build();
    }
}
