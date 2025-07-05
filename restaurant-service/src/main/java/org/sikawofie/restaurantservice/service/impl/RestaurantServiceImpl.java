package org.sikawofie.restaurantservice.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sikawofie.restaurantservice.dto.*;
import org.sikawofie.restaurantservice.entity.MenuItem;
import org.sikawofie.restaurantservice.entity.Restaurant;
import org.sikawofie.restaurantservice.enums.RestaurantStatus;
import org.sikawofie.restaurantservice.exceptions.BusinessException;
import org.sikawofie.restaurantservice.exceptions.ResourceNotFoundException;
import org.sikawofie.restaurantservice.exceptions.UnauthorizedException;
import org.sikawofie.restaurantservice.mappers.RestaurantRequestDtoToRestaurant;
import org.sikawofie.restaurantservice.mappers.RestaurantToDtoMapper;
import org.sikawofie.restaurantservice.repository.MenuItemRepository;
import org.sikawofie.restaurantservice.repository.RestaurantRepository;
import org.sikawofie.restaurantservice.service.RestaurantService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantToDtoMapper restaurantMapper;
    private final RestaurantRequestDtoToRestaurant requestMapper;

    @Override
    public RestaurantResponseDto createRestaurant(RestaurantRequestDto dto, Long ownerId, String role) {
        if (!"ROLE_RESTAURANT_OWNER".equals(role)) {
            throw new AccessDeniedException("Only restaurant owners can create restaurants.");
        }
        Restaurant restaurant = requestMapper.toDTO(dto);
        restaurant.setOwnerId(ownerId);
        restaurant.setStatus(RestaurantStatus.PENDING);
        Restaurant saved = restaurantRepository.save(restaurant);
        return mapToResponseDto(saved);
    }

    @Override
    public List<RestaurantResponseDto> getAll() {
        return restaurantRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MenuItemResponseDto> getMenu(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId).stream()
                .map(this::mapMenuItemToDto)
                .collect(Collectors.toList());
    }

    @Override
    public MenuItemResponseDto addMenuItem(Long restaurantId, MenuItemRequestDto dto, Long ownerId, String role) {
        if (!"ROLE_RESTAURANT_OWNER".equals(role)) {
            throw new AccessDeniedException("Unauthorized to add menu items.");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found."));

        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("You do not own this restaurant.");
        }

        MenuItem item = MenuItem.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .restaurant(restaurant)
                .build();

        return mapMenuItemToDto(menuItemRepository.save(item));
    }

    @Override
    @Transactional
    public RestaurantResponseDto updateRestaurant(Long id, RestaurantRequestDto request, Long ownerId) {
        Restaurant restaurant = restaurantRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new UnauthorizedException("Not authorized to update this restaurant."));

        if (!restaurant.getEmail().equals(request.getEmail()) &&
                restaurantRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use.");
        }

        if (!restaurant.getPhoneNumber().equals(request.getPhoneNumber()) &&
                restaurantRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessException("Phone number already in use.");
        }

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setLocation(request.getLocation());
        restaurant.setEmail(request.getEmail());
        restaurant.setPhoneNumber(request.getPhoneNumber());
        restaurant.setImageUrl(request.getImageUrl());

        return mapToResponseDto(restaurantRepository.save(restaurant));
    }

    @Override
    public RestaurantDTO updateRestaurantStatus(Long id, RestaurantStatus status) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + id));
        restaurant.setStatus(status);
        return restaurantMapper.toDTO(restaurantRepository.save(restaurant));
    }

    @Override
    public List<RestaurantDTO> getAllActiveRestaurants() {
        return restaurantRepository.findByStatus(RestaurantStatus.ACTIVE).stream()
                .map(restaurantMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RestaurantDTO getRestaurantById(Long id) {
        return restaurantMapper.toDTO(
                restaurantRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found."))
        );
    }

    @Override
    public List<RestaurantDTO> getRestaurantsByOwner(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId).stream()
                .map(restaurantMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RestaurantDTO> searchRestaurantsByName(String name) {
        return restaurantRepository.findByNameContainingIgnoreCase(name).stream()
                .map(restaurantMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RestaurantDTO> searchRestaurantsByAddress(String address) {
        return restaurantRepository.findByLocationContainingIgnoreCase(address).stream()
                .map(restaurantMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @CircuitBreaker(name = "restaurant-service", fallbackMethod = "fallbackGetAllRestaurants")
    public List<RestaurantDTO> getRestaurantsWithCircuitBreaker() {
        return getAllActiveRestaurants();
    }

    // Circuit breaker fallback
    public List<RestaurantDTO> fallbackGetAllRestaurants(Exception ex) {
        log.warn("Circuit breaker triggered: {}", ex.getMessage());
        return List.of();
    }

    // Helper DTO mappings
    private RestaurantResponseDto mapToResponseDto(Restaurant restaurant) {
        return RestaurantResponseDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .location(restaurant.getLocation())
                .status(restaurant.getStatus())
                .ownerId(restaurant.getOwnerId())
                .menuItems(restaurant.getMenuItems() != null
                        ? restaurant.getMenuItems().stream().map(this::mapMenuItemToDto).toList()
                        : List.of())
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
