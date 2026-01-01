package de.needix.games.faf.replay.api.summary;

import de.needix.games.faf.replay.api.entities.player.*;
import de.needix.games.faf.replay.api.entities.replay.ReplayPlayer;
import de.needix.games.faf.replay.api.entities.replay.ReplayPlayerApm;
import de.needix.games.faf.replay.api.entities.summarystats.*;
import de.needix.games.faf.replay.api.entities.summarystats.UnitStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerSummaryUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerSummaryUpdater.class);

    public static void updatePlayerSummary(Player player) {
        LOGGER.debug("Updating player summary for player {}", player.getName());

        PlayerSummary playerSummary = player.getPlayerSummary();
        if (playerSummary == null) {
            playerSummary = new PlayerSummary();
            playerSummary.setOwnerId(player.getOwnerId());
            playerSummary.setName(player.getName());
            player.setPlayerSummary(playerSummary);
        }

        Map<Date, String> playerNameHistory = playerSummary.getPlayerNameHistory();
        playerNameHistory.clear();
        List<PlayerRating> ratingHistory = playerSummary.getRatingHistory();
        ratingHistory.clear();
        List<Date> gamesPlayedHistory = playerSummary.getGamesPlayedHistory();
        gamesPlayedHistory.clear();
        Map<Faction, FactionStats> factionStatsMap = playerSummary.getFactionStats();
        factionStatsMap.clear();

        Set<ReplayPlayerSummary> replayPlayerSummaries = player.getReplayPlayerSummaries();
        for (ReplayPlayerSummary replayPlayerSummary : replayPlayerSummaries) {
            Date date = new Date(replayPlayerSummary.getReplay().getGameStart() * 1000);

            Faction faction = replayPlayerSummary.getFaction();

            GeneralStats generalStats = replayPlayerSummary.getGeneral();
            ResourceStats resourceStats = replayPlayerSummary.getResources();
            UnitStats unitStats = replayPlayerSummary.getUnits();
            Map<String, BlueprintStats> blueprintStatsMap = replayPlayerSummary.getBlueprints();

            FactionStats factionStats = factionStatsMap.computeIfAbsent(faction, e -> new FactionStats());
            factionStats.setFaction(faction);
            factionStats.getDefeatedStats().add(replayPlayerSummary.getDefeated());
            factionStats.setTotalEnergyReceived(factionStats.getTotalEnergyReceived() + resourceStats.getEnergyIn().getTotal());
            factionStats.setTotalEnergyShared(factionStats.getTotalEnergyShared() + resourceStats.getEnergyOut().getTotal());
            factionStats.setTotalMassReceived(factionStats.getTotalMassReceived() + resourceStats.getMassIn().getTotal());
            factionStats.setTotalMassShared(factionStats.getTotalMassShared() + resourceStats.getMassOut().getTotal());
        }

        Set<ReplayPlayer> allReplayPlayers = player.getReplayPlayers();
        for (ReplayPlayer replayPlayer : allReplayPlayers) {
            Date date = new Date(replayPlayer.getReplay().getGameStart() * 1000);
            gamesPlayedHistory.add(date);

            double energyReceived = replayPlayer.getEnergyReceived();
            double energyShared = replayPlayer.getEnergyShared();
            double massReceived = replayPlayer.getMassReceived();
            double massShared = replayPlayer.getMassShared();

            List<ReplayPlayerApm> apmPerMinute = replayPlayer.getApmPerMinute();

            Map<String, Object> armyInformation = replayPlayer.getArmyInformation();
            Number faction = (Number) armyInformation.get("Faction");
            String ownerId = (String) armyInformation.get("OwnerID");

            Number gamesPlayed = (Number) armyInformation.get("NG");
            Number placement = (Number) armyInformation.get("PL");
            Number mean = (Number) armyInformation.get("MEAN");
            PlayerRating playerRating = new PlayerRating();
            playerRating.setDate(date);
            playerRating.setMean(mean == null ? -1 : mean.intValue());
            playerRating.setGamePlayed(gamesPlayed == null ? -1 : gamesPlayed.intValue());
            playerRating.setRating(placement == null ? -1 : placement.intValue());
            ratingHistory.add(playerRating);

            String playerName = (String) armyInformation.get("PlayerName");
            playerNameHistory.put(date, playerName);
        }
        cleanPlayerNameHistory(playerNameHistory);
    }

    private static void cleanPlayerNameHistory(Map<Date, String> playerNameHistory) {
        playerNameHistory.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .filter(entry -> entry.getValue() != null)
                .filter(entry ->
                        !entry.getValue().equals(PreviousStringHolder.PREVIOUS)
                )
                .peek(entry -> PreviousStringHolder.PREVIOUS = entry.getValue())
                .map(Map.Entry::getKey)
                .forEach(playerNameHistory::remove);
    }

    private void addUnitStats(UnitStats combined, UnitStats toAdd) {
        if (toAdd == null) return;

        combineUnitDetail(combined.getAir(), toAdd.getAir());
        combineUnitDetail(combined.getLand(), toAdd.getLand());
        combineUnitDetail(combined.getExperimental(), toAdd.getExperimental());
        combineUnitDetail(combined.getTech1(), toAdd.getTech1());
        combineUnitDetail(combined.getTech2(), toAdd.getTech2());
        combineUnitDetail(combined.getTech3(), toAdd.getTech3());
        combineUnitDetail(combined.getStructures(), toAdd.getStructures());
        combineUnitDetail(combined.getSacu(), toAdd.getSacu());
        combineUnitDetail(combined.getEngineer(), toAdd.getEngineer());
        combineUnitDetail(combined.getNaval(), toAdd.getNaval());
        combineUnitDetail(combined.getTransportation(), toAdd.getTransportation());
    }

    private void combineUnitDetail(UnitStats.UnitDetail combined, UnitStats.UnitDetail toAdd) {
        if (toAdd == null) return;
        if (combined == null) {
            // If combined is null, initialize it (you may need to add this logic to UnitStats as well)
            combined = new UnitStats.UnitDetail();
        }

        combined.setLost(combined.getLost() + toAdd.getLost());
        combined.setKills(combined.getKills() + toAdd.getKills());
        combined.setBuilt(combined.getBuilt() + toAdd.getBuilt());
    }

    private void addResourceStats(ResourceStats combined, ResourceStats toAdd) {
        if (toAdd == null) return;

        // Combine massIn
        if (toAdd.getMassIn() != null) {
            if (combined.getMassIn() == null) combined.setMassIn(new ResourceStats.InStats());
            combined.getMassIn().setTotal(combined.getMassIn().getTotal() + toAdd.getMassIn().getTotal());
            combined.getMassIn().setReclaimed(combined.getMassIn().getReclaimed() + toAdd.getMassIn().getReclaimed());
            combined.getMassIn().setReclaimRate(combined.getMassIn().getReclaimRate() + toAdd.getMassIn().getReclaimRate());
            combined.getMassIn().setRate(combined.getMassIn().getRate() + toAdd.getMassIn().getRate());
        }

        // Combine massOut
        if (toAdd.getMassOut() != null) {
            if (combined.getMassOut() == null) combined.setMassOut(new ResourceStats.OutStats());
            combined.getMassOut().setTotal(combined.getMassOut().getTotal() + toAdd.getMassOut().getTotal());
            combined.getMassOut().setRate(combined.getMassOut().getRate() + toAdd.getMassOut().getRate());
            combined.getMassOut().setExcess(combined.getMassOut().getExcess() + toAdd.getMassOut().getExcess());
        }

        // Combine energyIn
        if (toAdd.getEnergyIn() != null) {
            if (combined.getEnergyIn() == null) combined.setEnergyIn(new ResourceStats.InStats());
            combined.getEnergyIn().setTotal(combined.getEnergyIn().getTotal() + toAdd.getEnergyIn().getTotal());
            combined.getEnergyIn().setReclaimed(combined.getEnergyIn().getReclaimed() + toAdd.getEnergyIn().getReclaimed());
            combined.getEnergyIn().setReclaimRate(combined.getEnergyIn().getReclaimRate() + toAdd.getEnergyIn().getReclaimRate());
            combined.getEnergyIn().setRate(combined.getEnergyIn().getRate() + toAdd.getEnergyIn().getRate());
        }

        // Combine energyOut
        if (toAdd.getEnergyOut() != null) {
            if (combined.getEnergyOut() == null) combined.setEnergyOut(new ResourceStats.OutStats());
            combined.getEnergyOut().setTotal(combined.getEnergyOut().getTotal() + toAdd.getEnergyOut().getTotal());
            combined.getEnergyOut().setRate(combined.getEnergyOut().getRate() + toAdd.getEnergyOut().getRate());
            combined.getEnergyOut().setExcess(combined.getEnergyOut().getExcess() + toAdd.getEnergyOut().getExcess());
        }

        // Combine storage
        if (toAdd.getStorage() != null) {
            if (combined.getStorage() == null) combined.setStorage(new ResourceStats.StorageStats());
            combined.getStorage().setMaxMass(combined.getStorage().getMaxMass() + toAdd.getStorage().getMaxMass());
            combined.getStorage().setStoredMass(combined.getStorage().getStoredMass() + toAdd.getStorage().getStoredMass());
            combined.getStorage().setMaxEnergy(combined.getStorage().getMaxEnergy() + toAdd.getStorage().getMaxEnergy());
            combined.getStorage().setStoredEnergy(combined.getStorage().getStoredEnergy() + toAdd.getStorage().getStoredEnergy());
        }
    }

    // Helper class to hold the previous string
    static class PreviousStringHolder {
        static String PREVIOUS = null;
    }
}
