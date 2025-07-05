package org.sikawofie.restaurantservice.service;

import org.sikawofie.restaurantservice.dto.*;
import org.sikawofie.restaurantservice.enums.RestaurantStatus;

import java.util.List;

public interface RestaurantService {

    RestaurantResponseDto createRestaurant(RestaurantRequestDto restaurantRequestDto, Long ownerId, String role);

    List<RestaurantResponseDto> getAll();

    List<MenuItemResponseDto> getMenu(Long restaurantId);

    MenuItemResponseDto addMenuItem(Long restaurantId, MenuItemRequestDto menuItemRequestDto, Long ownerId, String role);

    RestaurantResponseDto updateRestaurant(Long id, RestaurantRequestDto request, Long ownerId);

    RestaurantDTO updateRestaurantStatus(Long id, RestaurantStatus status);

    List<RestaurantDTO> getAllActiveRestaurants();

    RestaurantDTO getRestaurantById(Long id);

    List<RestaurantDTO> getRestaurantsByOwner(Long ownerId);

    List<RestaurantDTO> searchRestaurantsByName(String name);

    List<RestaurantDTO> searchRestaurantsByAddress(String address);


    List<RestaurantDTO> getRestaurantsWithCircuitBreaker();
}
