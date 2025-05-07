package de.needix.games.faf.replay.api.repositories;

import de.needix.games.faf.replay.api.entities.replay.Replay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplayRepository extends JpaRepository<Replay, Long> {
    @Query("SELECT r FROM Replay r JOIN r.players p WHERE p.name = :playerName")
    List<Replay> findAllReplaysByPlayerName(@Param("playerName") String playerName);

    @Query("SELECT r.id FROM Replay r")
    Page<Long> findReplayIds(Pageable pageable);

    @Query("SELECT DISTINCT r.id FROM Replay r " +
            "LEFT JOIN r.players p " +
            "WHERE LOWER(r.replayTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(r.gameType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(r.mapName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) "
    )
    Page<Long> findBySearchTerm(Pageable pageable, @Param("searchTerm") String searchTerm);

}
