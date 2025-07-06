package org.sikawofie.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sikawofie.authservice.dto.LoginRequest;
import org.sikawofie.authservice.dto.RegisterRequest;
import org.sikawofie.authservice.entity.User;
import org.sikawofie.authservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with the provided details",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    name = "RegisterRequestExample",
                                    value = "{\"email\": \"user@example.com\", \"password\": \"SecureP@ss1\", \"role\": \"USER\"}"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"id\": 1, \"email\": \"user@example.com\", \"role\": \"USER\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"error\": \"Email must be valid\", \"field\": \"email\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"error\": \"Email already registered\"}")
                    )
            )
    })
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole()
        ));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Authenticates user credentials and returns a JWT token",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "LoginRequestExample",
                                    value = "{\"email\": \"user@example.com\", \"password\": \"SecureP@ss1\"}"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"error\": \"Email must be valid\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"error\": \"Invalid email or password\"}")
                    )
            )
    })
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(Map.of("token", token));
    }
}