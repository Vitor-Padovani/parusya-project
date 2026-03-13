package com.parusya.domain.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CheckInDtos {

    // ─── Requests ────────────────────────────────────────────────────────────

    public record ScanRequest(
            @NotNull(message = "ID do evento é obrigatório")
            String eventId,

            @NotBlank(message = "Conteúdo do QR Code é obrigatório")
            String encodedData
    ) {}

    // ─── Responses ───────────────────────────────────────────────────────────

    public record CheckInResponse(
            String checkInId,
            ParticipantSummary participant,
            EventSummary event,
            String timestamp
    ) {}

    public record CheckInLogEntry(
            String checkInId,
            ParticipantSummary participant,
            StaffSummary staff,
            String timestamp
    ) {}

    public record PagedCheckInLog(
            List<CheckInLogEntry> content,
            long totalElements,
            int page,
            int size
    ) {}

    public record ParticipantSummary(
            String id,
            String fullName
    ) {}

    public record EventSummary(
            String id,
            String name
    ) {}

    public record StaffSummary(
            String id,
            String name
    ) {}

    // ─── Estatísticas ────────────────────────────────────────────────────────

    public record EventStatsResponse(
            String eventId,
            String eventName,
            long totalCheckIns,
            List<HourlyCount> hourlyDistribution,
            List<StaffCount> staffBreakdown
    ) {}

    public record AggregatedStatsResponse(
            int eventsAnalyzed,
            long totalCheckIns,
            List<EventCheckInSummary> events
    ) {}

    public record HourlyCount(
            String hour,
            long count
    ) {}

    public record StaffCount(
            String staffId,
            String staffName,
            long checkIns
    ) {}

    public record EventCheckInSummary(
            String id,
            String name,
            long checkIns,
            List<String> tags
    ) {}
}