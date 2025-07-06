package org.sikawofie.orderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sikawofie.orderservice.Utils.SecurityUtils;
import org.sikawofie.orderservice.dto.OrderRequestDto;
import org.sikawofie.orderservice.dto.OrderResponseDto;
import org.sikawofie.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/order/")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Endpoints for managing food orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(
            summary = "Place a new order",
            description = "Allows authenticated customers to place food orders",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OrderRequestDto.class),
                            examples = @ExampleObject(
                                    name = "OrderRequestExample",
                                    value = """
                        {
                          "restaurantId": 5,
                          "items": [
                            {
                              "menuItemId": 101,
                              "quantity": 2,
                              "specialInstructions": "No onions please"
                            },
                            {
                              "menuItemId": 205,
                              "quantity": 1
                            }
                          ],
                          "deliveryAddress": "123 Main St, Cityville"
                        }"""
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Order created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                        {
                          "status": 201,
                          "message": "Order placed successfully",
                          "data": {
                            "orderId": 789,
                            "customerId": 123,
                            "restaurantId": 5,
                            "status": "PENDING",
                            "totalAmount": 35.99,
                            "items": [
                              {
                                "name": "Chicken Burger",
                                "quantity": 2,
                                "price": 12.99
                              },
                              {
                                "name": "French Fries",
                                "quantity": 1,
                                "price": 4.99
                              }
                            ]
                          },
                          "timestamp": "2023-10-05T14:30:00"
                        }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid order data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                        {
                          "status": 400,
                          "message": "Validation error: items must not be empty",
                          "timestamp": "2023-10-05T14:31:22"
                        }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Customer role required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Restaurant or menu item not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                        {
                          "status": 404,
                          "message": "Restaurant not found",
                          "timestamp": "2023-10-05T14:32:15"
                        }"""
                            )
                    )
            )
    })
    public ResponseEntity<org.sikawofie.orderservice.dto.ApiResponse<OrderResponseDto>> placeOrder(
            @RequestBody @Valid OrderRequestDto request) {

        Long customerId = SecurityUtils.getUserId();
        String role = SecurityUtils.getUserRole();

        OrderResponseDto order = orderService.placeOrder(request, customerId, role);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(org.sikawofie.orderservice.dto.ApiResponse.<OrderResponseDto>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Order placed successfully")
                        .data(order)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @GetMapping
    @Operation(
            summary = "Get customer orders",
            description = "Retrieve all orders for the authenticated customer"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                        {
                          "status": 200,
                          "message": "Customer orders retrieved",
                          "data": [
                            {
                              "orderId": 789,
                              "restaurantName": "Burger Palace",
                              "status": "DELIVERED",
                              "totalAmount": 35.99,
                              "orderDate": "2023-10-01T12:30:00"
                            },
                            {
                              "orderId": 790,
                              "restaurantName": "Pizza World",
                              "status": "PREPARING",
                              "totalAmount": 24.50,
                              "orderDate": "2023-10-05T14:30:00"
                            }
                          ],
                          "timestamp": "2023-10-05T15:30:00"
                        }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Customer role required"
            )
    })
    public ResponseEntity<org.sikawofie.orderservice.dto.ApiResponse<List<OrderResponseDto>>> customerOrders() {
        Long customerId = SecurityUtils.getUserId();
        List<OrderResponseDto> orders = orderService.getOrdersByCustomer(customerId);

        return ResponseEntity.ok(org.sikawofie.orderservice.dto.ApiResponse.<List<OrderResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Customer orders retrieved")
                .data(orders)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/restaurant/{id}")
    @Operation(
            summary = "Get restaurant orders",
            description = "Retrieve orders for a specific restaurant (accessible to restaurant owners and admins)",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of the restaurant",
                            example = "5",
                            required = true
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                        {
                          "status": 200,
                          "message": "Orders for restaurant retrieved",
                          "data": [
                            {
                              "orderId": 789,
                              "customerName": "John Doe",
                              "status": "PREPARING",
                              "totalAmount": 35.99,
                              "orderDate": "2023-10-05T14:30:00",
                              "deliveryAddress": "123 Main St, Cityville"
                            },
                            {
                              "orderId": 791,
                              "customerName": "Jane Smith",
                              "status": "PENDING",
                              "totalAmount": 18.75,
                              "orderDate": "2023-10-05T15:15:00",
                              "deliveryAddress": "456 Oak Ave, Townsville"
                            }
                          ],
                          "timestamp": "2023-10-05T15:30:00"
                        }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Restaurant owner or admin role required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Restaurant not found"
            )
    })
    public ResponseEntity<org.sikawofie.orderservice.dto.ApiResponse<List<OrderResponseDto>>> ordersForRestaurant(
            @PathVariable Long id) {

        Long userId = SecurityUtils.getUserId();
        String role = SecurityUtils.getUserRole();

        List<OrderResponseDto> orders = orderService.getOrdersByRestaurant(id, userId, role);

        return ResponseEntity.ok(org.sikawofie.orderservice.dto.ApiResponse.<List<OrderResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Orders for restaurant retrieved")
                .data(orders)
                .timestamp(LocalDateTime.now())
                .build());
    }
}