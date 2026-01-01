package de.needix.games.faf.replay.api.repositories;

import de.needix.games.faf.replay.api.entities.replay.Replay;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplayRepository extends JpaRepository<Replay, Long>, JpaSpecificationExecutor<Replay>, ReplayRepositoryCustom {
    @Query("SELECT r FROM Replay r JOIN r.players p WHERE p.name = :playerName order by r.gameStart asc")
    List<Replay> findAllReplaysByPlayerName(@Param("playerName") String playerName);

    Slice<Replay> findByNewReplayTrue(Pageable pageable);

    @Query("SELECT r FROM Replay r WHERE (:cursor IS NULL OR r.id > :cursor) " +
            "ORDER BY r.id ASC")
    List<Replay> findReplaysWithCursor(@Param("cursor") Long cursor, Pageable pageable);

    @Query("SELECT r.id FROM Replay r")
    Slice<Long> getAllIds(Pageable pageable);

}
