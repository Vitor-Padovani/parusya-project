package com.parusya.domain.event.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class EventDtos {

    // ─── Requests ────────────────────────────────────────────────────────────

    public record CreateEventRequest(
            @NotBlank(message = "Nome do evento é obrigatório")
            String name,

            String description,

            @NotNull(message = "Data e hora de início são obrigatórias")
            LocalDateTime startDateTime,

            List<String> tags
    ) {}

    public record UpdateEventRequest(
            @NotBlank(message = "Nome do evento é obrigatório")
            String name,

            String description,

            @NotNull(message = "Data e hora de início são obrigatórias")
            LocalDateTime startDateTime,

            List<String> tags
    ) {}

    public record UpdateEventStatusRequest(
            @NotNull(message = "Status é obrigatório")
            Boolean isActive
    ) {}

    // ─── Responses ───────────────────────────────────────────────────────────

    public record EventResponse(
            String id,
            String name,
            String description,
            String startDateTime,
            boolean isActive,
            List<String> tags,
            String groupId,
            String createdAt
    ) {}

    public record EventSummary(
            String id,
            String name,
            boolean isActive,
            List<String> tags,
            String startDateTime
    ) {}

    public record EventStatusResponse(
            String id,
            boolean isActive
    ) {}

    public record PagedEventsResponse(
            List<EventSummary> content,
            long totalElements,
            int page,
            int size
    ) {}
}