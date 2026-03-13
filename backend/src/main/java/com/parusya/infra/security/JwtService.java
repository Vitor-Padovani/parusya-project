package com.parusya.infra.security;

import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Responsável por gerar JWTs com as claims necessárias para autorização.
 *
 * Claims incluídas no token:
 *   sub      → email do usuário
 *   user_id  → UUID do usuário
 *   role     → ORGANIZER | EVENT_STAFF | PARTICIPANT
 *   group_id → UUID do grupo (ausente para Participant)
 */
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final RsaKeyProperties rsaKeyProperties;

    public JwtService(JwtEncoder jwtEncoder, RsaKeyProperties rsaKeyProperties) {
        this.jwtEncoder = jwtEncoder;
        this.rsaKeyProperties = rsaKeyProperties;
    }

    public String generateToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(rsaKeyProperties.expirationSeconds());

        var claimsBuilder = JwtClaimsSet.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(expiry)
                .claim("user_id", user.getId().toString())
                .claim("name", user.getName())
                .claim("role", user.getRole().name());

        // group_id só é incluído para Organizer e EventStaff
        if (user.getGroupId() != null) {
            claimsBuilder.claim("group_id", user.getGroupId().toString());
        }

        var claims = claimsBuilder.build();
        var header = JwsHeader.with(() -> "RS256").build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpirationSeconds() {
        return rsaKeyProperties.expirationSeconds();
    }
}