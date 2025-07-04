package org.sikawofie.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 3)
    private String username;
    @NotBlank @Email
    private String email;
    @NotBlank
    @Size(min = 6)
    private String password;
}