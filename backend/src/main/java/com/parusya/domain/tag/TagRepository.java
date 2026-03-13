package com.parusya.domain.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findAllByGroupId(UUID groupId);

    Optional<Tag> findByNameAndGroupId(String name, UUID groupId);

    boolean existsByNameAndGroupId(String name, UUID groupId);
}