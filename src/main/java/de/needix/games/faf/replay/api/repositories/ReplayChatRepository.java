package de.needix.games.faf.replay.api.repositories;

import de.needix.games.faf.replay.api.entities.chat.ReplayChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplayChatRepository extends JpaRepository<ReplayChatMessage, Long> {
}
