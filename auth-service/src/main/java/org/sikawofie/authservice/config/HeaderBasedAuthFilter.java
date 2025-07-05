package org.sikawofie.authservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import reactor.util.annotation.NonNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class HeaderBasedAuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(HeaderBasedAuthFilter.class);

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String userEmail = request.getHeader("X-User-Email");
        String userRoleHeader = request.getHeader("X-User-Role");

        if (userId != null && !userId.isEmpty() && userEmail != null && !userEmail.isEmpty()) {
            try {
                Long parsedUserId = Long.parseLong(userId);

                List<SimpleGrantedAuthority> authorities = Collections.emptyList();
                if (userRoleHeader != null && !userRoleHeader.isEmpty()) {
                    authorities = Collections.singletonList(new SimpleGrantedAuthority(userRoleHeader));
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(parsedUserId, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.info("Authenticated user [id={}, email={}] with role [{}]", parsedUserId, userEmail, userRoleHeader != null ? userRoleHeader : "N/A");

            } catch (NumberFormatException e) {
                logger.warn("Invalid format for header 'X-User-Id': '{}'. Expected a numeric value.", userId);
            } catch (Exception e) {
                logger.error("Failed to authenticate user from headers. Reason: {}", e.getMessage(), e);
            }
        } else {
            logger.debug("Missing or empty authentication headers. Skipping security context setup for this request.");
        }

        filterChain.doFilter(request, response);
    }
}