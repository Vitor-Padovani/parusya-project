package com.parusya.domain.participant;

import com.parusya.domain.participant.dto.ParticipantDtos.*;
import com.parusya.infra.exception.BusinessException;
import com.parusya.infra.exception.BusinessException.ErrorCode;
import com.parusya.infra.security.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final QrCodeRepository qrCodeRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final PasswordEncoder passwordEncoder;

    public ParticipantService(ParticipantRepository participantRepository,
                              QrCodeRepository qrCodeRepository,
                              QrCodeGenerator qrCodeGenerator,
                              PasswordEncoder passwordEncoder) {
        this.participantRepository = participantRepository;
        this.qrCodeRepository = qrCodeRepository;
        this.qrCodeGenerator = qrCodeGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    // ─── POST /v1/participants/register ───────────────────────────────────────

    @Transactional
    public RegisterParticipantResponse register(RegisterParticipantRequest request) {
        if (participantRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (participantRepository.existsByPhone(request.phone())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
        }

        var participant = Participant.builder()
                .fullName(request.fullName())
                .gender(request.gender())
                .phone(request.phone())
                .email(request.email())
                .birthDate(request.birthDate())
                .password(passwordEncoder.encode(request.password()))
                .build();

        participantRepository.saveAndFlush(participant);

        // Gera o QR Code global imediatamente após o cadastro
        var qrCode = createQrCode(participant);

        return new RegisterParticipantResponse(
                participant.getId().toString(),
                participant.getFullName(),
                participant.getEmail(),
                new QrCodeSummary(
                        qrCode.getId().toString(),
                        qrCode.getEncodedData(),
                        qrCode.getCreatedAt().toString()
                )
        );
    }

    // ─── GET /v1/participants/me ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ParticipantResponse getMe() {
        var participant = findAuthenticatedParticipant();

        return new ParticipantResponse(
                participant.getId().toString(),
                participant.getFullName(),
                participant.getEmail(),
                participant.getPhone(),
                participant.getGender().name(),
                participant.getBirthDate().toString(),
                participant.getCreatedAt().toString()
        );
    }

    // ─── GET /v1/participants/me/qrcode ───────────────────────────────────────

    @Transactional(readOnly = true)
    public QrCodeResponse getMyQrCode() {
        var participant = findAuthenticatedParticipant();

        var qrCode = qrCodeRepository
                .findByParticipantIdAndEventIsNull(participant.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // Imagem gerada sob demanda — não armazenada no banco
        String imageBase64 = qrCodeGenerator.generateBase64(qrCode.getEncodedData());

        return new QrCodeResponse(
                qrCode.getId().toString(),
                qrCode.getEncodedData(),
                qrCode.getCreatedAt().toString(),
                imageBase64
        );
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private QrCode createQrCode(Participant participant) {
        String encodedData = qrCodeGenerator.buildEncodedData(participant.getId().toString());

        var qrCode = QrCode.builder()
                .encodedData(encodedData)
                .participant(participant)
                .build();

        return qrCodeRepository.saveAndFlush(qrCode);
    }

    private Participant findAuthenticatedParticipant() {
        UUID participantId = SecurityUtils.getUserId();
        return participantRepository.findById(participantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}