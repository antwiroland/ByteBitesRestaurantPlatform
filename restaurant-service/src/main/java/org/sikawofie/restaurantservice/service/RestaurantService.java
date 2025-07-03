package org.sikawofie.restaurantservice.service;


import org.sikawofie.restaurantservice.entity.MenuItem;
import org.sikawofie.restaurantservice.entity.Restaurant;

import java.util.List;


public interface RestaurantService {



    Restaurant createRestaurant(Restaurant restaurant, Long ownerId);

    List<Restaurant> getAll();

    List<MenuItem> getMenu(Long restaurantId);

    MenuItem addMenuItem(Long restaurantId, MenuItem item, Long ownerId);


}
