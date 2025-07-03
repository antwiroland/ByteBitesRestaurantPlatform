package org.sikawofie.restaurantservice.utils;

import org.springframework.http.server.ServerHttpRequest;

public class SecurityUtils {
    public static Long getCurrentUserId(ServerHttpRequest request) {
        return Long.valueOf(request.getHeaders().getFirst("X-User-Id"));
    }

    public static String getCurrentUserRole(ServerHttpRequest request) {
        return request.getHeaders().getFirst("X-User-Role");
    }
}
