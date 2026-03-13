package com.parusya.infra.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Implementação de UserDetails que carrega as informações necessárias
 * para autorização: id do usuário, role e group_id (null para Participants).
 *
 * É retornada pelo CustomUserDetailsService e usada pelo JwtService
 * para popular as claims do token.
 */
public class AuthenticatedUser implements UserDetails {

    private final UUID id;
    private final String name;
    private final String email;
    private final String password;
    private final UserRole role;
    private final UUID groupId; // null para Participant (entidade global)

    public AuthenticatedUser(UUID id, String name, String email,
                             String password, UserRole role, UUID groupId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.groupId = groupId;
    }

    public UUID getId()      { return id; }
    public String getName()  { return name; }
    public UserRole getRole(){ return role; }
    public UUID getGroupId() { return groupId; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getPassword()             { return password; }
    @Override public String getUsername()             { return email; }
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()              { return true; }
}