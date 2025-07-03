package org.sikawofie.restaurantservice.controller;


import lombok.RequiredArgsConstructor;
import org.sikawofie.restaurantservice.entity.MenuItem;
import org.sikawofie.restaurantservice.entity.Restaurant;
import org.sikawofie.restaurantservice.service.RestaurantService;
import org.sikawofie.restaurantservice.utils.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService service;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Restaurant restaurant, ServerHttpRequest request) {
        Long ownerId = SecurityUtils.getCurrentUserId(request);
        String role = SecurityUtils.getCurrentUserRole(request);
        if (!role.equals("ROLE_RESTAURANT_OWNER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createRestaurant(restaurant, ownerId));
    }

    @GetMapping
    public List<Restaurant> all() {
        return service.getAll();
    }

    @GetMapping("/{id}/menu")
    public List<MenuItem> getMenu(@PathVariable Long id) {
        return service.getMenu(id);
    }

    @PostMapping("/{id}/menu")
    public ResponseEntity<?> addMenu(@PathVariable Long id,
                                     @RequestBody MenuItem item,
                                     ServerHttpRequest request) {
        Long ownerId = SecurityUtils.getCurrentUserId(request);
        return ResponseEntity.ok(service.addMenuItem(id, item, ownerId));
    }
}
