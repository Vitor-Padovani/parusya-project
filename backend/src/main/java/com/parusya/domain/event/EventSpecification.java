package com.parusya.domain.event;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class EventSpecification {

    private EventSpecification() {}

    /**
     * Filtro dinâmico para listagem de eventos do grupo.
     *
     * Regras:
     * - group_id é sempre obrigatório (isolamento entre grupos)
     * - startDate e endDate são combináveis entre si (AND)
     * - tags usa filtro inclusivo: retorna eventos que tenham AO MENOS UMA das tags (OR)
     * - isActive é opcional
     * - Todos os filtros ativos são combinados com AND entre si
     */
    public static Specification<Event> filter(UUID groupId,
                                              LocalDateTime startDate,
                                              LocalDateTime endDate,
                                              List<String> tags,
                                              Boolean isActive) {
        return Specification
                .where(hasGroup(groupId))
                .and(afterOrEqual(startDate))
                .and(beforeOrEqual(endDate))
                .and(hasAnyTag(tags))
                .and(hasStatus(isActive));
    }

    private static Specification<Event> hasGroup(UUID groupId) {
        return (root, query, cb) ->
                cb.equal(root.get("group").get("id"), groupId);
    }

    private static Specification<Event> afterOrEqual(LocalDateTime startDate) {
        if (startDate == null) return null;
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("startDateTime"), startDate);
    }

    private static Specification<Event> beforeOrEqual(LocalDateTime endDate) {
        if (endDate == null) return null;
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("startDateTime"), endDate);
    }

    // Filtro inclusivo (OR): retorna eventos que possuam ao menos uma das tags informadas
    private static Specification<Event> hasAnyTag(List<String> tags) {
        if (tags == null || tags.isEmpty()) return null;
        return (root, query, cb) -> {
            var tagJoin = root.join("tags", JoinType.INNER);
            query.distinct(true);
            return tagJoin.get("name").in(tags);
        };
    }

    private static Specification<Event> hasStatus(Boolean isActive) {
        if (isActive == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("isActive"), isActive);
    }
}