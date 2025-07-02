package org.sikawofie.apiservice.config;

import lombok.RequiredArgsConstructor;
import org.sikawofie.apiservice.filters.JwtAuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RouteConfig {
    final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth/**", r -> r
                        .path("/auth/**")
                        .uri("lb://auth-service"))

                .route("restaurant-service", r -> r
                        .path("/api/restaurant/**")
                        .filters(f -> f.filters(jwtAuthenticationFilter))
                        .uri("lb://restaurant-service"))

                .route("order-service", r -> r
                        .path("/api/order/**")
                        .filters(f -> f.filters(jwtAuthenticationFilter))
                        .uri("lb://order-service"))

                .route("notification-service", r -> r
                        .path("/api/notification/**")
                        .filters(f -> f.filters(jwtAuthenticationFilter))
                        .uri("lb://notification-service"))

                .build();
    }
}
