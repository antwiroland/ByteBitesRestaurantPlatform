package org.sikawofie.restaurantservice.service.integrationTest;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockedStatic;
import org.sikawofie.restaurantservice.dto.*;
import org.sikawofie.restaurantservice.enums.RestaurantStatus;
import org.sikawofie.restaurantservice.service.RestaurantService;
import org.sikawofie.restaurantservice.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class RestaurantControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        // Mock static methods for SecurityUtils
        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);
        securityUtilsMock.when(SecurityUtils::getUserRole).thenReturn("OWNER");
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        public RestaurantService restaurantService() {
            return Mockito.mock(RestaurantService.class);
        }
    }

    @Test
    void testGetAllRestaurants() throws Exception {
        RestaurantResponseDto r1 = RestaurantResponseDto.builder().id(1L).name("R1").build();
        RestaurantResponseDto r2 = RestaurantResponseDto.builder().id(2L).name("R2").build();

        Mockito.when(restaurantService.getAll()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/restaurant/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("R1"))
                .andExpect(jsonPath("$.data[1].id").value(2));
    }

    @Test
    void testCreateRestaurant() throws Exception {
        RestaurantRequestDto requestDto = RestaurantRequestDto.builder()
                .name("New Place")
                .description("A lovely spot")
                .location("City Center")
                .email("owner@newplace.com")
                .phoneNumber("1234567890")
                .imageUrl("https://example.com/img.jpg")
                .build();

        RestaurantResponseDto responseDto = RestaurantResponseDto.builder()
                .id(10L)
                .name("New Place")
                .ownerId(1L)
                .status(RestaurantStatus.PENDING)
                .build();

        Mockito.when(restaurantService.createRestaurant(any(), anyLong(), anyString()))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/restaurant/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.name").value("New Place"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.ownerId").value(1L));
    }
}