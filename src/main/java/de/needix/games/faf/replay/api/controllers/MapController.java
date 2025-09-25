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
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
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
        if (mapName == null || mapName.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        if (mapName.startsWith(GENERATED_MAP_PREFIX) || mapName.startsWith(COOP_MAP_PREFIX)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }

        // Normalize the map name to lowercase and remove dangerous characters
        String normalizedMapName = sanitizeMapName(mapName);
        if (normalizedMapName == null) {
            return ResponseEntity.badRequest().body(null);
        }

        Path cachedImagePath = null;
        try {
            Path cacheDir = Paths.get(CACHE_DIR).toAbsolutePath().normalize();
            Files.createDirectories(cacheDir); // Ensure the cache directory exists

            cachedImagePath = cacheDir.resolve(normalizedMapName).normalize();
            // Verify that the resolved path is still within our cache directory
            if (!cachedImagePath.startsWith(cacheDir)) {
                return ResponseEntity.badRequest().body(null);
            }

            if (Files.exists(cachedImagePath)) {
                // Map was already checked earlier and could not be downloaded. We cache that it does not exist
                if (cachedImagePath.toFile().length() == 0) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(null);
                }

                // If a cached image exists, return it
                Resource resource = new FileSystemResource(cachedImagePath.toFile());
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(resource);
            }

            // If not cached, download from an external source and save to cache
            String externalURL = EXTERNAL_MAP_SOURCE_URL + normalizedMapName;
            if (!isUrlAllowed(externalURL)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }

            URLConnection connection = new URL(externalURL).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            try (InputStream inputStream = connection.getInputStream()) {

                Files.copy(inputStream, cachedImagePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Serve the newly cached image
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

    /**
     * Sanitizes and validates the map name to prevent path traversal attacks.
     *
     * @param mapName The raw map name from the request
     * @return The sanitized map name or null if invalid
     */
    private String sanitizeMapName(String mapName) {
        // Remove any path separators and dangerous characters
        String normalized = mapName.toLowerCase()
                .replace(".scmap", "")
                .replaceAll("[^a-z0-9._-]", "");

        // Ensure the final name ends with .png
        normalized = normalized + ".png";

        // Additional validations
        if (normalized.contains("..") ||
                normalized.startsWith(".") ||
                normalized.length() > 255 ||
                normalized.length() < 5) {
            return null;
        }

        return normalized;
    }

    private boolean isUrlAllowed(String urlString) {
        try {
            URL url = new URL(urlString);
            String host = url.getHost().toLowerCase();

            // Only allow HTTPS
            if (!url.getProtocol().equals("https")) {
                LOGGER.warn("Rejected URL due to non-HTTPS protocol: {}", urlString);
                return false;
            }

            // Whitelist of allowed domains
            String[] allowedDomains = {
                    "content.faforever.com"
            };

            boolean isAllowedDomain = false;
            for (String domain : allowedDomains) {
                if (host.equals(domain) || host.endsWith("." + domain)) {
                    isAllowedDomain = true;
                    break;
                }
            }

            if (!isAllowedDomain) {
                LOGGER.warn("Rejected URL with non-whitelisted domain: {}", urlString);
                return false;
            }

            // Reject if hostname resolves to local/private IP
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress addr : addresses) {
                if (addr.isAnyLocalAddress() ||
                        addr.isLoopbackAddress() ||
                        addr.isLinkLocalAddress() ||
                        addr.isSiteLocalAddress()) {
                    LOGGER.warn("Rejected URL resolving to private/local IP: {}", urlString);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Error validating URL: {}", urlString, e);
            return false;
        }
    }

}