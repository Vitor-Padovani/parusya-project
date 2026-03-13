package com.parusya.domain.participant;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.parusya.domain.event.Event;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "qr_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // v1: "vapt:pid:<participant_uuid>"
    // futuro: "vapt:eid:<event_uuid>:pid:<participant_uuid>"
    @Column(nullable = false, unique = true)
    private String encodedData;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    // Nulo na v1 (QR Code global).
    // Preenchido em versões futuras para QR Codes restritos a um evento.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = true)
    private Event event;
}