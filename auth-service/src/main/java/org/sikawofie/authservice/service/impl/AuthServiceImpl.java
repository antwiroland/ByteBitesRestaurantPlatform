package org.sikawofie.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.sikawofie.authservice.dto.LoginRequest;
import org.sikawofie.authservice.dto.RegisterRequest;
import org.sikawofie.authservice.entity.User;
import org.sikawofie.authservice.enums.Role;
import org.sikawofie.authservice.repository.UserRepository;
import org.sikawofie.authservice.service.AuthService;
import org.sikawofie.authservice.utils.JwtUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private  final JwtUtils jwtUtils;

    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_CUSTOMER);

        return userRepository.save(user);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return jwtUtils.generateToken(user.getUsername());
    }

}
