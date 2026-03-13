package com.parusya.domain.event;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.parusya.domain.group.Group;
import com.parusya.domain.tag.Tag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "events",
        indexes = {
                // Acelera GET /events/active — chamado pelo EventStaff a cada sessão de scan
                @Index(name = "idx_events_group_active", columnList = "group_id, is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    // Começa sempre false. Ativado manualmente pelo Organizer.
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Evento pertence ao Grupo, não ao Organizer que o criou
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToMany
    @JoinTable(
            name = "event_tags",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private List<Tag> tags = new ArrayList<>();
}