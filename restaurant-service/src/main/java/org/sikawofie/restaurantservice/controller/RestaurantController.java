package org.sikawofie.restaurantservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sikawofie.restaurantservice.dto.*;
import org.sikawofie.restaurantservice.service.RestaurantService;
import org.sikawofie.restaurantservice.utils.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService service;

    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantResponseDto>> create(
            @RequestBody @Valid RestaurantRequestDto restaurant,
            ServerHttpRequest request
    ) {
        Long ownerId = SecurityUtils.getCurrentUserId(request);
        String role = SecurityUtils.getCurrentUserRole(request);

        RestaurantResponseDto created = service.createRestaurant(restaurant, ownerId, role);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<RestaurantResponseDto>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Restaurant created successfully")
                        .data(created)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantResponseDto>>> all() {
        List<RestaurantResponseDto> list = service.getAll();
        return ResponseEntity.ok(ApiResponse.<List<RestaurantResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Restaurants retrieved")
                .data(list)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<ApiResponse<List<MenuItemResponseDto>>> getMenu(@PathVariable Long id) {
        List<MenuItemResponseDto> menu = service.getMenu(id);
        return ResponseEntity.ok(ApiResponse.<List<MenuItemResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Menu retrieved")
                .data(menu)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/{id}/menu")
    public ResponseEntity<ApiResponse<MenuItemResponseDto>> addMenu(
            @PathVariable Long id,
            @RequestBody @Valid MenuItemRequestDto item,
            ServerHttpRequest request
    ) {
        Long ownerId = SecurityUtils.getCurrentUserId(request);
        String role = SecurityUtils.getCurrentUserRole(request);

        MenuItemResponseDto added = service.addMenuItem(id, item, ownerId, role);

        return ResponseEntity.ok(ApiResponse.<MenuItemResponseDto>builder()
                .status(HttpStatus.OK.value())
                .message("Menu item added successfully")
                .data(added)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
