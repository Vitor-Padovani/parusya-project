package com.parusya.infra.security;

import com.parusya.domain.organizer.OrganizerRepository;
import com.parusya.domain.participant.ParticipantRepository;
import com.parusya.domain.staff.EventStaffRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * O username passado ao loadUserByUsername segue o formato "email:ROLE"
 * para que o service saiba em qual repositório buscar.
 *
 * Exemplos:
 *   "joao@email.com:ORGANIZER"
 *   "maria@email.com:EVENT_STAFF"
 *   "pedro@email.com:PARTICIPANT"
 *
 * Esse formato é usado apenas internamente no fluxo de autenticação
 * (AuthenticationManager → DaoAuthenticationProvider → aqui).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final OrganizerRepository organizerRepository;
    private final EventStaffRepository staffRepository;
    private final ParticipantRepository participantRepository;

    public CustomUserDetailsService(OrganizerRepository organizerRepository,
                                    EventStaffRepository staffRepository,
                                    ParticipantRepository participantRepository) {
        this.organizerRepository = organizerRepository;
        this.staffRepository = staffRepository;
        this.participantRepository = participantRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String[] parts = username.split(":");
        if (parts.length != 2) {
            throw new UsernameNotFoundException("Formato de username inválido: " + username);
        }

        String email = parts[0];
        UserRole role;

        try {
            role = UserRole.valueOf(parts[1]);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Role desconhecida: " + parts[1]);
        }

        return switch (role) {
            case ORGANIZER -> organizerRepository.findByEmail(email)
                    .map(o -> new AuthenticatedUser(
                            o.getId(), o.getName(), o.getEmail(),
                            o.getPassword(), UserRole.ORGANIZER, o.getGroup().getId()))
                    .orElseThrow(() -> new UsernameNotFoundException("Organizer não encontrado: " + email));

            case EVENT_STAFF -> staffRepository.findByEmail(email)
                    .map(s -> new AuthenticatedUser(
                            s.getId(), s.getName(), s.getEmail(),
                            s.getPassword(), UserRole.EVENT_STAFF, s.getGroup().getId()))
                    .orElseThrow(() -> new UsernameNotFoundException("EventStaff não encontrado: " + email));

            case PARTICIPANT -> participantRepository.findByEmail(email)
                    .map(p -> new AuthenticatedUser(
                            p.getId(), p.getFullName(), p.getEmail(),
                            p.getPassword(), UserRole.PARTICIPANT, null))
                    .orElseThrow(() -> new UsernameNotFoundException("Participant não encontrado: " + email));
        };
    }
}