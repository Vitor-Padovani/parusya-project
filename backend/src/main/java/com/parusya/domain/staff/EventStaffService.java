package com.parusya.domain.staff;

import com.parusya.domain.group.GroupRepository;
import com.parusya.domain.organizer.OrganizerRepository;
import com.parusya.domain.checkin.CheckInRepository;
import com.parusya.domain.staff.dto.EventStaffDtos.*;
import com.parusya.infra.exception.BusinessException;
import com.parusya.infra.exception.BusinessException.ErrorCode;
import com.parusya.infra.security.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EventStaffService {

    private final EventStaffRepository staffRepository;
    private final GroupRepository groupRepository;
    private final OrganizerRepository organizerRepository;
    private final CheckInRepository checkInRepository;
    private final PasswordEncoder passwordEncoder;

    public EventStaffService(EventStaffRepository staffRepository,
                             GroupRepository groupRepository, OrganizerRepository organizerRepository, CheckInRepository checkInRepository,
                             PasswordEncoder passwordEncoder) {
        this.staffRepository = staffRepository;
        this.groupRepository = groupRepository;
        this.organizerRepository = organizerRepository;
        this.checkInRepository = checkInRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ─── POST /v1/staff ───────────────────────────────────────────────────────

    @Transactional
    public StaffResponse create(CreateStaffRequest request) {
        if (staffRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        UUID groupId = SecurityUtils.getGroupId();
        var group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        UUID createdById = SecurityUtils.getUserId();
        var createdBy = organizerRepository.findById(createdById).orElse(null);

        var staff = EventStaff.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .group(group)
                .createdBy(createdBy)
                .build();

        staffRepository.saveAndFlush(staff);

        return toResponse(staff);
    }

    // ─── GET /v1/staff ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<StaffResponse> listByGroup() {
        UUID groupId = SecurityUtils.getGroupId();
        return staffRepository.findAllByGroupId(groupId).stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── GET /v1/staff/:id ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public StaffResponse findById(UUID id) {
        return toResponse(findStaffInGroup(id));
    }

    // ─── DELETE /v1/staff/:id ─────────────────────────────────────────────────

    @Transactional
    public void delete(UUID id) {
        var staff = findStaffInGroup(id);
        checkInRepository.nullifyStaffReference(staff.getId());
        staffRepository.delete(staff);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    // Garante que o EventStaff existe E pertence ao grupo do token
    private EventStaff findStaffInGroup(UUID staffId) {
        UUID groupId = SecurityUtils.getGroupId();
        var staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!staff.getGroup().getId().equals(groupId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return staff;
    }

    private StaffResponse toResponse(EventStaff staff) {
        return new StaffResponse(
                staff.getId().toString(),
                staff.getName(),
                staff.getEmail(),
                staff.getGroup().getId().toString(),
                staff.getCreatedAt().toString()
        );
    }
}