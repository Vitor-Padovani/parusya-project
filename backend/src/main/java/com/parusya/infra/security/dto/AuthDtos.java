package com.parusya.infra.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {

    public record LoginRequest(
            @NotBlank(message = "E-mail é obrigatório")
            @Email(message = "Formato de e-mail inválido")
            String email,

            @NotBlank(message = "Senha é obrigatória")
            String password
    ) {}

    public record LoginResponse(
            String token,
            long expiresIn,
            UserSummary user
    ) {}

    public record UserSummary(
            String id,
            String name,
            String email,
            String role
    ) {}
}