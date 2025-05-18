package de.needix.games.faf.replay.api.controllers;

import org.slf4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/v1/maps")
public class MapController {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MapController.class);

    private static final String CACHE_DIR = "cached-maps"; // Directory for cached images
    private static final String EXTERNAL_MAP_SOURCE_URL = "https://content.faforever.com/maps/previews/large/";
    private static final String GENERATED_MAP_PREFIX = "neroxis_map_generator_";
    private static final String COOP_MAP_PREFIX = "faf_coop_operation";

    @GetMapping("/preview")
    public ResponseEntity<Resource> getMapPreview(@RequestParam String mapName) {
        if (mapName.startsWith(GENERATED_MAP_PREFIX) || mapName.startsWith(COOP_MAP_PREFIX)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }

        Path cachedImagePath = null;
        try {
            // Normalize map name to lowercase and remove dangerous characters
            String normalizedMapName = mapName.toLowerCase().replace(".scmap", "") + ".png";
            Path cacheDir = Paths.get(CACHE_DIR);
            Files.createDirectories(cacheDir); // Ensure the cache directory exists
            cachedImagePath = cacheDir.resolve(normalizedMapName);

            if (Files.exists(cachedImagePath)) {
                // Map was already checked earlier and could not be downloaded. We cache that it does not exists
                if (cachedImagePath.toFile().length() == 0) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(null);
                }

                // If cached image exists, return it
                Resource resource = new FileSystemResource(cachedImagePath.toFile());
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(resource);
            }

            // If not cached, download from external source and save to cache
            String externalURL = EXTERNAL_MAP_SOURCE_URL + normalizedMapName;
            try (InputStream inputStream = new URL(externalURL).openStream()) {
                Files.copy(inputStream, cachedImagePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Serve the newly-cached image
            Resource resource = new FileSystemResource(cachedImagePath.toFile());
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);

        } catch (IOException e) {
            if (cachedImagePath != null) {
                try {
                    Files.createFile(cachedImagePath);
                } catch (IOException ex) {
                    LOGGER.debug("Failed to create cached image file: {}", ex.getMessage());
                }
            }

            // Handle cases where the image can't be downloaded or served
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}