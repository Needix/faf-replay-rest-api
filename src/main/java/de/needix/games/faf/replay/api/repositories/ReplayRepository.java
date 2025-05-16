package de.needix.games.faf.replay.api.repositories;

import de.needix.games.faf.replay.api.entities.replay.Replay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplayRepository extends JpaRepository<Replay, Long>, JpaSpecificationExecutor<Replay> {
    @Query("SELECT r FROM Replay r JOIN r.players p WHERE p.name = :playerName order by r.gameStart asc")
    List<Replay> findAllReplaysByPlayerName(@Param("playerName") String playerName);

    @Query("SELECT r.id FROM Replay r")
    Page<Long> findReplayIds(Pageable pageable);
}
