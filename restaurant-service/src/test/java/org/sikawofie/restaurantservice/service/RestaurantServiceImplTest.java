package org.sikawofie.restaurantservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.sikawofie.restaurantservice.dto.*;
import org.sikawofie.restaurantservice.entity.MenuItem;
import org.sikawofie.restaurantservice.entity.Restaurant;
import org.sikawofie.restaurantservice.enums.RestaurantStatus;
import org.sikawofie.restaurantservice.exceptions.*;
import org.sikawofie.restaurantservice.mappers.RestaurantRequestDtoToRestaurant;
import org.sikawofie.restaurantservice.mappers.RestaurantToDtoMapper;
import org.sikawofie.restaurantservice.repository.MenuItemRepository;
import org.sikawofie.restaurantservice.repository.RestaurantRepository;
import org.sikawofie.restaurantservice.service.impl.RestaurantServiceImpl;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class RestaurantServiceImplTest {

        @Mock private RestaurantRepository restaurantRepository;
        @Mock private MenuItemRepository menuItemRepository;
        @Mock private RestaurantToDtoMapper restaurantMapper;
        @Mock private RestaurantRequestDtoToRestaurant requestMapper;

        @InjectMocks
        private RestaurantServiceImpl restaurantService;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
        }

        @Test
        void testCreateRestaurant_UnauthorizedRole_ShouldThrowException() {
            RestaurantRequestDto dto = new RestaurantRequestDto();
            assertThrows(AccessDeniedException.class,
                    () -> restaurantService.createRestaurant(dto, 1L, "CUSTOMER"));
        }

        @Test
        void testCreateRestaurant_Success() {
            RestaurantRequestDto dto = new RestaurantRequestDto();
            Restaurant restaurant = new Restaurant();
            restaurant.setOwnerId(1L);
            restaurant.setStatus(RestaurantStatus.PENDING);

            when(requestMapper.toDTO(dto)).thenReturn(restaurant);
            when(restaurantRepository.save(any())).thenReturn(restaurant);

            RestaurantResponseDto response = restaurantService.createRestaurant(dto, 1L, "OWNER");
            assertEquals(RestaurantStatus.PENDING, response.getStatus());
            verify(restaurantRepository).save(any(Restaurant.class));
        }

        @Test
        void testGetAll_ShouldReturnRestaurantList() {
            List<Restaurant> mockList = List.of(new Restaurant(), new Restaurant());
            when(restaurantRepository.findAll()).thenReturn(mockList);

            List<RestaurantResponseDto> result = restaurantService.getAll();
            assertEquals(2, result.size());
        }

        @Test
        void testGetMenu_ShouldReturnMenuItemList() {
            MenuItem item = MenuItem.builder()
                    .id(1L)
                    .name("Burger")
                    .description("Tasty")
                    .price(Double.valueOf(5.99))
                    .build();

            when(menuItemRepository.findByRestaurantId(1L)).thenReturn(List.of(item));

            List<MenuItemResponseDto> result = restaurantService.getMenu(1L);
            assertEquals(1, result.size());
            assertEquals("Burger", result.get(0).getName());
        }

        @Test
        void testAddMenuItem_WithUnauthorizedRole_ShouldThrow() {
            assertThrows(AccessDeniedException.class,
                    () -> restaurantService.addMenuItem(1L, new MenuItemRequestDto(), 1L, "CUSTOMER"));
        }

        @Test
        void testAddMenuItem_WithMismatchedOwner_ShouldThrow() {
            Restaurant restaurant = new Restaurant();
            restaurant.setOwnerId(99L);
            when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

            assertThrows(AccessDeniedException.class,
                    () -> restaurantService.addMenuItem(1L, new MenuItemRequestDto(), 1L, "OWNER"));
        }

        @Test
        void testUpdateRestaurant_Success() {
            Restaurant existing = new Restaurant();
            existing.setId(1L);
            existing.setOwnerId(1L);
            existing.setEmail("old@mail.com");
            existing.setPhoneNumber("123");

            RestaurantRequestDto update = new RestaurantRequestDto();
            update.setEmail("new@mail.com");
            update.setPhoneNumber("456");

            when(restaurantRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(existing));
            when(restaurantRepository.existsByEmail("new@mail.com")).thenReturn(false);
            when(restaurantRepository.existsByPhoneNumber("456")).thenReturn(false);
            when(restaurantRepository.save(any())).thenReturn(existing);

            RestaurantResponseDto result = restaurantService.updateRestaurant(1L, update, 1L);
            assertNotNull(result);
        }

        @Test
        void testUpdateRestaurant_DuplicateEmail_ShouldThrow() {
            Restaurant existing = new Restaurant();
            existing.setEmail("existing@mail.com");
            existing.setPhoneNumber("123");

            RestaurantRequestDto request = new RestaurantRequestDto();
            request.setEmail("duplicate@mail.com");
            request.setPhoneNumber("123");

            when(restaurantRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(existing));
            when(restaurantRepository.existsByEmail("duplicate@mail.com")).thenReturn(true);

            assertThrows(BusinessException.class,
                    () -> restaurantService.updateRestaurant(1L, request, 1L));
        }

        @Test
        void testUpdateRestaurantStatus_Success() {
            Restaurant restaurant = new Restaurant();
            restaurant.setStatus(RestaurantStatus.PENDING);

            when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
            when(restaurantRepository.save(any())).thenReturn(restaurant);
            when(restaurantMapper.toDTO(any())).thenReturn(new RestaurantDTO());

            RestaurantDTO result = restaurantService.updateRestaurantStatus(1L, RestaurantStatus.ACTIVE);
            assertNotNull(result);
        }

        @Test
        void testGetRestaurantById_NotFound_ShouldThrow() {
            when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> restaurantService.getRestaurantById(1L));
        }

        @Test
        void testFallbackGetAllRestaurants_ShouldReturnEmptyList() {
            List<RestaurantDTO> fallback = restaurantService.fallbackGetAllRestaurants(new RuntimeException("CB open"));
            assertTrue(fallback.isEmpty());
        }


}
