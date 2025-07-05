package org.sikawofie.orderservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

        String userId = request.getHeader("X-User-Id");
        String userRolesHeader = request.getHeader("X-User-Role");
        String email = request.getHeader("X-User-Email");

        if (userId != null && !userId.isEmpty() && userRolesHeader != null && !userRolesHeader.isEmpty()) {
            try {
                Long parsedUserId = Long.parseLong(userId);

                List<SimpleGrantedAuthority> authorities = Arrays.stream(userRolesHeader.split(","))
                        .map(String::trim)
                        .filter(role -> !role.isEmpty())
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role) // Ensure role prefix
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(parsedUserId, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.info("Authenticated user [id={}, email={}] with roles: {}", parsedUserId, email != null ? email : "N/A", authorities);

            } catch (NumberFormatException e) {
                logger.warn("Failed to parse 'X-User-Id' header. Received invalid numeric value: '{}'", userId);
            } catch (Exception e) {
                logger.error("Unexpected error during header-based authentication: {}", e.getMessage(), e);
            }
        } else {
            logger.debug("Missing required authentication headers: 'X-User-Id' or 'X-User-Role'. Skipping authentication context setup.");
        }

        filterChain.doFilter(request, response);
    }
}