package com.parusya.domain.organizer.dto;

public class OrganizerDtos {

    public record OrganizerResponse(
            String id,
            String name,
            String email,
            String groupId,
            String createdAt
    ) {}
}