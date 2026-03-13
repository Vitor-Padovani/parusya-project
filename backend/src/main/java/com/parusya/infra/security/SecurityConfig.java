package com.parusya.infra.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(RsaKeyProperties.class)
public class SecurityConfig {

    private final RsaKeyProperties rsaKeyProperties;
    private final ResourceLoader resourceLoader;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(RsaKeyProperties rsaKeyProperties,
                          ResourceLoader resourceLoader,
                          CustomUserDetailsService userDetailsService) {
        this.rsaKeyProperties = rsaKeyProperties;
        this.resourceLoader = resourceLoader;
        this.userDetailsService = userDetailsService;
    }

    // ─── Filter Chain ─────────────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers(HttpMethod.POST, "/v1/auth/login/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/participants/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/groups").permitAll()

                        // Participant
                        .requestMatchers("/v1/participants/me/**").hasAuthority("ROLE_PARTICIPANT")

                        // EventStaff
                        .requestMatchers(HttpMethod.POST, "/v1/checkins/scan").hasAuthority("ROLE_EVENT_STAFF")
                        .requestMatchers(HttpMethod.GET, "/v1/events/active").hasAuthority("ROLE_EVENT_STAFF")

                        // Organizer
                        .requestMatchers("/v1/groups/me/**").hasAuthority("ROLE_ORGANIZER")
                        .requestMatchers("/v1/staff/**").hasAuthority("ROLE_ORGANIZER")
                        .requestMatchers("/v1/events/**").hasAuthority("ROLE_ORGANIZER")
                        .requestMatchers("/v1/checkins/**").hasAuthority("ROLE_ORGANIZER")
                        .requestMatchers("/v1/stats/**").hasAuthority("ROLE_ORGANIZER")
                        .requestMatchers("/v1/tags/**").hasAuthority("ROLE_ORGANIZER")
                        .requestMatchers("/v1/participants/ranking/**").hasAuthority("ROLE_ORGANIZER")
                        .requestMatchers("/v1/export/**").hasAuthority("ROLE_ORGANIZER")

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    // ─── JWT Converter: lê a claim "role" e converte para GrantedAuthority ────

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthoritiesClaimName("role");
        converter.setAuthorityPrefix("ROLE_");

        var authConverter = new JwtAuthenticationConverter();
        authConverter.setJwtGrantedAuthoritiesConverter(converter);
        return authConverter;
    }

    // ─── JwtDecoder: valida tokens com a chave pública (configurado no .properties) ─

    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey publicKey) {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    // ─── JwtEncoder: assina tokens com a chave privada ────────────────────────

    @Bean
    public JwtEncoder jwtEncoder(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
        var rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        var jwkSet = new ImmutableJWKSet<SecurityContext>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwkSet);
    }

    // ─── Chave pública (lida do classpath, configurada no .properties) ─────────

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        String pem = rsaKeyProperties.publicKey()
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        var keySpec = new java.security.spec.X509EncodedKeySpec(decoded);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    @Bean
    public RSAPrivateKey rsaPrivateKey() throws Exception {
        String pem = rsaKeyProperties.privateKey()
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        var keySpec = new PKCS8EncodedKeySpec(decoded);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    // ─── Password Encoder ─────────────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // ─── AuthenticationManager ────────────────────────────────────────────────

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(List.of(provider));
    }
}