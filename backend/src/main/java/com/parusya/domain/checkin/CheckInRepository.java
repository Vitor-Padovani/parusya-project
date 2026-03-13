package com.parusya.domain.checkin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {

    // Verifica duplicidade antes de tentar persistir (resposta rápida ao EventStaff)
    boolean existsByParticipantIdAndEventId(UUID participantId, UUID eventId);

    // Log paginado de um evento com filtros opcionais
    @Query("""
        SELECT c FROM CheckIn c
        WHERE c.event.id = :eventId
          AND (CAST(:startTime AS localDateTime) IS NULL OR c.timestamp >= :startTime)
          AND (CAST(:endTime AS localDateTime) IS NULL OR c.timestamp <= :endTime)
          AND (CAST(:staffId AS uuid) IS NULL OR c.staff.id = :staffId)
          AND (:name IS NULL OR LOWER(c.participant.fullName) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
        ORDER BY c.timestamp DESC
    """)
    Page<CheckIn> findEventLog(
            @Param("eventId")   UUID eventId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime")   LocalDateTime endTime,
            @Param("staffId")   UUID staffId,
            @Param("name")      String name,
            Pageable pageable
    );

    // Total de check-ins por evento — usado nas estatísticas
    long countByEventId(UUID eventId);

    // Distribuição por hora — usado no gráfico de frequência
    @Query(value = """
        SELECT
            date_trunc('minute', timestamp - (EXTRACT(MINUTE FROM timestamp)::int % 5) * INTERVAL '1 minute') AS slot,
            COUNT(*) AS count
        FROM check_ins
        WHERE event_id = :eventId
        GROUP BY slot
        ORDER BY slot
    """, nativeQuery = true)
    List<Object[]> countByIntervalForEvent(@Param("eventId") UUID eventId);

    // Breakdown por EventStaff — quantos check-ins cada membro realizou no evento
    @Query("""
        SELECT c.staff.id, c.staff.name, COUNT(c)
        FROM CheckIn c
        WHERE c.event.id = :eventId AND c.staff IS NOT NULL
        GROUP BY c.staff.id, c.staff.name
    """)
    List<Object[]> countByStaffForEvent(@Param("eventId") UUID eventId);

    @Modifying
    @Query("UPDATE CheckIn c SET c.staff = null WHERE c.staff.id = :staffId")
    void nullifyStaffReference(@Param("staffId") UUID staffId);

    @Modifying
    @Query("DELETE FROM CheckIn c WHERE c.participant.id = :participantId")
    void deleteAllByParticipantId(@Param("participantId") UUID participantId);

    // Check-ins de múltiplos eventos — usado nas estatísticas agregadas
    List<CheckIn> findAllByEventIdIn(List<UUID> eventIds);

    // ─── Ranking de participants por grupo ───────────────────────────────────

    // Ranking geral: todos os participants com check-in no grupo, ordenado por contagem DESC
    @Query("""
    SELECT c.participant.id, c.participant.fullName, COUNT(c)
    FROM CheckIn c
    WHERE c.event.group.id = :groupId
      AND (:name IS NULL OR LOWER(c.participant.fullName) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
    GROUP BY c.participant.id, c.participant.fullName
    ORDER BY COUNT(c) DESC, c.participant.fullName ASC
""")
    Page<Object[]> rankParticipantsByGroup(
            @Param("groupId") UUID groupId,
            @Param("name")    String name,
            Pageable pageable
    );

    // Ranking de ausentes: participants que NÃO fizeram check-in no último evento
    @Query("""
        SELECT c.participant.id, c.participant.fullName, COUNT(c)
        FROM CheckIn c
        WHERE c.event.group.id = :groupId
          AND (:name IS NULL OR LOWER(c.participant.fullName) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
          AND c.participant.id NOT IN (
              SELECT ab.participant.id
              FROM CheckIn ab
              WHERE ab.event.id = :lastEventId
          )
        GROUP BY c.participant.id, c.participant.fullName
        ORDER BY COUNT(c) DESC, c.participant.fullName ASC
    """)
    Page<Object[]> rankAbsentFromLastEvent(
            @Param("groupId")     UUID groupId,
            @Param("lastEventId") UUID lastEventId,
            @Param("name")        String name,
            Pageable pageable
    );

    // Último evento do grupo — maior startDateTime independente de estar ativo
    @Query("""
        SELECT e.id FROM Event e
        WHERE e.group.id = :groupId
        ORDER BY e.startDateTime DESC
        LIMIT 1
    """)
    Optional<UUID> findLastEventIdByGroup(@Param("groupId") UUID groupId);

    // Todas as datas de check-in de um participant num grupo — para o perfil
    @Query("""
        SELECT c.timestamp
        FROM CheckIn c
        WHERE c.participant.id = :participantId
          AND c.event.group.id = :groupId
        ORDER BY c.timestamp ASC
    """)
    List<LocalDateTime> findCheckInDatesByParticipantAndGroup(
            @Param("participantId") UUID participantId,
            @Param("groupId")       UUID groupId
    );

    // Exportação: todos os check-ins do grupo com participant e evento carregados
    @Query("""
        SELECT c FROM CheckIn c
        JOIN FETCH c.participant
        JOIN FETCH c.event
        WHERE c.event.group.id = :groupId
        ORDER BY c.event.startDateTime ASC, c.timestamp ASC
    """)
    List<CheckIn> findAllByGroupIdForExport(@Param("groupId") UUID groupId);
}