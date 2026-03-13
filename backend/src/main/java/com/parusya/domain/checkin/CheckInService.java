package com.parusya.domain.checkin;

import com.parusya.domain.checkin.dto.CheckInDtos.*;
import com.parusya.domain.event.EventRepository;
import com.parusya.domain.participant.QrCodeRepository;
import com.parusya.domain.staff.EventStaffRepository;
import com.parusya.infra.exception.BusinessException;
import com.parusya.infra.exception.BusinessException.ErrorCode;
import com.parusya.infra.security.SecurityUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final EventRepository eventRepository;
    private final QrCodeRepository qrCodeRepository;
    private final EventStaffRepository staffRepository;

    public CheckInService(CheckInRepository checkInRepository,
                          EventRepository eventRepository,
                          QrCodeRepository qrCodeRepository,
                          EventStaffRepository staffRepository) {
        this.checkInRepository = checkInRepository;
        this.eventRepository = eventRepository;
        this.qrCodeRepository = qrCodeRepository;
        this.staffRepository = staffRepository;
    }

    // ─── POST /v1/checkins/scan ───────────────────────────────────────────────

    @Transactional
    public CheckInResponse scan(ScanRequest request) {
        UUID groupId = SecurityUtils.getGroupId();
        UUID staffId = SecurityUtils.getUserId();

        // 1. Valida que o evento existe, pertence ao grupo e está ativo
        var event = eventRepository
                .findByIdAndGroupId(UUID.fromString(request.eventId()), groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        if (!event.isActive()) {
            throw new BusinessException(ErrorCode.EVENT_INACTIVE);
        }

        // 2. Valida o QR Code e resolve o Participant
        var qrCode = qrCodeRepository
                .findByEncodedData(request.encodedData())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_QR_CODE));

        var participant = qrCode.getParticipant();

        // 3. Verifica duplicidade (check rápido antes de tentar persistir)
        if (checkInRepository.existsByParticipantIdAndEventId(participant.getId(), event.getId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_CHECKIN);
        }

        // 4. Resolve o EventStaff para o log
        var staff = staffRepository.findById(staffId).orElse(null);

        // 5. Registra o check-in
        var checkIn = CheckIn.builder()
                .participant(participant)
                .event(event)
                .staff(staff)
                .build();

        checkInRepository.saveAndFlush(checkIn);

        return new CheckInResponse(
                checkIn.getId().toString(),
                new ParticipantSummary(participant.getId().toString(), participant.getFullName()),
                new EventSummary(event.getId().toString(), event.getName()),
                checkIn.getTimestamp().toString()
        );
    }

    // ─── GET /v1/checkins/event/:eventId ──────────────────────────────────────

    @Transactional(readOnly = true)
    public PagedCheckInLog getEventLog(UUID eventId,
                                       int page, int size,
                                       LocalDateTime startTime,
                                       LocalDateTime endTime,
                                       UUID staffId,
                                       String name) {
        UUID groupId = SecurityUtils.getGroupId();

        // Garante que o evento pertence ao grupo antes de retornar dados
        eventRepository.findByIdAndGroupId(eventId, groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        var pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        var result = checkInRepository.findEventLog(
                eventId, startTime, endTime, staffId, name,
                PageRequest.of(page, size)
        );

        return new PagedCheckInLog(
                result.getContent().stream().map(this::toLogEntry).toList(),
                result.getTotalElements(),
                result.getNumber(),
                result.getSize()
        );
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private CheckInLogEntry toLogEntry(CheckIn checkIn) {
        var participant = checkIn.getParticipant();
        var staff = checkIn.getStaff();

        return new CheckInLogEntry(
                checkIn.getId().toString(),
                new ParticipantSummary(participant.getId().toString(), participant.getFullName()),
                staff != null
                        ? new StaffSummary(staff.getId().toString(), staff.getName())
                        : null,
                checkIn.getTimestamp().toString()
        );
    }
}