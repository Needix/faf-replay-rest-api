package de.needix.games.faf.replay.api.controllers;

import de.needix.games.faf.replay.api.entities.player.Player;
import de.needix.games.faf.replay.api.entities.player.PlayerSummary;
import de.needix.games.faf.replay.api.entities.replay.ReplayPlayer;
import de.needix.games.faf.replay.api.repositories.PlayerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<?>> searchReplays(@RequestParam(required = false) String query,
                                                 @RequestParam(required = false) Long cursor,
                                                 @RequestParam(required = false) Integer size) {
        LOGGER.info("Received search request for query '{}' and cursor '{}'", query, cursor);

        Pageable pageable = PageRequest.of(0, size != null ? size : 20); // Default to 20 replays per request

        if (StringUtils.isEmpty(query)) {
            List<Player> replays = playerRepository.findPlayersWithCursor(cursor, pageable);
            LOGGER.info("Found (cursor) {} results in {} ms", replays.size(), System.currentTimeMillis() - System.currentTimeMillis());
            return ResponseEntity.ok(replays);
        }

        final long startTime = System.currentTimeMillis();
        try {
            Specification<ReplayPlayer> spec = Specification.where(PlayerSpecification.playerNameContains(query))
                    .and(PlayerSpecification.cursor(cursor));

            Slice<ReplayPlayer> result = playerRepository.findSlice(spec, pageable, ReplayPlayer.class);

            LOGGER.info("Found (filter) {} results in {} ms", result.getNumberOfElements(), System.currentTimeMillis() - startTime);
            return ResponseEntity.ok(result.getContent()); // Return only IDs

        } catch (Exception e) {
            LOGGER.error("Error occurred while searching for replays: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "A summary of the stats a given playername")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A summary of the stats a given playername",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Player.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{playerName}")
    public ResponseEntity<PlayerSummary> getPlayerStatsSummary(
            @Parameter(description = "The name of the player", example = "Need")
            @PathVariable("playerName")
            String playerName) {

        return ResponseEntity.ok(playerRepository.findReplayPlayerSummary(playerName));
    }
}