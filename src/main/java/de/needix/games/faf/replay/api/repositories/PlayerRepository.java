package de.needix.games.faf.replay.api.repositories;

import de.needix.games.faf.replay.api.entities.replay.ReplayPlayer;
import de.needix.games.faf.replay.api.entities.summarystats.ReplayPlayerSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PlayerRepository extends CrudRepository<ReplayPlayer, Long> {

    @Query("SELECT DISTINCT p.name FROM ReplayPlayer p WHERE p.name IS NOT NULL ORDER BY p.name")
    Page<String> findDistinctPlayerNames(Pageable pageable);

    @Query("SELECT p FROM ReplayPlayer p WHERE p.name = :playerName")
    List<ReplayPlayer> findAllByPlayerName(String playerName);

    @Query("SELECT s FROM ReplayPlayerSummary s WHERE s.name = :playerName")
    List<ReplayPlayerSummary> findAllSummariesByPlayerName(String playerName);

    @Query("SELECT DISTINCT p.name FROM ReplayPlayer p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY p.name")
    Page<String> searchReplayPlayer(Pageable pageable, String searchTerm);
}
