package com.parusya.domain.staff;

import com.parusya.domain.staff.dto.EventStaffDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/staff")
public class EventStaffController {

    private final EventStaffService staffService;

    public EventStaffController(EventStaffService staffService) {
        this.staffService = staffService;
    }

    // POST /v1/staff — Organizer autenticado
    @PostMapping
    public ResponseEntity<StaffResponse> create(
            @Valid @RequestBody CreateStaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(staffService.create(request));
    }

    // GET /v1/staff — Organizer autenticado
    @GetMapping
    public ResponseEntity<List<StaffResponse>> list() {
        return ResponseEntity.ok(staffService.listByGroup());
    }

    // GET /v1/staff/:id — Organizer autenticado
    @GetMapping("/{id}")
    public ResponseEntity<StaffResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(staffService.findById(id));
    }

    // DELETE /v1/staff/:id — Organizer autenticado
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        staffService.delete(id);
        return ResponseEntity.noContent().build();
    }
}