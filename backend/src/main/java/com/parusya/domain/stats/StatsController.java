package com.parusya.domain.stats;

import com.parusya.domain.checkin.dto.CheckInDtos.AggregatedStatsResponse;
import com.parusya.domain.checkin.dto.CheckInDtos.EventStatsResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    // GET /v1/stats/events/:eventId — Organizer
    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventStatsResponse> getEventStats(
            @PathVariable UUID eventId) {
        return ResponseEntity.ok(statsService.getEventStats(eventId));
    }

    // GET /v1/stats/events — Organizer (com filtros opcionais)
    @GetMapping("/events")
    public ResponseEntity<AggregatedStatsResponse> getAggregatedStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(
                statsService.getAggregatedStats(startDate, endDate, tags, limit));
    }
}