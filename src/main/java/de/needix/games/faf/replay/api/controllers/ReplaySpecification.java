package de.needix.games.faf.replay.api.controllers;

import de.needix.games.faf.replay.api.entities.replay.Replay;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.List;

public class ReplaySpecification {

    public static Specification<Replay> titleContains(String query) {
        /*
        "SELECT DISTINCT r.id FROM Replay r " +
            "LEFT JOIN r.players p " +
            "WHERE LOWER(r.replayTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(r.gameType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(r.mapName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) "
         */
        return (root, queryBuilder, criteriaBuilder) -> {
            if (query == null || query.isBlank()) {
                return null;
            }

            // Perform a join with players (adjust "players" to match the name of your relationship attribute in Replay)
            var playersJoin = root.join("players", JoinType.LEFT);

            // Build the predicates for the search
            Predicate replayTitlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("replayTitle")),
                    "%" + query.toLowerCase() + "%"
            );

            Predicate gameTypePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("gameType")),
                    "%" + query.toLowerCase() + "%"
            );

            Predicate mapNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("mapName")),
                    "%" + query.toLowerCase() + "%"
            );

            Predicate playerNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(playersJoin.get("name")),
                    "%" + query.toLowerCase() + "%"
            );

            // Combine the predicates with OR
            return criteriaBuilder.or(replayTitlePredicate, gameTypePredicate, mapNamePredicate, playerNamePredicate);
        };

    }

    public static Specification<Replay> isComplete(String completeStatus) {
        return (root, queryBuilder, criteriaBuilder) -> {
            if ("all".equalsIgnoreCase(completeStatus)) {
                return null; // No filter if "all"
            }
            boolean isComplete = "complete".equalsIgnoreCase(completeStatus);
            return criteriaBuilder.equal(root.get("complete"), isComplete);
        };
    }

    public static Specification<Replay> hasMods(List<String> mods) {
        return (root, queryBuilder, criteriaBuilder) ->
                mods == null || mods.isEmpty()
                        ? null
                        : root.get("featuredMod").in(mods);
    }

    public static Specification<Replay> hasGameTypes(List<String> gameTypes) {
        return (root, queryBuilder, criteriaBuilder) ->
                gameTypes == null || gameTypes.isEmpty()
                        ? null
                        : root.get("gameType").in(gameTypes);
    }

    public static Specification<Replay> playerCountInRange(Integer minPlayers, Integer maxPlayers) {
        return (root, queryBuilder, criteriaBuilder) -> {
            Predicate minCondition = minPlayers != null ? criteriaBuilder.greaterThanOrEqualTo(root.get("numberOfPlayers"), minPlayers) : null;
            Predicate maxCondition = maxPlayers != null ? criteriaBuilder.lessThanOrEqualTo(root.get("numberOfPlayers"), maxPlayers) : null;

            if (minCondition != null && maxCondition != null) {
                return criteriaBuilder.and(minCondition, maxCondition);
            } else if (minCondition != null) {
                return minCondition;
            } else return maxCondition;
        };
    }

    public static Specification<Replay> timeFrame(Date start, Date end) {
        return (root, queryBuilder, criteriaBuilder) -> {
            Predicate startCondition = start != null ? criteriaBuilder.greaterThanOrEqualTo(root.get("gameStart"), start.getTime()) : null;
            Predicate endCondition = end != null ? criteriaBuilder.lessThanOrEqualTo(root.get("gameEnd"), end.getTime()) : null;

            if (startCondition != null && endCondition != null) {
                return criteriaBuilder.and(startCondition, endCondition);
            } else if (startCondition != null) {
                return startCondition;
            } else return endCondition;
        };
    }

    public static Specification<Replay> isRanked(boolean rankedOnly) {
        return (root, queryBuilder, criteriaBuilder) ->
                !rankedOnly
                        ? null
                        : criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("cheatsEnabled"), false),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("mapName")), "%ranked%"), // Example, adjust it
                        criteriaBuilder.equal(root.get("teamLock"), true)
                );
    }
}
