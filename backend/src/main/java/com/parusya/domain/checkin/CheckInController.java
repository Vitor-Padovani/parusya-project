package com.parusya.domain.checkin;

import com.parusya.domain.checkin.dto.CheckInDtos.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/v1/checkins")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    // POST /v1/checkins/scan — EventStaff
    @PostMapping("/scan")
    public ResponseEntity<CheckInResponse> scan(
            @Valid @RequestBody ScanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(checkInService.scan(request));
    }

    // GET /v1/checkins/event/:eventId — Organizer
    @GetMapping("/event/{eventId}")
    public ResponseEntity<PagedCheckInLog> getEventLog(
            @PathVariable UUID eventId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) UUID staffId,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(
                checkInService.getEventLog(eventId, page, size, startTime, endTime, staffId, name));
    }
}