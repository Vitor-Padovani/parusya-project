package com.parusya.domain.stats;

import com.parusya.domain.checkin.CheckInRepository;
import com.parusya.domain.checkin.dto.CheckInDtos.*;
import com.parusya.domain.event.Event;
import com.parusya.domain.event.EventRepository;
import com.parusya.domain.event.EventSpecification;
import com.parusya.domain.tag.Tag;
import com.parusya.infra.exception.BusinessException;
import com.parusya.infra.exception.BusinessException.ErrorCode;
import com.parusya.infra.security.SecurityUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class StatsService {

    private final EventRepository eventRepository;
    private final CheckInRepository checkInRepository;

    public StatsService(EventRepository eventRepository,
                        CheckInRepository checkInRepository) {
        this.eventRepository = eventRepository;
        this.checkInRepository = checkInRepository;
    }

    // ─── GET /v1/stats/events/:eventId ────────────────────────────────────────

    @Transactional(readOnly = true)
    public EventStatsResponse getEventStats(UUID eventId) {
        UUID groupId = SecurityUtils.getGroupId();

        var event = eventRepository.findByIdAndGroupId(eventId, groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        long totalCheckIns = checkInRepository.countByEventId(eventId);
        var hourly = buildHourlyDistribution(eventId);
        var staffBreakdown = buildStaffBreakdown(eventId);

        return new EventStatsResponse(
                event.getId().toString(),
                event.getName(),
                totalCheckIns,
                hourly,
                staffBreakdown
        );
    }

    // ─── GET /v1/stats/events ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AggregatedStatsResponse getAggregatedStats(LocalDateTime startDate,
                                                      LocalDateTime endDate,
                                                      List<String> tags,
                                                      Integer limit) {
        UUID groupId = SecurityUtils.getGroupId();

        var spec = EventSpecification.filter(groupId, startDate, endDate, tags, null);

        // Ordena por data DESC e aplica limit se fornecido
        var sort = Sort.by(Sort.Direction.DESC, "startDateTime");
        List<Event> events = limit != null
                ? eventRepository.findAll(spec, PageRequest.of(0, limit, sort)).getContent()
                : eventRepository.findAll(spec, sort);

        if (events.isEmpty()) {
            return new AggregatedStatsResponse(0, 0, List.of());
        }

        var eventIds = events.stream().map(Event::getId).toList();

        var allCheckIns = checkInRepository.findAllByEventIdIn(eventIds);

        var checkInCountByEvent = allCheckIns.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        c -> c.getEvent().getId(),
                        java.util.stream.Collectors.counting()
                ));

        long totalCheckIns = allCheckIns.size();

        // Inverte para ordem cronológica — o gráfico lê da esquerda (mais antigo) para direita (mais recente)
        var eventSummaries = events.reversed().stream()
                .map(event -> new EventCheckInSummary(
                        event.getId().toString(),
                        event.getName(),
                        checkInCountByEvent.getOrDefault(event.getId(), 0L),
                        event.getTags().stream().map(Tag::getName).toList()
                ))
                .toList();

        return new AggregatedStatsResponse(events.size(), totalCheckIns, eventSummaries);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private List<HourlyCount> buildHourlyDistribution(UUID eventId) {
        return checkInRepository.countByIntervalForEvent(eventId).stream()
                .map(row -> {
                    var timestamp = ((java.sql.Timestamp) row[0]).toLocalDateTime();
                    long count = ((Number) row[1]).longValue();
                    return new HourlyCount(
                            timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                            count
                    );
                })
                .toList();
    }

    private List<StaffCount> buildStaffBreakdown(UUID eventId) {
        return checkInRepository.countByStaffForEvent(eventId).stream()
                .map(row -> new StaffCount(
                        row[0].toString(),
                        (String) row[1],
                        (Long) row[2]
                ))
                .toList();
    }
}