package com.parusya.domain.event;

import com.parusya.domain.checkin.CheckInRepository;
import com.parusya.domain.event.dto.EventDtos.*;
import com.parusya.domain.group.GroupRepository;
import com.parusya.domain.tag.Tag;
import com.parusya.domain.tag.TagRepository;
import com.parusya.infra.exception.BusinessException;
import com.parusya.infra.exception.BusinessException.ErrorCode;
import com.parusya.infra.security.SecurityUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final GroupRepository groupRepository;
    private final TagRepository tagRepository;
    private final CheckInRepository checkInRepository;

    public EventService(EventRepository eventRepository,
                        GroupRepository groupRepository,
                        TagRepository tagRepository,
                        CheckInRepository checkInRepository) {
        this.eventRepository = eventRepository;
        this.groupRepository = groupRepository;
        this.tagRepository = tagRepository;
        this.checkInRepository = checkInRepository;
    }

    // ─── POST /v1/events ──────────────────────────────────────────────────────

    @Transactional
    public EventResponse create(CreateEventRequest request) {
        UUID groupId = SecurityUtils.getGroupId();
        var group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        var tags = resolveTags(request.tags(), groupId);

        var event = Event.builder()
                .name(request.name())
                .description(request.description())
                .startDateTime(request.startDateTime())
                .isActive(false)
                .group(group)
                .tags(tags)
                .build();

        eventRepository.saveAndFlush(event);

        return toResponse(event);
    }

    // ─── GET /v1/events ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PagedEventsResponse list(int page, int size,
                                    LocalDateTime startDate, LocalDateTime endDate,
                                    List<String> tags, Boolean isActive) {
        UUID groupId = SecurityUtils.getGroupId();
        var pageable = PageRequest.of(page, size, Sort.by("startDateTime").descending());
        var spec = EventSpecification.filter(groupId, startDate, endDate, tags, isActive);
        var result = eventRepository.findAll(spec, pageable);

        return new PagedEventsResponse(
                result.getContent().stream().map(this::toSummary).toList(),
                result.getTotalElements(),
                result.getNumber(),
                result.getSize()
        );
    }

    // ─── GET /v1/events/active ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EventSummary> listActive() {
        UUID groupId = SecurityUtils.getGroupId();
        return eventRepository.findAllByGroupIdAndIsActiveTrue(groupId).stream()
                .map(this::toSummary)
                .toList();
    }

    // ─── GET /v1/events/:id ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public EventResponse findById(UUID id) {
        return toResponse(findEventInGroup(id));
    }

    // ─── PUT /v1/events/:id ───────────────────────────────────────────────────

    @Transactional
    public EventResponse update(UUID id, UpdateEventRequest request) {
        UUID groupId = SecurityUtils.getGroupId();
        var event = findEventInGroup(id);
        var tags = resolveTags(request.tags(), groupId);

        event.setName(request.name());
        event.setDescription(request.description());
        event.setStartDateTime(request.startDateTime());
        event.setTags(tags);

        return toResponse(eventRepository.save(event));
    }

    // ─── PATCH /v1/events/:id/status ─────────────────────────────────────────

    @Transactional
    public EventStatusResponse updateStatus(UUID id, UpdateEventStatusRequest request) {
        var event = findEventInGroup(id);
        event.setActive(request.isActive());
        eventRepository.save(event);
        return new EventStatusResponse(event.getId().toString(), event.isActive());
    }

    // ─── DELETE /v1/events/:id ────────────────────────────────────────────────

    @Transactional
    public void delete(UUID id) {
        var event = findEventInGroup(id);

        if (checkInRepository.countByEventId(event.getId()) > 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN,
                    "Não é possível remover um evento que já possui check-ins registrados");
        }

        eventRepository.delete(event);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Event findEventInGroup(UUID eventId) {
        UUID groupId = SecurityUtils.getGroupId();
        return eventRepository.findByIdAndGroupId(eventId, groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
    }

    /**
     * Resolve tags pelo nome dentro do grupo.
     * Se a tag já existir no grupo, reutiliza. Se não, cria.
     * O nome é normalizado: lowercase e sem espaços extras.
     */
    private List<Tag> resolveTags(List<String> tagNames, UUID groupId) {
        if (tagNames == null || tagNames.isEmpty()) return List.of();

        var group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        return tagNames.stream()
                .map(name -> name.trim().toLowerCase())
                .distinct()
                .map(normalizedName -> tagRepository
                        .findByNameAndGroupId(normalizedName, groupId)
                        .orElseGet(() -> tagRepository.save(
                                Tag.builder()
                                        .name(normalizedName)
                                        .group(group)
                                        .build()
                        )))
                .toList();
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId().toString(),
                event.getName(),
                event.getDescription(),
                event.getStartDateTime().toString(),
                event.isActive(),
                event.getTags().stream().map(Tag::getName).toList(),
                event.getGroup().getId().toString(),
                event.getCreatedAt().toString()
        );
    }

    private EventSummary toSummary(Event event) {
        return new EventSummary(
                event.getId().toString(),
                event.getName(),
                event.isActive(),
                event.getTags().stream().map(Tag::getName).toList(),
                event.getStartDateTime().toString()
        );
    }
}