package de.needix.games.faf.replay.api.repositories;

import de.needix.games.faf.replay.api.entities.player.Player;
import de.needix.games.faf.replay.api.entities.player.PlayerSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerRepository extends CrudRepository<Player, Long>, JpaSpecificationExecutor<Player>, PlayerRepositoryCustom {

    @Query("SELECT p FROM Player p WHERE p.ownerId = :ownerId")
    Player findPlayerByOwnerId(@Param("ownerId") String ownerId);

    @Query("SELECT p FROM Player p WHERE (:cursor IS NULL OR p.ownerId > :cursor) " +
            "ORDER BY p.ownerId ASC")
    List<Player> findPlayersWithCursor(@Param("cursor") Long cursor, Pageable pageable);

    @Query("SELECT ps from PlayerSummary ps where ps.name = :name")
    PlayerSummary findPlayerSummary(@Param("cursor") String name);
}
