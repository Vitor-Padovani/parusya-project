package com.parusya.domain.group;

import com.parusya.domain.group.dto.GroupDtos.*;
import com.parusya.domain.organizer.Organizer;
import com.parusya.domain.organizer.OrganizerRepository;
import com.parusya.domain.organizer.dto.OrganizerDtos.OrganizerResponse;
import com.parusya.infra.exception.BusinessException;
import com.parusya.infra.exception.BusinessException.ErrorCode;
import com.parusya.infra.security.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final OrganizerRepository organizerRepository;
    private final PasswordEncoder passwordEncoder;

    public GroupService(GroupRepository groupRepository,
                        OrganizerRepository organizerRepository,
                        PasswordEncoder passwordEncoder) {
        this.groupRepository = groupRepository;
        this.organizerRepository = organizerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ─── POST /v1/groups ──────────────────────────────────────────────────────

    @Transactional
    public CreateGroupResponse createGroup(CreateGroupRequest request) {
        if (organizerRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        var group = Group.builder()
                .name(request.groupName())
                .build();

        var organizer = Organizer.builder()
                .name(request.organizerName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .group(group)
                .build();

        group.getOrganizers().add(organizer);

        // Salva o grupo — o organizer é salvo em cascata
        groupRepository.save(group);

        return new CreateGroupResponse(
                group.getId().toString(),
                group.getName(),
                new OrganizerSummary(
                        organizer.getId().toString(),
                        organizer.getName(),
                        organizer.getEmail()
                )
        );
    }

    // ─── GET /v1/groups/me ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public GroupResponse getMyGroup() {
        UUID groupId = SecurityUtils.getGroupId();
        var group = findGroupById(groupId);

        return new GroupResponse(
                group.getId().toString(),
                group.getName(),
                group.getCreatedAt().toString()
        );
    }

    // ─── POST /v1/groups/me/organizers/invite ─────────────────────────────────

    @Transactional
    public OrganizerResponse inviteOrganizer(InviteOrganizerRequest request) {
        if (organizerRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.ORGANIZER_ALREADY_IN_GROUP);
        }

        UUID groupId = SecurityUtils.getGroupId();
        var group = findGroupById(groupId);

        var organizer = Organizer.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .group(group)
                .build();

        organizerRepository.saveAndFlush(organizer);

        return toOrganizerResponse(organizer);
    }

    // ─── GET /v1/groups/me/organizers ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<OrganizerResponse> listOrganizers() {
        UUID groupId = SecurityUtils.getGroupId();
        var group = findGroupById(groupId);

        return group.getOrganizers().stream()
                .map(this::toOrganizerResponse)
                .toList();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Group findGroupById(UUID id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private OrganizerResponse toOrganizerResponse(Organizer organizer) {
        return new OrganizerResponse(
                organizer.getId().toString(),
                organizer.getName(),
                organizer.getEmail(),
                organizer.getGroup().getId().toString(),
                organizer.getCreatedAt().toString()
        );
    }
}