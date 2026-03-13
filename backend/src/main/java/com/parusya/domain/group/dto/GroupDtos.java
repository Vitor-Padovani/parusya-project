package com.parusya.domain.group.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GroupDtos {

    // ─── Requests ────────────────────────────────────────────────────────────

    public record CreateGroupRequest(
            @NotBlank(message = "Nome do grupo é obrigatório")
            String groupName,

            @NotBlank(message = "Nome do organizador é obrigatório")
            String organizerName,

            @NotBlank(message = "E-mail é obrigatório")
            @Email(message = "Formato de e-mail inválido")
            String email,

            @NotBlank(message = "Senha é obrigatória")
            @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
            String password
    ) {}

    public record InviteOrganizerRequest(
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

    public record GroupResponse(
            String id,
            String name,
            String createdAt
    ) {}

    public record CreateGroupResponse(
            String groupId,
            String groupName,
            OrganizerSummary organizer
    ) {}

    public record OrganizerSummary(
            String id,
            String name,
            String email
    ) {}
}