package org.sikawofie.restaurantservice.service.impl;

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
        log.info("Creating restaurant for ownerId={} with role={}", ownerId, role);
        if (!"OWNER".equals(role)) {
            log.warn("Access denied: role '{}' cannot create restaurant", role);
            throw new AccessDeniedException("Only restaurant owners can create restaurants.");
        }

        Restaurant restaurant = requestMapper.toDTO(dto);
        restaurant.setOwnerId(ownerId);
        restaurant.setStatus(RestaurantStatus.PENDING);
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Restaurant '{}' created with ID {}", saved.getName(), saved.getId());

        return mapToResponseDto(saved);
    }

    @Override
    public List<RestaurantResponseDto> getAll() {
        log.info("Fetching all restaurants");
        List<RestaurantResponseDto> list = restaurantRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
        log.debug("Found {} restaurants", list.size());
        return list;
    }

    @Override
    public List<MenuItemResponseDto> getMenu(Long restaurantId) {
        log.info("Fetching menu for restaurant ID {}", restaurantId);
        List<MenuItemResponseDto> list = menuItemRepository.findByRestaurantId(restaurantId).stream()
                .map(this::mapMenuItemToDto)
                .collect(Collectors.toList());
        log.debug("Found {} menu items", list.size());
        return list;
    }

    @Override
    public MenuItemResponseDto addMenuItem(Long restaurantId, MenuItemRequestDto dto, Long ownerId, String role) {
        log.info("Adding menu item to restaurantId={} by ownerId={} with role={}", restaurantId, ownerId, role);

        if (!"OWNER".equals(role)) {
            log.warn("Access denied: role '{}' cannot add menu items", role);
            throw new AccessDeniedException("Unauthorized to add menu items.");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> {
                    log.warn("Restaurant with ID {} not found", restaurantId);
                    return new ResourceNotFoundException("Restaurant not found.");
                });

        if (!restaurant.getOwnerId().equals(ownerId)) {
            log.warn("Unauthorized access: user {} does not own restaurant {}", ownerId, restaurantId);
            throw new AccessDeniedException("You do not own this restaurant.");
        }

        MenuItem item = MenuItem.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .restaurant(restaurant)
                .build();

        MenuItem savedItem = menuItemRepository.save(item);
        log.info("Menu item '{}' added to restaurant {}", savedItem.getName(), restaurantId);
        return mapMenuItemToDto(savedItem);
    }

    @Override
    @Transactional
    public RestaurantResponseDto updateRestaurant(Long id, RestaurantRequestDto request, Long ownerId) {
        log.info("Updating restaurant ID={} by owner ID={}", id, ownerId);

        Restaurant restaurant = restaurantRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> {
                    log.warn("Unauthorized update attempt on restaurant ID={} by user ID={}", id, ownerId);
                    return new UnauthorizedException("Not authorized to update this restaurant.");
                });

        if (!restaurant.getEmail().equals(request.getEmail()) &&
                restaurantRepository.existsByEmail(request.getEmail())) {
            log.warn("Email '{}' already in use", request.getEmail());
            throw new BusinessException("Email already in use.");
        }

        if (!restaurant.getPhoneNumber().equals(request.getPhoneNumber()) &&
                restaurantRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            log.warn("Phone number '{}' already in use", request.getPhoneNumber());
            throw new BusinessException("Phone number already in use.");
        }

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setLocation(request.getLocation());
        restaurant.setEmail(request.getEmail());
        restaurant.setPhoneNumber(request.getPhoneNumber());
        restaurant.setImageUrl(request.getImageUrl());

        Restaurant updated = restaurantRepository.save(restaurant);
        log.info("Restaurant ID={} updated successfully", updated.getId());

        return mapToResponseDto(updated);
    }

    @Override
    public RestaurantDTO updateRestaurantStatus(Long id, RestaurantStatus status) {
        log.info("Updating status of restaurant ID={} to {}", id, status);
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Restaurant not found with ID {}", id);
                    return new ResourceNotFoundException("Restaurant not found with ID: " + id);
                });
        restaurant.setStatus(status);
        Restaurant updated = restaurantRepository.save(restaurant);
        log.info("Status of restaurant ID={} updated to {}", id, status);
        return restaurantMapper.toDTO(updated);
    }

    @Override
    public List<RestaurantDTO> getAllActiveRestaurants() {
        log.info("Fetching all active restaurants");
        List<RestaurantDTO> list = restaurantRepository.findByStatus(RestaurantStatus.ACTIVE).stream()
                .map(restaurantMapper::toDTO)
                .collect(Collectors.toList());
        log.debug("Found {} active restaurants", list.size());
        return list;
    }

    @Override
    public RestaurantDTO getRestaurantById(Long id) {
        log.info("Fetching restaurant by ID {}", id);
        return restaurantMapper.toDTO(
                restaurantRepository.findById(id)
                        .orElseThrow(() -> {
                            log.warn("Restaurant with ID {} not found", id);
                            return new ResourceNotFoundException("Restaurant not found.");
                        })
        );
    }

    @Override
    public List<RestaurantDTO> getRestaurantsByOwner(Long ownerId) {
        log.info("Fetching restaurants by owner ID {}", ownerId);
        return restaurantRepository.findByOwnerId(ownerId).stream()
                .map(restaurantMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RestaurantDTO> searchRestaurantsByName(String name) {
        log.info("Searching restaurants by name: {}", name);
        return restaurantRepository.findByNameContainingIgnoreCase(name).stream()
                .map(restaurantMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RestaurantDTO> searchRestaurantsByAddress(String address) {
        log.info("Searching restaurants by address: {}", address);
        return restaurantRepository.findByLocationContainingIgnoreCase(address).stream()
                .map(restaurantMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<RestaurantDTO> fallbackGetAllRestaurants(Exception ex) {
        log.warn("Circuit breaker fallback triggered due to: {}", ex.getMessage());
        return List.of();
    }

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
