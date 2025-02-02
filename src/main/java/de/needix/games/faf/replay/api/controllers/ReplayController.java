package de.needix.games.faf.replay.api.controllers;

import de.needix.games.faf.replay.analyser.ReplayAnalyser;
import de.needix.games.faf.replay.api.entities.replay.Replay;
import de.needix.games.faf.replay.api.repositories.ReplayRepository;
import de.needix.games.faf.replay.downloader.ReplayDownloader;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/replays")
public class ReplayController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReplayController.class);

    private static final String HOTFOLDER_PATH = "hotfolder";
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    @Autowired
    private ReplayRepository replayRepository;

    @GetMapping("/{replayId}")
    public ResponseEntity<?> getReplayById(
            @PathVariable("replayId") Long replayId,
            @RequestParam(value = "force", required = false, defaultValue = "false") boolean force) {
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

        File file;
        try {
            file = ReplayDownloader.downloadReplay(ReplayDownloader.DOWNLOAD_DIRECTORY, replayId);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to download replay with ID " + replayId + ": " + e.getMessage());
        }

        Replay createdReplay;
        try {
            createdReplay = createDatabaseReplayEntity(file);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to analyze replay with ID " + replayId + ": " + e.getMessage());
        }

        return ResponseEntity.ok(createdReplay);
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

    private Replay createDatabaseReplayEntity(File file) throws IOException {
        // Analyze the replay file
        Replay replay = new Replay();
        new ReplayAnalyser(file, replay).analyzeFAFReplay();

        // Save to repository
        try {
            replayRepository.save(replay);
        } catch (Exception e) {
            LOGGER.error("Failed to save replay to database: {}", e.getMessage(), e);
            throw e;
        }

        return replay;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadReplay(@RequestParam("file") MultipartFile file) {
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

    @GetMapping("/ids")
    public ResponseEntity<List<Long>> getAllReplayIds() {
        List<Long> replayIds = replayRepository.findAll().stream()
                .map(Replay::getId)
                .collect(Collectors.toList());
        return ResponseEntity.ok(replayIds);
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteAllReplays() {
        if (denyForceAnalyseAccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You don't have permission to delete replays.");
        }
        replayRepository.deleteAll();
        return ResponseEntity.ok("All replays deleted successfully.");
    }

    @PostConstruct
    public void initHotfolderListener() {
        executorService.submit(() -> {
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

        executorService.submit(() -> {
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
        executorService.submit(() -> {
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