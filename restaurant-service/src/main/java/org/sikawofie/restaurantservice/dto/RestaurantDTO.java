package org.sikawofie.restaurantservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sikawofie.restaurantservice.enums.RestaurantStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class RestaurantDTO {
    private Long id;
    private String name;
    private String description;
    private String location;
    private String phoneNumber;
    private String email;
    private String imageUrl;
    private RestaurantStatus status;
}
