package de.needix.games.faf.replay.api.controllers;

import de.needix.games.faf.replay.analyser.ReplayAnalyser;
import de.needix.games.faf.replay.api.entities.replay.Replay;
import de.needix.games.faf.replay.api.repositories.ReplayRepository;
import de.needix.games.faf.replay.downloader.ReplayDownloader;
import de.needix.games.faf.replay.exceptions.ReplayNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/replays")
public class ReplayController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReplayController.class);

    private static final String HOTFOLDER_PATH = "hotfolder";
    private final ExecutorService asyncReplayAnalyserExecutorService = Executors.newFixedThreadPool(4);

    @Autowired
    private ReplayRepository replayRepository;

    @Operation(summary = "Upload a FAF replay file",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The replay file to upload. Must be a `.fafreplay` file.",
                    required = true,
                    content = @Content(mediaType = "multipart/form-data")
            ))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Replay uploaded successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Replay.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or file type",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadReplay(
            @Parameter(description = "The replay file to analyse. Has to be a .fafreplay file")
            @RequestParam MultipartFile file) {
        LOGGER.info("Received upload request for replay file {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file was uploaded.");
        }

        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".fafreplay")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file type. Only .fafreplay files are allowed.");
        }

        // Define a temporary directory to save uploaded files
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path tempFile;

        try {
            // Save the file temporarily
            tempFile = Files.createTempFile(tempDir, "replay_", ".fafreplay");
            file.transferTo(tempFile.toFile());
        } catch (IOException e) {
            LOGGER.error("Failed to save uploaded file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process the uploaded file.");
        }

        // Analyze the replay file and save to database
        Replay replay;
        try {
            replay = createDatabaseReplayEntity(tempFile.toFile());
        } catch (IOException e) {
            LOGGER.error("Failed to analyze and save replay: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to analyze or save replay data.");
        } finally {
            // Clean up temporary file
            try {
                Files.delete(tempFile);
            } catch (IOException e) {
                LOGGER.warn("Failed to delete temporary file: {}", tempFile);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(replay);
    }

    private Replay createDatabaseReplayEntity(File file) throws IOException {
        // Analyze the replay file
        Replay replay = new Replay();
        new ReplayAnalyser(file, replay).analyzeFAFReplay();

        long startTime = System.currentTimeMillis();
        // Save to repository
        try {
            replayRepository.save(replay);
        } catch (Exception e) {
            LOGGER.error("Failed to save replay to database: {}", e.getMessage(), e);
            throw e;
        }

        if (LOGGER.isDebugEnabled()) {
            long endTime = System.currentTimeMillis();
            LOGGER.info("Replay {} saved in {} ms", replay.getId(), endTime - startTime);
        }

        return replay;
    }

    @Operation(summary = "Retrieve all replay IDs with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page of replay IDs retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/ids")
    public ResponseEntity<Page<Long>> getAllReplayIds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        LOGGER.info("Received request for page {} of {} replay IDs", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Long> replayIdsPage = replayRepository.findReplayIds(pageable);
        return ResponseEntity.ok(replayIdsPage);
    }


    @Operation(summary = "Deletes all replays")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All replays deleted",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "403", description = "You are not allowed to do this", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/delete")
    public ResponseEntity<?> deleteAllReplays() {
        LOGGER.info("Received request to delete all replays");

        if (denyForceAnalyseAccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You don't have permission to delete replays.");
        }
        replayRepository.deleteAll();
        return ResponseEntity.ok("All replays deleted successfully.");
    }

    private boolean denyForceAnalyseAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .noneMatch(grantedAuthority ->
                            grantedAuthority.getAuthority().equals("ROLE_ADMIN") ||
                                    grantedAuthority.getAuthority().equals("REPLAY_FORCE_ACCESS")
                    );
        }
        return true;
    }

    @Operation(summary = "Returns all replays of a specific player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All replays of a specific player.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "404", description = "No replays for that player found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/player/{username}")
    public ResponseEntity<List<Replay>> getReplaysByPlayerName(
            @Parameter(description = "The username of the player", example = "Need")
            @PathVariable("username")
            String username) {
        LOGGER.info("Received request for replays of player '{}'", username);

        List<Replay> replays = replayRepository.findAllReplaysByPlayerName(username);
        if (replays.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(replays);
    }

    @Operation(summary = "Analyses a replay by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All replays of a specific player.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "403", description = "You are not allowed to do this", content = @Content),
            @ApiResponse(responseCode = "404", description = "No replays for that player found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{replayId}")
    public ResponseEntity<?> getReplayById(
            @Parameter(description = "The id of the replay", example = "21428000")
            @PathVariable("replayId")
            Long replayId,

            @Parameter(description = "To forcibly reanalyze the given replay, if it was already analyzed.",
                    example = "false")
            @RequestParam(value = "force", required = false, defaultValue = "false")
            boolean force) {
        LOGGER.info("Received request for replay with ID {}. Forcing reanalysis: {}", replayId, force);

        if (force) {
            // Dynamically check if the user has the required role/authority
            if (denyForceAnalyseAccess()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have permission to forcibly reanalyze replays.");
            }
            LOGGER.info("Forcibly reanalyzing replay with ID {}", replayId);
        } else {
            Optional<Replay> replayById = replayRepository.findById(replayId);
            if (replayById.isPresent()) {
                return ResponseEntity.ok(replayById.get());
            }
        }

        return downloadAndAnalyseReplay(replayId);
    }

    private ResponseEntity<?> downloadAndAnalyseReplay(Long replayId) {
        File file;
        try {
            file = ReplayDownloader.downloadReplay(replayId);
        } catch (ReplayNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Replay with ID " + replayId + " not found.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to download replay with ID " + replayId + ": " + e.getMessage());
        }

        try {
            return ResponseEntity.ok(createDatabaseReplayEntity(file));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to analyze replay with ID " + replayId + ": " + e.getMessage());
        }
    }

    @Operation(summary = "Analyses a replay by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All replays of a specific player.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "403", description = "You are not allowed to do this", content = @Content),
            @ApiResponse(responseCode = "404", description = "No replays for that player found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })

    @GetMapping("/range")
    public ResponseEntity<?> getReplaysByRange(
            @Parameter(description = "The start index", example = "21428000")
            @RequestParam("from")
            Long from,

            @Parameter(description = "The end index", example = "21428010")
            @RequestParam("to")
            Long to) {
        LOGGER.info("Received request for replays from {} to {}.", from, to);

        if (denyForceAnalyseAccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You don't have permission to forcibly reanalyze replays.");
        }

        LOGGER.info("Analyzing replays from {} to {}.", from, to);

        asyncReplayAnalyserExecutorService.submit(() -> {
                    for (long currentReplayId = from; currentReplayId <= to; currentReplayId++) {
                        try {
                            ResponseEntity<?> responseEntity = downloadAndAnalyseReplay(currentReplayId);
                            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                                LOGGER.warn("Failed to analyze replay with ID {}: {}", currentReplayId, responseEntity.getBody());
                            }
                        } catch (RuntimeException e) {
                            LOGGER.error("Failed to analyze replay with ID {}: {}", currentReplayId, e.getMessage());
                        }
                    }
                }
        );

        return ResponseEntity.ok("Replay processing started. This may take some time.");
    }

    @PostConstruct
    public void initHotfolderListener() {
        asyncReplayAnalyserExecutorService.submit(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                Path path = Paths.get(HOTFOLDER_PATH);
                path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

                while (true) {
                    WatchKey key = watchService.take(); // Wait for events
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            Path filePath = path.resolve((Path) event.context());
                            File file = filePath.toFile();

                            while (!isFileReady(file)) {
                                //noinspection BusyWait
                                Thread.sleep(100);
                            }

                            processReplayFileAsync(file);
                        }
                    }
                    if (!key.reset()) break;
                }
            } catch (IOException | InterruptedException e) {
                LOGGER.warn("Hotfolder listener terminated: {}", e.getMessage());
            }
        });
    }

    private boolean isFileReady(File file) {
        try (var fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE)) {
            // Try to lock the file
            FileLock lock = fileChannel.tryLock();
            if (lock != null) {
                lock.release(); // Release the lock immediately if it was successful
                return true;
            }
        } catch (IOException e) {
            // File is not ready (still locked)
        }
        return false;
    }

    @Async
    public void processReplayFileAsync(File file) {
        Objects.requireNonNull(file, "file must not be null");

        asyncReplayAnalyserExecutorService.submit(() -> {
            try {
                if (!file.exists() || !file.isFile() || !file.getName().endsWith(ReplayDownloader.FILE_EXTENSION)) {
                    LOGGER.error("Skipping invalid file: {}", file.getName());
                    return;
                }

                createDatabaseReplayEntity(file);

                LOGGER.debug("Successfully processed replay file: {}", file.getName());
                Files.delete(file.toPath());
            } catch (IOException e) {
                LOGGER.warn("Failed to process replay file: {}", e.getMessage());
            }
        });
    }

    @PostConstruct
    public void importInitialReplaysFromHotfolder() {
        asyncReplayAnalyserExecutorService.submit(() -> {
            File[] hotfolderFiles = new File(HOTFOLDER_PATH).listFiles();
            if (hotfolderFiles == null) return;
            for (File file : hotfolderFiles) {
                if (file.getName().endsWith(ReplayDownloader.FILE_EXTENSION)) {
                    processReplayFileAsync(file);
                }
            }
        });
    }
}