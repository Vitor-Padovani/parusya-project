package com.parusya.domain.tag;

import jakarta.persistence.*;
import lombok.*;

import com.parusya.domain.group.Group;

import java.util.UUID;

@Entity
@Table(
        name = "tags",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_tag_name_group",
                columnNames = {"name", "group_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Normalizado para lowercase sem espaços extras antes de persistir
    @Column(nullable = false)
    private String name;

    // Tags são isoladas por Grupo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
}