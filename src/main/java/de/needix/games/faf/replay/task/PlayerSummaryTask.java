package de.needix.games.faf.replay.task;

import de.needix.games.faf.replay.api.controllers.RootController;
import de.needix.games.faf.replay.api.entities.player.Player;
import de.needix.games.faf.replay.api.entities.replay.Replay;
import de.needix.games.faf.replay.api.entities.replay.ReplayPlayer;
import de.needix.games.faf.replay.api.entities.summarystats.ReplayPlayerSummary;
import de.needix.games.faf.replay.api.repositories.PlayerRepository;
import de.needix.games.faf.replay.api.repositories.ReplayRepository;
import de.needix.games.faf.replay.api.summary.PlayerSummaryUpdater;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.Set;

@Service
public class PlayerSummaryTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerSummaryTask.class);

    @Autowired
    private ReplayRepository replayRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @PostConstruct
    public void init() {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    // Runs every 5 minutes (adjust via application.properties if needed)
    @Scheduled(fixedDelayString = "${faf.summary.update-interval-ms:300000}")
    public void updatePlayers() {
        LOGGER.info("Starting background player updates...");

        Slice<Replay> newReplaysSlice = replayRepository.findByNewReplayTrue(PageRequest.of(0, 100));
        while (newReplaysSlice.hasContent()) {
            for (Replay replay : newReplaysSlice) {
                LOGGER.info("Updating player summaries for replay {}", replay.getId());
                transactionTemplate.executeWithoutResult(status ->
                        replay.getPlayers().forEach(
                                replayPlayer ->
                                        updateDatabasePlayerFromReplayPlayer(replay, replayPlayer)
                        )
                );
            }

            newReplaysSlice = replayRepository.findByNewReplayTrue(PageRequest.of(0, 100));
        }

        LOGGER.info("Finished background player updates.");
    }

    private void updateDatabasePlayerFromReplayPlayer(Replay replay, ReplayPlayer replayPlayer) {
        String ownerId = (String) replayPlayer.getArmyInformation().get("OwnerID");
        if (ownerId == null) {
            ownerId = "noOwnerId_" + replayPlayer.getName();
        }
        Player databasePlayer = playerRepository.findPlayerByOwnerId(ownerId);
        if (databasePlayer == null) {
            databasePlayer = new Player();
            databasePlayer.setName(replayPlayer.getName());
            databasePlayer.setOwnerId(ownerId);
            RootController.saveEntityInDatabase(playerRepository, databasePlayer);
        }
        replayPlayer.setPlayer(databasePlayer);

        final Player finalDatabasePlayer = databasePlayer;
        Optional<ReplayPlayerSummary> optionalPlayerSummary = getPlayerSummary(replay.getPlayerScores(), replayPlayer);
        optionalPlayerSummary.ifPresent(replayPlayerSummary -> replayPlayerSummary.setPlayer(finalDatabasePlayer));

        replay.setNewReplay(false);
        RootController.saveEntityInDatabase(replayRepository, replay);

        PlayerSummaryUpdater.updatePlayerSummary(databasePlayer);
        RootController.saveEntityInDatabase(playerRepository, databasePlayer);

        entityManager.detach(replay);
        entityManager.detach(databasePlayer);
    }

    private Optional<ReplayPlayerSummary> getPlayerSummary(Set<ReplayPlayerSummary> playerScores, ReplayPlayer replayPlayer) {
        return playerScores.stream().filter(replayPlayerSummary -> replayPlayerSummary.getName().equalsIgnoreCase(replayPlayer.getName())).findAny();
    }
}
