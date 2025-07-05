package org.sikawofie.restaurantservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sikawofie.restaurantservice.security.AuthUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import reactor.util.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HeaderAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HeaderAuthFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String userIdHeader = request.getHeader("X-User-Id");
        String username = request.getHeader("X-User-Name");
        String userRolesHeader = request.getHeader("X-User-Role");
        String email = request.getHeader("X-User-Email");

        if (userIdHeader != null && !userIdHeader.isEmpty() && userRolesHeader != null && !userRolesHeader.isEmpty()) {
            try {
                Long userId = Long.parseLong(userIdHeader);

                List<SimpleGrantedAuthority> authorities = Arrays.stream(userRolesHeader.split(","))
                        .map(String::trim)
                        .filter(role -> !role.isEmpty())
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                // Store additional user details
                authentication.setDetails(new AuthUser(userId, email));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.info("Authenticated user [id={}, name={}] with roles: {}",
                        userId, username, authorities);

            } catch (NumberFormatException e) {
                logger.warn("Failed to parse 'X-User-Id' header. Received value: '{}'", userIdHeader);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid user ID format");
                return;
            } catch (Exception e) {
                logger.error("Unexpected error during authentication: {}", e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
                return;
            }
        } else {
            logger.debug("Missing required authentication headers");
        }

        filterChain.doFilter(request, response);
    }
}