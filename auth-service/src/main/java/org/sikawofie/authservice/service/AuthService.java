package org.sikawofie.authservice.service;

import org.sikawofie.authservice.dto.LoginRequest;
import org.sikawofie.authservice.dto.RegisterRequest;
import org.sikawofie.authservice.entity.User;

public interface AuthService {
    User register(RegisterRequest request);
    String login(LoginRequest request);
}
