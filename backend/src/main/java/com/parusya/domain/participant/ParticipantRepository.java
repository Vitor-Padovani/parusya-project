package com.parusya.domain.participant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, UUID> {

    Optional<Participant> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}