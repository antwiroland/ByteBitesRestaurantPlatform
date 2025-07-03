package org.sikawofie.restaurantservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.sikawofie.restaurantservice.entity.MenuItem;
import org.sikawofie.restaurantservice.entity.Restaurant;
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

    public Restaurant createRestaurant(Restaurant restaurant, Long ownerId) {
        restaurant.setOwnerId(ownerId);
        return restaurantRepo.save(restaurant);
    }

    public List<Restaurant> getAll() {
        return restaurantRepo.findAll();
    }

    public List<MenuItem> getMenu(Long restaurantId) {
        return menuItemRepo.findByRestaurantId(restaurantId);
    }

    public MenuItem addMenuItem(Long restaurantId, MenuItem item, Long ownerId) {
        Restaurant restaurant = restaurantRepo.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("You don't own this restaurant");
        }

        item.setRestaurant(restaurant);
        return menuItemRepo.save(item);
    }
}
