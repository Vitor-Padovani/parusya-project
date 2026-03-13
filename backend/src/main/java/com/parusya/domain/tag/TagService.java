package com.parusya.domain.tag;

import com.parusya.infra.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    // ─── GET /v1/tags ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TagResponse> listByGroup() {
        UUID groupId = SecurityUtils.getGroupId();
        return tagRepository.findAllByGroupId(groupId).stream()
                .map(tag -> new TagResponse(tag.getId().toString(), tag.getName()))
                .toList();
    }

    public record TagResponse(String id, String name) {}
}