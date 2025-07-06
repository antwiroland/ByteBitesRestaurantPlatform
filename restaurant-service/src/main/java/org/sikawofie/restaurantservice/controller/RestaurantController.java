package org.sikawofie.restaurantservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sikawofie.restaurantservice.dto.*;
import org.sikawofie.restaurantservice.enums.RestaurantStatus;
import org.sikawofie.restaurantservice.service.RestaurantService;
import org.sikawofie.restaurantservice.utils.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/restaurant/")
@RequiredArgsConstructor
@Tag(name = "Restaurant Management", description = "Endpoints for managing restaurants and menus")
@SecurityRequirement(name = "bearerAuth")
public class RestaurantController {

    private final RestaurantService service;

    private <T> ResponseEntity<org.sikawofie.restaurantservice.dto.ApiResponse<T>> buildResponse(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status)
                .body(org.sikawofie.restaurantservice.dto.ApiResponse.<T>builder()
                        .status(status.value())
                        .message(message)
                        .data(data)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @Operation(
            summary = "Create a new restaurant",
            description = "Allows restaurant owners or admins to register a new restaurant",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RestaurantRequestDto.class),
                            examples = @ExampleObject(
                                    name = "RestaurantCreateExample",
                                    value = """
                        {
                          "name": "Gourmet Paradise",
                          "description": "Fine dining experience with international cuisine",
                          "address": "789 Food Street, Culinary City",
                          "contactInfo": "contact@gourmetparadise.com",
                          "operatingHours": "10:00 AM - 10:00 PM"
                        }"""
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Restaurant created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                        {
                          "status": 201,
                          "message": "Restaurant created successfully",
                          "data": {
                            "id": 15,
                            "name": "Gourmet Paradise",
                            "status": "PENDING_APPROVAL",
                            "ownerId": 42
                          },
                          "timestamp": "2023-10-05T14:30:00"
                        }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - ADMIN or OWNER role required"
            )
    })
    public ResponseEntity<org.sikawofie.restaurantservice.dto.ApiResponse<RestaurantResponseDto>> create(
            @RequestBody @Valid RestaurantRequestDto dto
    ) {
        Long ownerId = SecurityUtils.getUserId();
        String role = SecurityUtils.getUserRole();

        RestaurantResponseDto created = service.createRestaurant(dto, ownerId, role);
        return buildResponse(HttpStatus.CREATED, "Restaurant created successfully", created);
    }

    @GetMapping
    @Operation(
            summary = "Get all restaurants",
            description = "Retrieves a list of all restaurants (active and inactive)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Restaurants retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                            value = """
                    {
                      "status": 200,
                      "message": "Restaurants retrieved",
                      "data": [
                        {
                          "id": 15,
                          "name": "Gourmet Paradise",
                          "status": "ACTIVE",
                          "address": "789 Food Street, Culinary City"
                        },
                        {
                          "id": 16,
                          "name": "Burger World",
                          "status": "ACTIVE",
                          "address": "123 Fast Food Ave"
                        }
                      ],
                      "timestamp": "2023-10-05T15:30:00"
                    }"""
                    )
            )
    )
    public ResponseEntity<org.sikawofie.restaurantservice.dto.ApiResponse<List<RestaurantResponseDto>>> getAll() {
        return buildResponse(HttpStatus.OK, "Restaurants retrieved", service.getAll());
    }

    @GetMapping("/{id}/menu")
    @Operation(
            summary = "Get restaurant menu",
            description = "Retrieves the full menu for a specific restaurant",
            parameters = @Parameter(
                    name = "id",
                    description = "ID of the restaurant",
                    example = "15",
                    in = ParameterIn.PATH
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Menu retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                        {
                          "status": 200,
                          "message": "Menu retrieved",
                          "data": [
                            {
                              "id": 101,
                              "name": "Truffle Pasta",
                              "description": "Fresh pasta with black truffle",
                              "price": 24.99,
                              "category": "Main Course"
                            },
                            {
                              "id": 102,
                              "name": "Chocolate Soufflé",
                              "description": "Warm chocolate dessert",
                              "price": 12.99,
                              "category": "Dessert"
                            }
                          ],
                          "timestamp": "2023-10-05T15:45:00"
                        }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Restaurant not found"
            )
    })
    public ResponseEntity<org.sikawofie.restaurantservice.dto.ApiResponse<List<MenuItemResponseDto>>> getMenu(@PathVariable Long id) {
        return buildResponse(HttpStatus.OK, "Menu retrieved", service.getMenu(id));
    }

    @PostMapping("/{id}/menu")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('OWNER') and @restaurantService.isOwner(#id, authentication.principal.userId))")
    @Operation(
            summary = "Add menu item",
            description = "Add a new item to a restaurant's menu (accessible to restaurant owners and admins)",
            parameters = @Parameter(
                    name = "id",
                    description = "ID of the restaurant",
                    example = "15",
                    in = ParameterIn.PATH
            ),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MenuItemRequestDto.class),
                            examples = @ExampleObject(
                                    name = "MenuItemCreateExample",
                                    value = """
                        {
                          "name": "Seafood Platter",
                          "description": "Fresh selection of local seafood",
                          "price": 34.99,
                          "category": "Appetizer",
                          "isAvailable": true
                        }"""
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Menu item added successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                        {
                          "status": 200,
                          "message": "Menu item added",
                          "data": {
                            "id": 103,
                            "name": "Seafood Platter",
                            "price": 34.99
                          },
                          "timestamp": "2023-10-05T16:00:00"
                        }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid menu item data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Not restaurant owner or admin"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Restaurant not found"
            )
    })
    public ResponseEntity<org.sikawofie.restaurantservice.dto.ApiResponse<MenuItemResponseDto>> addMenuItem(
            @PathVariable Long id,
            @RequestBody @Valid MenuItemRequestDto item
    ) {
        Long ownerId = SecurityUtils.getUserId();
        String role = SecurityUtils.getUserRole();

        MenuItemResponseDto added = service.addMenuItem(id, item, ownerId, role);
        return buildResponse(HttpStatus.OK, "Menu item added", added);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('OWNER') and @restaurantService.isOwner(#id, authentication.principal.userId))")
    @Operation(
            summary = "Update restaurant details",
            description = "Update restaurant information (accessible to owners and admins)",
            parameters = @Parameter(
                    name = "id",
                    description = "ID of the restaurant to update",
                    example = "15",
                    in = ParameterIn.PATH
            ),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                        {
                          "name": "Gourmet Paradise - Premium",
                          "description": "Enhanced fine dining experience",
                          "address": "789 Food Street, Culinary City",
                          "contactInfo": "premium@gourmetparadise.com",
                          "operatingHours": "11:00 AM - 11:00 PM"
                        }"""
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Restaurant updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                        {
                          "status": 200,
                          "message": "Restaurant updated",
                          "data": {
                            "id": 15,
                            "name": "Gourmet Paradise - Premium",
                            "status": "ACTIVE"
                          },
                          "timestamp": "2023-10-05T16:15:00"
                        }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Not restaurant owner or admin"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Restaurant not found"
            )
    })
    public ResponseEntity<org.sikawofie.restaurantservice.dto.ApiResponse<RestaurantResponseDto>> updateRestaurant(
            @PathVariable Long id,
            @RequestBody @Valid RestaurantRequestDto dto
    ) {
        Long ownerId = SecurityUtils.getUserId();
        RestaurantResponseDto updated = service.updateRestaurant(id, dto, ownerId);
        return buildResponse(HttpStatus.OK, "Restaurant updated", updated);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update restaurant status",
            description = "Change restaurant status (ADMIN only)",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of the restaurant",
                            example = "15",
                            in = ParameterIn.PATH
                    ),
                    @Parameter(
                            name = "status",
                            description = "New status for the restaurant",
                            example = "ACTIVE",
                            in = ParameterIn.QUERY
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                        {
                          "status": 200,
                          "message": "Status updated",
                          "data": {
                            "id": 15,
                            "name": "Gourmet Paradise",
                            "status": "ACTIVE"
                          },
                          "timestamp": "2023-10-05T16:20:00"
                        }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status value"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - ADMIN role required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Restaurant not found"
            )
    })
    public ResponseEntity<org.sikawofie.restaurantservice.dto.ApiResponse<RestaurantDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam RestaurantStatus status
    ) {
        RestaurantDTO updated = service.updateRestaurantStatus(id, status);
        return buildResponse(HttpStatus.OK, "Status updated", updated);
    }

    @GetMapping("/active")
    @Operation(
            summary = "Get active restaurants",
            description = "Retrieve all restaurants with ACTIVE status"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Active restaurants retrieved",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                            value = """
                    {
                      "status": 200,
                      "message": "Active restaurants retrieved",
                      "data": [
                        {
                          "id": 15,
                          "name": "Gourmet Paradise",
                          "status": "ACTIVE",
                          "address": "789 Food Street"
                        },
                        {
                          "id": 16,
                          "name": "Burger World",
                          "status": "ACTIVE",
                          "address": "123 Fast Food Ave"
                        }
                      ],
                      "timestamp": "2023-10-05T16:25:00"
                    }"""
                    )
            )
    )
    public ResponseEntity<org.sikawofie.restaurantservice.dto.ApiResponse<List<RestaurantDTO>>> getAllActive() {
        return buildResponse(HttpStatus.OK, "Active restaurants retrieved", service.getAllActiveRestaurants());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get restaurant by ID",
            description = "Retrieve detailed information for a specific restaurant",
            parameters = @Parameter(
                    name = "id",
                    description = "ID of the restaurant",
                    example = "15",
                    in = ParameterIn.PATH
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Restaurant details retrieved",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                        {
                          "status": 200,
                          "message": "Restaurant retrieved",
                          "data": {
                            "id": 15,
                            "name": "Gourmet Paradise",
                            "description": "Fine dining experience...",
                            "status": "ACTIVE",
                            "address": "789 Food Street",
                            "contactInfo": "contact@gourmet.com",
                            "operatingHours": "11:00 AM - 11:00 PM",
                            "ownerId": 42
                          },
                          "timestamp": "2023-10-05T16:30:00"
                        }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Restaurant not found"
            )
    })
    public ResponseEntity<org.sikawofie.restaurantservice.dto.ApiResponse<RestaurantDTO>> getById(@PathVariable Long id) {
        return buildResponse(HttpStatus.OK, "Restaurant retrieved", service.getRestaurantById(id));
    }

    @GetMapping("/owner")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @Operation(
            summary = "Get owner's restaurants",
            description = "Retrieve restaurants owned by the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Owner's restaurants retrieved",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                        {
                          "status": 200,
                          "message": "Owner's restaurants retrieved",
                          "data": [
                            {
                              "id": 15,
                              "name": "Gourmet Paradise",
                              "status": "ACTIVE"
                            },
                            {
                              "id": 22,
                              "name": "Seafood Haven",
                              "status": "PENDING_APPROVAL"
                            }
                          ],
                          "timestamp": "2023-10-05T16:35:00"
                        }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - ADMIN or OWNER role required"
            )
    })
    public ResponseEntity<org.sikawofie.restaurantservice.dto.ApiResponse<List<RestaurantDTO>>> getByOwner() {
        Long ownerId = SecurityUtils.getUserId();
        return buildResponse(HttpStatus.OK, "Owner's restaurants retrieved", service.getRestaurantsByOwner(ownerId));
    }

    @GetMapping("/search/name")
    @Operation(
            summary = "Search restaurants by name",
            description = "Find restaurants by name match",
            parameters = @Parameter(
                    name = "name",
                    description = "Name or partial name to search",
                    example = "Gourmet",
                    in = ParameterIn.QUERY
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "Search results retrieved",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                            value = """
                    {
                      "status": 200,
                      "message": "Search by name results",
                      "data": [
                        {
                          "id": 15,
                          "name": "Gourmet Paradise",
                          "status": "ACTIVE"
                        }
                      ],
                      "timestamp": "2023-10-05T16:40:00"
                    }"""
                    )
            )
    )
    public ResponseEntity<org.sikawofie.restaurantservice.dto.ApiResponse<List<RestaurantDTO>>> searchByName(@RequestParam String name) {
        return buildResponse(HttpStatus.OK, "Search by name results", service.searchRestaurantsByName(name));
    }

    @GetMapping("/search/address")
    @Operation(
            summary = "Search restaurants by address",
            description = "Find restaurants by address match",
            parameters = @Parameter(
                    name = "address",
                    description = "Address or partial address to search",
                    example = "Food Street",
                    in = ParameterIn.QUERY
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "Search results retrieved",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                            value = """
                    {
                      "status": 200,
                      "message": "Search by address results",
                      "data": [
                        {
                          "id": 15,
                          "name": "Gourmet Paradise",
                          "address": "789 Food Street, Culinary City"
                        },
                        {
                          "id": 18,
                          "name": "Street Food Hub",
                          "address": "456 Food Street, Downtown"
                        }
                      ],
                      "timestamp": "2023-10-05T16:45:00"
                    }"""
                    )
            )
    )
    public ResponseEntity<org.sikawofie.restaurantservice.dto.ApiResponse<List<RestaurantDTO>>> searchByAddress(@RequestParam String address) {
        return buildResponse(HttpStatus.OK, "Search by address results", service.searchRestaurantsByAddress(address));
    }
}