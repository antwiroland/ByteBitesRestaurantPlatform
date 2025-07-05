package org.sikawofie.orderservice.Utils;

import org.sikawofie.orderservice.security.AuthUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SecurityUtils {

    public static Long getUserId() {
        return getAuthUser() != null ? getAuthUser().getUserId() : null;
    }

    public static String getUsername() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null) {
            Object principal = context.getAuthentication().getPrincipal();
            if (principal instanceof String) {
                return (String) principal;
            }
        }
        return null;
    }

    public static String getUserEmail() {
        return getAuthUser() != null ? getAuthUser().getEmail() : null;
    }

    // New method to get user role
    public static String getUserRole() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null) {
            List<GrantedAuthority> authorities = (List<GrantedAuthority>) context.getAuthentication().getAuthorities();
            if (!authorities.isEmpty()) {
                // Return the first role (without ROLE_ prefix)
                String role = authorities.get(0).getAuthority();
                return role.startsWith("ROLE_") ? role.substring(5) : role;
            }
        }
        return null;
    }

    public static List<String> getUserRoles() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null) {
            return context.getAuthentication().getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static AuthUser getAuthUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null) {
            Object details = context.getAuthentication().getDetails();
            if (details instanceof AuthUser) {
                return (AuthUser) details;
            }
        }
        return null;
    }
}