package de.needix.games.faf.replay.api.repositories;

import de.needix.games.faf.replay.api.entities.summarystats.ReplayPlayerSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplayPlayerSummaryRepository extends JpaRepository<ReplayPlayerSummary, Long> {
}