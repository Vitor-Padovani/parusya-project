package com.parusya.domain.staff;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.parusya.domain.group.Group;
import com.parusya.domain.organizer.Organizer;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Acesso definido pelo group_id, não pelo organizer criador.
    // createdBy existe apenas para auditoria.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = true)
    private Organizer createdBy;
}