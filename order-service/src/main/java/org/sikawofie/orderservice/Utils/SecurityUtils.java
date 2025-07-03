package org.sikawofie.orderservice.Utils;

import org.springframework.http.server.ServerHttpRequest;

import java.util.Objects;

public class SecurityUtils {
    public static Long getUserId(ServerHttpRequest request) {
        return Long.parseLong(Objects.requireNonNull(request.getHeaders().getFirst("X-User-Id")));
    }

    public static String getUserRole(ServerHttpRequest request) {
        return request.getHeaders().getFirst("X-User-Role");
    }
}
