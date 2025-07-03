package org.sikawofie.restaurantservice.service.impl;



import lombok.RequiredArgsConstructor;
import org.sikawofie.restaurantservice.dto.*;
import org.sikawofie.restaurantservice.entity.MenuItem;
import org.sikawofie.restaurantservice.entity.Restaurant;
import org.sikawofie.restaurantservice.exceptions.ResourceNotFoundException;
import org.sikawofie.restaurantservice.repository.MenuItemRepository;
import org.sikawofie.restaurantservice.repository.RestaurantRepository;
import org.sikawofie.restaurantservice.service.RestaurantService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepo;
    private final MenuItemRepository menuItemRepo;

    @Override
    public RestaurantResponseDto createRestaurant(RestaurantRequestDto dto, Long ownerId, String role) {
        if (!"ROLE_RESTAURANT_OWNER".equals(role)) {
            throw new AccessDeniedException("You are not authorized to create a restaurant.");
        }

        Restaurant restaurant = Restaurant.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .ownerId(ownerId)
                .build();

        Restaurant saved = restaurantRepo.save(restaurant);
        return mapToDto(saved);
    }

    @Override
    public List<RestaurantResponseDto> getAll() {
        return restaurantRepo.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<MenuItemResponseDto> getMenu(Long restaurantId) {
        return menuItemRepo.findByRestaurantId(restaurantId)
                .stream()
                .map(this::mapMenuItemToDto)
                .toList();
    }

    @Override
    public MenuItemResponseDto addMenuItem(Long restaurantId, MenuItemRequestDto dto, Long ownerId, String role) {
        if (!"ROLE_RESTAURANT_OWNER".equals(role)) {
            throw new AccessDeniedException("You are not authorized to add menu items.");
        }

        Restaurant restaurant = restaurantRepo.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("You do not own this restaurant.");
        }

        MenuItem item = MenuItem.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .restaurant(restaurant)
                .build();

        return mapMenuItemToDto(menuItemRepo.save(item));
    }

    // Mapping helpers
    private RestaurantResponseDto mapToDto(Restaurant restaurant) {
        return RestaurantResponseDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .location(restaurant.getLocation())
                .ownerId(restaurant.getOwnerId())
                .menuItems(
                        restaurant.getMenuItems() != null ?
                                restaurant.getMenuItems().stream().map(this::mapMenuItemToDto).toList()
                                : List.of()
                )
                .build();
    }

    private MenuItemResponseDto mapMenuItemToDto(MenuItem item) {
        return MenuItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .build();
    }
}
