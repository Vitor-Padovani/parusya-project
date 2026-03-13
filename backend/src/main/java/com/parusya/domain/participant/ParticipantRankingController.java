package com.parusya.domain.participant;

import com.parusya.domain.participant.dto.ParticipantDtos.ParticipantProfileResponse;
import com.parusya.domain.participant.dto.ParticipantDtos.ParticipantRankingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/participants/ranking")
public class ParticipantRankingController {

    private final ParticipantRankingService rankingService;

    public ParticipantRankingController(ParticipantRankingService rankingService) {
        this.rankingService = rankingService;
    }

    // GET /v1/participants/ranking — Organizer
    @GetMapping
    public ResponseEntity<ParticipantRankingResponse> getRanking(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false)    String name,
            @RequestParam(defaultValue = "false") boolean absentOnly) {
        return ResponseEntity.ok(rankingService.getRanking(page, size, name, absentOnly));
    }

    // GET /v1/participants/ranking/:participantId — Organizer
    @GetMapping("/{participantId}")
    public ResponseEntity<ParticipantProfileResponse> getProfile(
            @PathVariable UUID participantId) {
        return ResponseEntity.ok(rankingService.getProfile(participantId));
    }

    // DELETE /v1/participants/ranking/:participantId — Organizer
    @DeleteMapping("/{participantId}")
    public ResponseEntity<Void> deleteParticipant(@PathVariable UUID participantId) {
        rankingService.deleteParticipant(participantId);
        return ResponseEntity.noContent().build();
    }
}