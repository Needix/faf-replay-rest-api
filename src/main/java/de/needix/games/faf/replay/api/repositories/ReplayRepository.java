package de.needix.games.faf.replay.api.repositories;

import de.needix.games.faf.replay.api.entities.replay.Replay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReplayRepository extends JpaRepository<Replay, Long> {
    @Query("SELECT r FROM Replay r JOIN r.players p WHERE p.name = :playerName")
    List<Replay> findAllReplaysByPlayerName(@Param("playerName") String playerName);
}
