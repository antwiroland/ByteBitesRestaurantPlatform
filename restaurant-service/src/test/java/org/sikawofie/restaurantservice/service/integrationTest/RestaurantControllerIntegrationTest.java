package org.sikawofie.restaurantservice.service.integrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sikawofie.restaurantservice.dto.MenuItemRequestDto;
import org.sikawofie.restaurantservice.dto.RestaurantRequestDto;
import org.sikawofie.restaurantservice.entity.MenuItem;
import org.sikawofie.restaurantservice.entity.Restaurant;
import org.sikawofie.restaurantservice.enums.RestaurantStatus;
import org.sikawofie.restaurantservice.repository.MenuItemRepository;
import org.sikawofie.restaurantservice.repository.RestaurantRepository;
import org.sikawofie.restaurantservice.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RestaurantControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @MockBean
    private SecurityUtils securityUtils;

    private Restaurant testRestaurant;
    private MenuItem testMenuItem;

    @BeforeEach
    void setUp() {
        menuItemRepository.deleteAll();
        restaurantRepository.deleteAll();

        testRestaurant = Restaurant.builder()
                .name("Test Restaurant")
                .description("Test Description")
                .location("Test Location")
                .email("test@example.com")
                .phoneNumber("1234567890")
                .status(RestaurantStatus.ACTIVE)
                .ownerId(1L)
                .build();
        testRestaurant = restaurantRepository.save(testRestaurant);

        testMenuItem = MenuItem.builder()
                .name("Test Item")
                .description("Test Item Description")
                .price(9.99)
                .restaurant(testRestaurant)
                .build();
        testMenuItem = menuItemRepository.save(testMenuItem);

        when(securityUtils.getUserId()).thenReturn(1L);
        when(securityUtils.getUserRole()).thenReturn("OWNER");
    }

    @Test
    @WithMockUser(roles = {"OWNER"})
    void createRestaurant_ShouldReturnCreatedRestaurant() throws Exception {
        RestaurantRequestDto request = RestaurantRequestDto.builder()
                .name("New Restaurant")
                .description("New Description")
                .location("New Location")
                .email("new@example.com")
                .phoneNumber("9876543210")
                .imageUrl("image.jpg")
                .status(RestaurantStatus.PENDING)
                .build();

        mockMvc.perform(post("/api/restaurant/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.message", is("Restaurant created successfully")))
                .andExpect(jsonPath("$.data.name", is("New Restaurant")))
                .andExpect(jsonPath("$.data.status", is("PENDING")));

        verify(securityUtils, atLeastOnce()).getUserId();
        verify(securityUtils, atLeastOnce()).getUserRole();
    }

    @Test
    @WithMockUser
    void getAllRestaurants_ShouldReturnAllRestaurants() throws Exception {
        mockMvc.perform(get("/api/restaurant/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Restaurants retrieved")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is(testRestaurant.getName())));
    }

    @Test
    @WithMockUser
    void getRestaurantMenu_ShouldReturnMenuItems() throws Exception {
        mockMvc.perform(get("/api/restaurant/{id}/menu", testRestaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Menu retrieved")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is(testMenuItem.getName())));
    }

    @Test
    @WithMockUser(roles = {"OWNER"})
    void addMenuItem_ShouldReturnCreatedMenuItem() throws Exception {
        MenuItemRequestDto request = MenuItemRequestDto.builder()
                .name("New Item")
                .description("New Item Description")
                .price(12.99)
                .build();

        mockMvc.perform(post("/api/restaurant/{id}/menu", testRestaurant.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Menu item added")))
                .andExpect(jsonPath("$.data.name", is("New Item")))
                .andExpect(jsonPath("$.data.price", is(12.99)));

        verify(securityUtils, atLeastOnce()).getUserId();
        verify(securityUtils, atLeastOnce()).getUserRole();
    }

    @Test
    @WithMockUser(roles = {"OWNER"})
    void updateRestaurant_ShouldReturnUpdatedRestaurant() throws Exception {
        RestaurantRequestDto request = RestaurantRequestDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .location("Updated Location")
                .email("updated@example.com")
                .phoneNumber("9876543210")
                .imageUrl("updated.jpg")
                .status(RestaurantStatus.ACTIVE)
                .build();

        mockMvc.perform(put("/api/restaurant/{id}", testRestaurant.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Restaurant updated")))
                .andExpect(jsonPath("$.data.name", is("Updated Name")));

        verify(securityUtils, atLeastOnce()).getUserId();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateRestaurantStatus_ShouldReturnUpdatedStatus() throws Exception {
        mockMvc.perform(patch("/api/restaurant/{id}/status?status=SUSPENDED", testRestaurant.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Status updated")))
                .andExpect(jsonPath("$.data.status", is("SUSPENDED")));
    }


    @Test
    @WithMockUser
    void getRestaurantById_ShouldReturnCorrectRestaurant() throws Exception {
        mockMvc.perform(get("/api/restaurant/{id}", testRestaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Restaurant retrieved")))
                .andExpect(jsonPath("$.data.id", is(testRestaurant.getId().intValue())))
                .andExpect(jsonPath("$.data.name", is(testRestaurant.getName())));
    }

    @Test
    @WithMockUser
    void searchRestaurantsByName_ShouldReturnMatchingResults() throws Exception {
        mockMvc.perform(get("/api/restaurant/search/name?name=Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Search by name results")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", containsString("Test")));
    }

    @Test
    @WithMockUser
    void searchRestaurantsByAddress_ShouldReturnMatchingResults() throws Exception {
        mockMvc.perform(get("/api/restaurant/search/address?address=Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Search by address results")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].location", containsString("Test")));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void createRestaurant_WithoutOwnerRole_ShouldReturnForbidden() throws Exception {
        RestaurantRequestDto request = RestaurantRequestDto.builder()
                .name("New Restaurant")
                .description("New Description")
                .location("New Location")
                .email("new@example.com")
                .phoneNumber("9876543210")
                .imageUrl("image.jpg")
                .status(RestaurantStatus.PENDING)
                .build();

        when(securityUtils.getUserRole()).thenReturn("USER");

        mockMvc.perform(post("/api/restaurant/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(securityUtils, atLeastOnce()).getUserRole();
    }

    @Test
    @WithMockUser(roles = {"OWNER"})
    void addMenuItem_ForNonOwnedRestaurant_ShouldReturnForbidden() throws Exception {
        MenuItemRequestDto request = MenuItemRequestDto.builder()
                .name("New Item")
                .description("New Item Description")
                .price(12.99)
                .build();

        when(securityUtils.getUserId()).thenReturn(2L); // Different owner

        mockMvc.perform(post("/api/restaurant/{id}/menu", testRestaurant.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(securityUtils, atLeastOnce()).getUserId();
    }
}
