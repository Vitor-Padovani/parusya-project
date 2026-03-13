package com.parusya.domain.participant.dto;

import com.parusya.domain.participant.Gender;
import jakarta.validation.constraints.*;

import java.util.List;

public class ParticipantDtos {

    // ─── Requests ────────────────────────────────────────────────────────────

    public record RegisterParticipantRequest(
            @NotBlank(message = "Nome completo é obrigatório")
            @Size(min = 3, message = "Nome deve ter no mínimo 3 caracteres")
            String fullName,

            @NotNull(message = "Sexo é obrigatório")
            Gender gender,

            @NotBlank(message = "Celular é obrigatório")
            @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Celular deve estar no formato E.164 (ex: +5511999999999)")
            String phone,

            @NotBlank(message = "E-mail é obrigatório")
            @Email(message = "Formato de e-mail inválido")
            String email,

            @NotNull(message = "Data de nascimento é obrigatória")
            @Past(message = "Data de nascimento deve ser no passado")
            java.time.LocalDate birthDate,

            @NotBlank(message = "Senha é obrigatória")
            @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
            String password
    ) {}

    // ─── Responses ───────────────────────────────────────────────────────────

    public record ParticipantResponse(
            String id,
            String fullName,
            String email,
            String phone,
            String gender,
            String birthDate,
            String createdAt
    ) {}

    public record RegisterParticipantResponse(
            String id,
            String fullName,
            String email,
            QrCodeSummary qrCode
    ) {}

    public record QrCodeSummary(
            String id,
            String encodedData,
            String createdAt
    ) {}

    public record QrCodeResponse(
            String id,
            String encodedData,
            String createdAt,
            String imageBase64
    ) {}

    // ─── Ranking de Participants (Organizer) ─────────────────────────────────

    public record RankedParticipant(
            int rank,
            String participantId,
            String fullName,
            long totalCheckIns
    ) {}

    public record ParticipantRankingResponse(
            List<RankedParticipant> content,
            long totalElements,
            int page,
            int size
    ) {}

    public record ParticipantProfileResponse(
            String participantId,
            String fullName,
            String gender,
            String phone,
            String email,
            String birthDate,
            long totalCheckIns,
            String firstCheckIn,
            List<String> checkInDates
    ) {}
}