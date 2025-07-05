package org.sikawofie.restaurantservice.mappers;

import org.sikawofie.restaurantservice.dto.RestaurantRequestDto;
import org.sikawofie.restaurantservice.entity.Restaurant;
import org.springframework.stereotype.Component;

@Component
public class RestaurantRequestDtoToRestaurant {
    public Restaurant toDTO(RestaurantRequestDto dto) {
        return Restaurant.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .imageUrl(dto.getImageUrl())
                .status(dto.getStatus())
                .build();
    }
}
