package de.needix.games.faf.replay.api.controllers;

import de.needix.games.faf.replay.api.entities.summarystats.ReplayPlayerSummary;
import de.needix.games.faf.replay.api.entities.summarystats.ResourceStats;
import de.needix.games.faf.replay.api.entities.summarystats.UnitStats;
import de.needix.games.faf.replay.api.repositories.PlayerRepository;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/players")
public class PlayerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerController.class);

    @Autowired
    private PlayerRepository playerRepository;

    @Operation(summary = "Search players available in replays by playername")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found replays",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<Page<String>> searchPlayerNames(@RequestParam(defaultValue = "") String searchTerm,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        if (StringUtils.isBlank(searchTerm)) {
            return getPlayerNames(page, size);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<String> playerNames = playerRepository.searchReplayPlayer(pageable, searchTerm);
        return ResponseEntity.ok(playerNames);
    }

    @Operation(summary = "Lists all players available in replays")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All player names available in replays",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/list")
    public ResponseEntity<Page<String>> getPlayerNames(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<String> playerNames = playerRepository.findDistinctPlayerNames(pageable);
        return ResponseEntity.ok(playerNames);
    }

    @Operation(summary = "A summary of the stats a given playername")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A summary of the stats a given playername",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{playerName}/summary")
    public ResponseEntity<?> getPlayerStatsSummary(
            @Parameter(description = "The name of the player", example = "Need")
            @PathVariable("playerName")
            String playerName) {
//        List<ReplayPlayer> replayPlayer = playerRepository.findAllByPlayerName(playerName);

        List<ReplayPlayerSummary> summaries = playerRepository.findAllSummariesByPlayerName(playerName);

        if (summaries.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<Integer, Object> result = new HashMap<>();
        Map<Integer, List<ReplayPlayerSummary>> summaryGroupedByFaction = summaries.stream().collect(Collectors.groupingBy(ReplayPlayerSummary::getFaction));
        for (var entry : summaryGroupedByFaction.entrySet()) {
            List<ReplayPlayerSummary> factionSummaries = entry.getValue();

            // Aggregate numerical and list-based stats
            List<String> typesList = factionSummaries.stream().map(ReplayPlayerSummary::getType).distinct().toList();
            List<Integer> factionsList = factionSummaries.stream().map(ReplayPlayerSummary::getFaction).distinct().toList();
            List<Double> defeated = factionSummaries.stream().map(ReplayPlayerSummary::getDefeated).toList();

            // Create combined unit stats
            UnitStats combinedUnits = new UnitStats();
            factionSummaries.forEach(summary -> addUnitStats(combinedUnits, summary.getUnits()));

            // Combine resources
            ResourceStats combinedResources = new ResourceStats();
            factionSummaries.forEach(summary -> addResourceStats(combinedResources, summary.getResources()));

            // Prepare response object
            var summary = new Object() {
                public final int totalReplays = summaries.size();
                public final Map<String, Object> units = combinedUnits.toMap();
                public final Map<String, Object> resources = combinedResources.toMap();
                public final List<Double> defeatedStats = defeated;
                public final List<Integer> factions = factionsList;
                public final List<String> types = typesList;
            };
            result.put(entry.getKey(), summary);
        }

        return ResponseEntity.ok(result);
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
}