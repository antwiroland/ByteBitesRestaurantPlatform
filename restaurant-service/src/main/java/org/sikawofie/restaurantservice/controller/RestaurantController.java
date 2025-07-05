package org.sikawofie.restaurantservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sikawofie.restaurantservice.dto.*;
import org.sikawofie.restaurantservice.enums.RestaurantStatus;
import org.sikawofie.restaurantservice.service.RestaurantService;
import org.sikawofie.restaurantservice.utils.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/restaurant/")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService service;

    private <T> ResponseEntity<ApiResponse<T>> buildResponse(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status)
                .body(ApiResponse.<T>builder()
                        .status(status.value())
                        .message(message)
                        .data(data)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<ApiResponse<RestaurantResponseDto>> create(
            @RequestBody @Valid RestaurantRequestDto dto
    ) {
        Long ownerId = SecurityUtils.getUserId();
        String role = SecurityUtils.getUserRole();

        RestaurantResponseDto created = service.createRestaurant(dto, ownerId, role);
        return buildResponse(HttpStatus.CREATED, "Restaurant created successfully", created);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantResponseDto>>> getAll() {
        return buildResponse(HttpStatus.OK, "Restaurants retrieved", service.getAll());
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<ApiResponse<List<MenuItemResponseDto>>> getMenu(@PathVariable Long id) {
        return buildResponse(HttpStatus.OK, "Menu retrieved", service.getMenu(id));
    }

    @PostMapping("/{id}/menu")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('OWNER') and @restaurantService.isOwner(#id, authentication.principal.userId))")
    public ResponseEntity<ApiResponse<MenuItemResponseDto>> addMenuItem(
            @PathVariable Long id,
            @RequestBody @Valid MenuItemRequestDto item
    ) {
        Long ownerId = SecurityUtils.getUserId();
        String role = SecurityUtils.getUserRole();

        MenuItemResponseDto added = service.addMenuItem(id, item, ownerId, role);
        return buildResponse(HttpStatus.OK, "Menu item added", added);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('OWNER') and @restaurantService.isOwner(#id, authentication.principal.userId))")
    public ResponseEntity<ApiResponse<RestaurantResponseDto>> updateRestaurant(
            @PathVariable Long id,
            @RequestBody @Valid RestaurantRequestDto dto
    ) {
        Long ownerId = SecurityUtils.getUserId();
        RestaurantResponseDto updated = service.updateRestaurant(id, dto, ownerId);
        return buildResponse(HttpStatus.OK, "Restaurant updated", updated);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam RestaurantStatus status
    ) {
        RestaurantDTO updated = service.updateRestaurantStatus(id, status);
        return buildResponse(HttpStatus.OK, "Status updated", updated);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<RestaurantDTO>>> getAllActive() {
        return buildResponse(HttpStatus.OK, "Active restaurants retrieved", service.getAllActiveRestaurants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantDTO>> getById(@PathVariable Long id) {
        return buildResponse(HttpStatus.OK, "Restaurant retrieved", service.getRestaurantById(id));
    }

    @GetMapping("/owner")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<ApiResponse<List<RestaurantDTO>>> getByOwner() {
        Long ownerId = SecurityUtils.getUserId();
        return buildResponse(HttpStatus.OK, "Owner's restaurants retrieved", service.getRestaurantsByOwner(ownerId));
    }

    @GetMapping("/search/name")
    public ResponseEntity<ApiResponse<List<RestaurantDTO>>> searchByName(@RequestParam String name) {
        return buildResponse(HttpStatus.OK, "Search by name results", service.searchRestaurantsByName(name));
    }

    @GetMapping("/search/address")
    public ResponseEntity<ApiResponse<List<RestaurantDTO>>> searchByAddress(@RequestParam String address) {
        return buildResponse(HttpStatus.OK, "Search by address results", service.searchRestaurantsByAddress(address));
    }
}