package de.needix.games.faf.replay.analyser;

import de.needix.games.faf.replay.api.entities.player.FactionStats;
import de.needix.games.faf.replay.api.entities.player.Player;
import de.needix.games.faf.replay.api.entities.player.PlayerRating;
import de.needix.games.faf.replay.api.entities.player.PlayerSummary;
import de.needix.games.faf.replay.api.entities.replay.ReplayPlayer;
import de.needix.games.faf.replay.api.entities.replay.ReplayPlayerApm;
import de.needix.games.faf.replay.api.entities.summarystats.*;

import java.util.*;

public class PlayerSummaryUpdater {
    public static void updatePlayerSummary(Player player) {
        PlayerSummary playerSummary = player.getPlayerSummary();
        if (playerSummary == null) {
            playerSummary = new PlayerSummary();
            playerSummary.setOwnerId(player.getOwnerId());
            playerSummary.setName(player.getName());
            player.setPlayerSummary(playerSummary);
        }

        Map<Date, String> playerNameHistory = new HashMap<>();
        List<PlayerRating> ratingHistory = new ArrayList<>();
        List<Date> gamesPlayedHistory = new ArrayList<>();

        Map<Integer, FactionStats> factionStatsMap = new HashMap<>();

        List<ReplayPlayerSummary> replayPlayerSummaries = player.getReplayPlayerSummaries();
        for (ReplayPlayerSummary replayPlayerSummary : replayPlayerSummaries) {
            Date date = new Date(replayPlayerSummary.getReplay().getGameStart() * 1000);

            int faction = replayPlayerSummary.getFaction();

            GeneralStats generalStats = replayPlayerSummary.getGeneral();
            ResourceStats resourceStats = replayPlayerSummary.getResources();
            UnitStats unitStats = replayPlayerSummary.getUnits();
            Map<String, BlueprintStats> blueprintStatsMap = replayPlayerSummary.getBlueprints();

            double defeatedTimes = replayPlayerSummary.getDefeated();

            FactionStats factionStats = factionStatsMap.computeIfAbsent(faction, e -> new FactionStats());
            factionStats.setId(player.getOwnerId() + "_" + faction);
            factionStats.getDefeatedStats().add(defeatedTimes);
            factionStats.setTotalEnergyReceived(factionStats.getTotalEnergyReceived() + resourceStats.getEnergyIn().getTotal());
            factionStats.setTotalEnergyShared(factionStats.getTotalEnergyShared() + resourceStats.getEnergyOut().getTotal());
            factionStats.setTotalMassReceived(factionStats.getTotalMassReceived() + resourceStats.getMassIn().getTotal());
            factionStats.setTotalMassShared(factionStats.getTotalMassShared() + resourceStats.getMassOut().getTotal());
        }

        List<ReplayPlayer> allReplayPlayers = player.getReplayPlayers();
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
            playerRating.setMean(mean.intValue());
            playerRating.setGamePlayed(gamesPlayed.intValue());
            playerRating.setRating(placement.intValue());
            ratingHistory.add(playerRating);

            String playerName = (String) armyInformation.get("PlayerName");
            playerNameHistory.put(date, playerName);

            FactionStats factionStats = factionStatsMap.computeIfAbsent(faction.intValue(), e -> new FactionStats());
        }

        playerSummary.setPlayerNameHistory(playerNameHistory);
        playerSummary.setFactionStats(factionStatsMap.values().stream().toList());
        playerSummary.setRatingHistory(ratingHistory);
        playerSummary.setGamesPlayedHistory(gamesPlayedHistory);
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
}
