package com.parusya.domain.staff.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EventStaffDtos {

    // ─── Requests ────────────────────────────────────────────────────────────

    public record CreateStaffRequest(
            @NotBlank(message = "Nome é obrigatório")
            String name,

            @NotBlank(message = "E-mail é obrigatório")
            @Email(message = "Formato de e-mail inválido")
            String email,

            @NotBlank(message = "Senha é obrigatória")
            @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
            String password
    ) {}

    // ─── Responses ───────────────────────────────────────────────────────────

    public record StaffResponse(
            String id,
            String name,
            String email,
            String groupId,
            String createdAt
    ) {}
}