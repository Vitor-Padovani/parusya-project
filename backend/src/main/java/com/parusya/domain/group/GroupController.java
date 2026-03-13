package com.parusya.domain.group;

import com.parusya.domain.group.dto.GroupDtos.*;
import com.parusya.domain.organizer.dto.OrganizerDtos.OrganizerResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    // POST /v1/groups — público (onboarding)
    @PostMapping("/groups")
    public ResponseEntity<CreateGroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(request));
    }

    // GET /v1/groups/me — Organizer autenticado
    @GetMapping("/groups/me")
    public ResponseEntity<GroupResponse> getMyGroup() {
        return ResponseEntity.ok(groupService.getMyGroup());
    }

    // POST /v1/groups/me/organizers/invite — Organizer autenticado
    @PostMapping("/groups/me/organizers/invite")
    public ResponseEntity<OrganizerResponse> inviteOrganizer(
            @Valid @RequestBody InviteOrganizerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.inviteOrganizer(request));
    }

    // GET /v1/groups/me/organizers — Organizer autenticado
    @GetMapping("/groups/me/organizers")
    public ResponseEntity<List<OrganizerResponse>> listOrganizers() {
        return ResponseEntity.ok(groupService.listOrganizers());
    }
}