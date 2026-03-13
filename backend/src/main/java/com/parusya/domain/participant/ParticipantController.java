package com.parusya.domain.participant;

import com.parusya.domain.participant.dto.ParticipantDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    // POST /v1/participants/register — público
    @PostMapping("/register")
    public ResponseEntity<RegisterParticipantResponse> register(
            @Valid @RequestBody RegisterParticipantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(participantService.register(request));
    }

    // GET /v1/participants/me — Participant autenticado
    @GetMapping("/me")
    public ResponseEntity<ParticipantResponse> getMe() {
        return ResponseEntity.ok(participantService.getMe());
    }

    // GET /v1/participants/me/qrcode — Participant autenticado
    @GetMapping("/me/qrcode")
    public ResponseEntity<QrCodeResponse> getMyQrCode() {
        return ResponseEntity.ok(participantService.getMyQrCode());
    }
}