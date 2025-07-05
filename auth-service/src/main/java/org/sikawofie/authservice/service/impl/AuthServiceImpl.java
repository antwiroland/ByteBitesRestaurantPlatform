package org.sikawofie.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.sikawofie.authservice.dto.LoginRequest;
import org.sikawofie.authservice.dto.RegisterRequest;
import org.sikawofie.authservice.entity.User;
import org.sikawofie.authservice.enums.Role;
import org.sikawofie.authservice.repository.UserRepository;
import org.sikawofie.authservice.service.AuthService;
import org.sikawofie.authservice.utils.JwtUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DataIntegrityViolationException("Email already registered");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DataIntegrityViolationException("Username already taken");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_CUSTOMER);

        return userRepository.save(user);
    }

    @Override
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return jwtUtils.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getEmail()
        );
    }
}