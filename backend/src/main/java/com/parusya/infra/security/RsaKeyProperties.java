package com.parusya.infra.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record RsaKeyProperties(
        String privateKey,   // conteúdo PEM em Base64 (sem headers)
        String publicKey,    // conteúdo PEM em Base64 (sem headers)
        long expirationSeconds
) {}