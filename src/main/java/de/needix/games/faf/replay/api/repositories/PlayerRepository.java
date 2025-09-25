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

    @Query("SELECT r FROM Replay r WHERE (:cursor IS NULL OR r.id > :cursor) " +
            "ORDER BY r.id ASC")
    List<Player> findPlayersWithCursor(@Param("cursor") Long cursor, Pageable pageable);

    @Query("SELECT ps from PlayerSummary ps where ps.name = :name")
    PlayerSummary findReplayPlayerSummary(@Param("cursor") String name);
}
