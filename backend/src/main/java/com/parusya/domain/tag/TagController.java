package com.parusya.domain.tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // GET /v1/tags — Organizer
    @GetMapping
    public ResponseEntity<List<TagService.TagResponse>> list() {
        return ResponseEntity.ok(tagService.listByGroup());
    }
}