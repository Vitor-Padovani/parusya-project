package com.parusya.domain.checkin;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.parusya.domain.event.Event;
import com.parusya.domain.participant.Participant;
import com.parusya.domain.staff.EventStaff;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "check_ins",
        uniqueConstraints = {
                // Garante a nível de banco que um Participant não faça check-in duas vezes no mesmo evento
                @UniqueConstraint(name = "uk_checkin_participant_event", columnNames = {"participant_id", "event_id"})
        },
        indexes = {
                // Acelera queries de log e estatísticas filtradas por evento e período
                @Index(name = "idx_checkin_event_timestamp", columnList = "event_id, timestamp")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // Nullable: se o EventStaff for removido do Grupo, o histórico permanece íntegro
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = true)
    private EventStaff staff;
}