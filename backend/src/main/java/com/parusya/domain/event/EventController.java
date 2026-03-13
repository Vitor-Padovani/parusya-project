package com.parusya.domain.event;

import com.parusya.domain.event.dto.EventDtos.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // POST /v1/events — Organizer
    @PostMapping
    public ResponseEntity<EventResponse> create(
            @Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.create(request));
    }

    // GET /v1/events — Organizer (com filtros opcionais)
    @GetMapping
    public ResponseEntity<PagedEventsResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(
                eventService.list(page, size, startDate, endDate, tags, isActive));
    }

    // GET /v1/events/active — EventStaff
    // Mapeado antes de /{id} para evitar conflito de rota
    @GetMapping("/active")
    public ResponseEntity<List<EventSummary>> listActive() {
        return ResponseEntity.ok(eventService.listActive());
    }

    // GET /v1/events/:id — Organizer
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.findById(id));
    }

    // PUT /v1/events/:id — Organizer
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request) {
        return ResponseEntity.ok(eventService.update(id, request));
    }

    // PATCH /v1/events/:id/status — Organizer
    @PatchMapping("/{id}/status")
    public ResponseEntity<EventStatusResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventStatusRequest request) {
        return ResponseEntity.ok(eventService.updateStatus(id, request));
    }

    // DELETE /v1/events/:id — Organizer
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}