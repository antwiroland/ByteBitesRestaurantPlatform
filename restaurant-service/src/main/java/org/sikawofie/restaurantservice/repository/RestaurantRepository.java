package org.sikawofie.restaurantservice.repository;

import org.sikawofie.restaurantservice.entity.Restaurant;
import org.sikawofie.restaurantservice.enums.RestaurantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    List<Restaurant> findByOwnerId(Long ownerId);

    List<Restaurant> findByNameContainingIgnoreCase(String name);

    List<Restaurant> findByLocationContainingIgnoreCase(String address);

    // NEW: Find all restaurants by status
    List<Restaurant> findByStatus(RestaurantStatus status);
}
