package org.sikawofie.restaurantservice.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sikawofie.restaurantservice.enums.RestaurantStatus;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class RestaurantRequestDto {
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String location;

    private String email;
    private String phoneNumber;
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private RestaurantStatus status;

    private List<MenuItemRequestDto> menuItems;
}
