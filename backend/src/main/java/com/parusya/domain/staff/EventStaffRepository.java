package com.parusya.domain.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventStaffRepository extends JpaRepository<EventStaff, UUID> {

    Optional<EventStaff> findByEmail(String email);

    boolean existsByEmail(String email);

    List<EventStaff> findAllByGroupId(UUID groupId);
}