package com.parusya.domain.participant;

import com.parusya.domain.checkin.CheckInRepository;
import com.parusya.domain.participant.dto.ParticipantDtos.ParticipantProfileResponse;
import com.parusya.domain.participant.dto.ParticipantDtos.ParticipantRankingResponse;
import com.parusya.domain.participant.dto.ParticipantDtos.RankedParticipant;
import com.parusya.infra.exception.BusinessException;
import com.parusya.infra.exception.BusinessException.ErrorCode;
import com.parusya.infra.security.SecurityUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ParticipantRankingService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final CheckInRepository checkInRepository;
    private final ParticipantRepository participantRepository;

    public ParticipantRankingService(CheckInRepository checkInRepository,
                                     ParticipantRepository participantRepository) {
        this.checkInRepository = checkInRepository;
        this.participantRepository = participantRepository;
    }

    // ─── GET /v1/participants/ranking ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public ParticipantRankingResponse getRanking(int page, int size,
                                                 String name, boolean absentOnly) {
        UUID groupId = SecurityUtils.getGroupId();
        var pageable = PageRequest.of(page, size);

        // name vazio tratado como null para não filtrar
        String nameFilter = (name != null && !name.isBlank()) ? name.trim() : null;

        var result = absentOnly
                ? getRankingAbsent(groupId, nameFilter, pageable)
                : getRankingAll(groupId, nameFilter, pageable);

        return result;
    }

    private ParticipantRankingResponse getRankingAll(UUID groupId, String name,
                                                     org.springframework.data.domain.Pageable pageable) {
        var pageResult = checkInRepository.rankParticipantsByGroup(groupId, name, pageable);

        var content = buildRankedList(pageResult.getContent(), pageable.getPageNumber(), pageable.getPageSize());

        return new ParticipantRankingResponse(
                content,
                pageResult.getTotalElements(),
                pageResult.getNumber(),
                pageResult.getSize()
        );
    }

    private ParticipantRankingResponse getRankingAbsent(UUID groupId, String name,
                                                        org.springframework.data.domain.Pageable pageable) {
        var lastEventId = checkInRepository.findLastEventIdByGroup(groupId)
                .orElse(null);

        // Se não há nenhum evento no grupo ainda, retorna vazio
        if (lastEventId == null) {
            return new ParticipantRankingResponse(List.of(), 0, pageable.getPageNumber(), pageable.getPageSize());
        }

        var pageResult = checkInRepository.rankAbsentFromLastEvent(groupId, lastEventId, name, pageable);

        var content = buildRankedList(pageResult.getContent(), pageable.getPageNumber(), pageable.getPageSize());

        return new ParticipantRankingResponse(
                content,
                pageResult.getTotalElements(),
                pageResult.getNumber(),
                pageResult.getSize()
        );
    }

    // ─── GET /v1/participants/ranking/:participantId ───────────────────────────

    @Transactional(readOnly = true)
    public ParticipantProfileResponse getProfile(UUID participantId) {
        UUID groupId = SecurityUtils.getGroupId();

        // Garante que o participant existe
        var participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        var dates = checkInRepository.findCheckInDatesByParticipantAndGroup(participantId, groupId);

        // Participant existe mas nunca foi a um evento deste grupo
        if (dates.isEmpty()) {
            return new ParticipantProfileResponse(
                    participant.getId().toString(),
                    participant.getFullName(),
                    participant.getGender().name(),
                    participant.getPhone(),
                    participant.getEmail(),
                    participant.getBirthDate().toString(),
                    0,
                    null,
                    List.of()
            );
        }

        String firstCheckIn = dates.get(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        List<String> checkInDates = dates.stream()
                .map(dt -> dt.format(DATE_FORMATTER))
                .distinct()   // mesmo dia com 2 check-ins (impossível pelo unique constraint, mas defensivo)
                .toList();

        return new ParticipantProfileResponse(
                participant.getId().toString(),
                participant.getFullName(),
                participant.getGender().name(),
                participant.getPhone(),
                participant.getEmail(),
                participant.getBirthDate().toString(),
                dates.size(),
                firstCheckIn,
                checkInDates
        );
    }

    @Transactional
    public void deleteParticipant(UUID participantId) {
        var participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 1. Remove check-ins (FK impede deleção direta do participant)
        checkInRepository.deleteAllByParticipantId(participantId);

        // 2. Remove o participant — QrCodes são deletados em cascata
        participantRepository.delete(participant);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private List<RankedParticipant> buildRankedList(List<Object[]> rows, int page, int size) {
        int baseRank = page * size;
        var result = new java.util.ArrayList<RankedParticipant>(rows.size());

        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            result.add(new RankedParticipant(
                    baseRank + i + 1,                    // posição real no ranking global
                    row[0].toString(),                   // participantId (UUID)
                    (String) row[1],                     // fullName
                    ((Number) row[2]).longValue()        // totalCheckIns
            ));
        }

        return result;
    }
}