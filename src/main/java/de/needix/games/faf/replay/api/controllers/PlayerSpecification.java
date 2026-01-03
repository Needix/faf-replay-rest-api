package de.needix.games.faf.replay.api.controllers;

import de.needix.games.faf.replay.api.entities.player.Player;
import org.springframework.data.jpa.domain.Specification;

public class PlayerSpecification {
    public static Specification<Player> cursor(Long cursor) {
        return (root, queryBuilder, criteriaBuilder) -> {
            if (cursor == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("id"), cursor);
        };
    }


    public static Specification<Player> playerNameContains(String query) {
        return (root, queryBuilder, criteriaBuilder) -> {
            if (query == null || query.isBlank()) {
                return null;
            }

            // Combine the predicates with OR
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + query.toLowerCase() + "%"
            );
        };
    }
}
