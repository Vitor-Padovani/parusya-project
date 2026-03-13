package com.parusya.domain.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {

    // Usado pelo EventStaff para listar eventos disponíveis para scan
    List<Event> findAllByGroupIdAndIsActiveTrue(UUID groupId);

    // Verifica que o evento pertence ao grupo antes de qualquer operação
    Optional<Event> findByIdAndGroupId(UUID id, UUID groupId);

    // Listagem paginada para o Organizer — filtros avançados via Specification
    Page<Event> findAllByGroupId(UUID groupId, Pageable pageable);
}