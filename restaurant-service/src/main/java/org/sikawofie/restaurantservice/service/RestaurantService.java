package org.sikawofie.restaurantservice.service;


import org.sikawofie.restaurantservice.dto.*;

import java.util.List;

public interface RestaurantService {

    RestaurantResponseDto createRestaurant(RestaurantRequestDto restaurant, Long ownerId, String role);

    List<RestaurantResponseDto> getAll();

    List<MenuItemResponseDto> getMenu(Long restaurantId);

    MenuItemResponseDto addMenuItem(Long restaurantId, MenuItemRequestDto item, Long ownerId, String role);
}
