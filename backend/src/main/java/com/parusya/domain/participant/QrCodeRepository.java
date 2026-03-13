package com.parusya.domain.participant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {

    // Busca o QR Code global do participant (event_id nulo = v1)
    Optional<QrCode> findByParticipantIdAndEventIsNull(UUID participantId);

    // Busca por conteúdo escaneado — usado na validação do check-in
    Optional<QrCode> findByEncodedData(String encodedData);
}