package com.parusya.infra.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/**
 * Utilitário estático para extrair claims do JWT autenticado.
 *
 * Usado nos services para obter group_id e user_id sem precisar
 * receber esses valores como parâmetro do controller.
 *
 * Exemplo de uso:
 *   UUID groupId = SecurityUtils.getGroupId();
 *   UUID userId  = SecurityUtils.getUserId();
 */
public class SecurityUtils {

    private SecurityUtils() {}

    public static Jwt getJwt() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (Jwt) auth.getPrincipal();
    }

    /**
     * Retorna o group_id do token.
     * Nunca deve ser chamado em contexto de Participant (group_id é null).
     */
    public static UUID getGroupId() {
        String groupId = getJwt().getClaimAsString("group_id");
        if (groupId == null) {
            throw new IllegalStateException("group_id não presente no token — perfil não suportado neste contexto");
        }
        return UUID.fromString(groupId);
    }

    public static UUID getUserId() {
        return UUID.fromString(getJwt().getClaimAsString("user_id"));
    }

    public static String getUserRole() {
        return getJwt().getClaimAsString("role");
    }

    public static String getUserEmail() {
        return getJwt().getSubject();
    }
}