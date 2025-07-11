package org.sikawofie.restaurantservice.utils;

import org.sikawofie.restaurantservice.security.AuthUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SecurityUtils {

    public Long getUserId() {
        AuthUser authUser = getAuthUser();
        return authUser != null ? authUser.getUserId() : null;
    }

    public String getUsername() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null) {
            Object principal = context.getAuthentication().getPrincipal();
            if (principal instanceof String) {
                return (String) principal;
            }
        }
        return null;
    }

    public String getUserEmail() {
        AuthUser authUser = getAuthUser();
        return authUser != null ? authUser.getEmail() : null;
    }

    public String getUserRole() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null) {
            List<GrantedAuthority> authorities = (List<GrantedAuthority>) context.getAuthentication().getAuthorities();
            if (!authorities.isEmpty()) {
                String role = authorities.get(0).getAuthority();
                return role.startsWith("ROLE_") ? role.substring(5) : role;
            }
        }
        return null;
    }

    public List<String> getUserRoles() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null) {
            return context.getAuthentication().getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private AuthUser getAuthUser() {
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
