package de.needix.games.faf.replay.downloader;

import de.needix.games.faf.replay.exceptions.ReplayNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.concurrent.Semaphore;

public class ReplayDownloader {
    public static final String BASE_URL = "https://replay.faforever.com/";
    public static final String FILE_EXTENSION = ".fafreplay";
    public static final String BASE_DOWNLOAD_DIRECTORY = "/mnt/faf-replays";
    public static final long RETRY_DELAY_MS = 1000; // Delay in milliseconds between retries
    private static final int FILES_PER_FOLDER = 10000;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReplayDownloader.class);
    private static final Semaphore DOWNLOAD_SEMAPHORE = new Semaphore(1, false);

    public static File downloadReplay(long replayId, boolean overwriteExistingFile) throws IOException, ReplayNotFoundException {

        // Determine target folder based on the file number
        long folderNumber = replayId / FILES_PER_FOLDER;
        String targetDir = BASE_DOWNLOAD_DIRECTORY + "/" + "subfolder-" + folderNumber;

        ensureDirectoryExists(targetDir);

        String fileName = "replay-" + replayId + FILE_EXTENSION;
        File outputFile = new File(targetDir, fileName).getCanonicalFile();

        // Validate that outputFile is inside the base directory
        String baseCanonicalPath = new File(targetDir).getCanonicalPath();
        if (!outputFile.getPath().startsWith(baseCanonicalPath)) {
            throw new SecurityException("Path traversal attempt detected.");
        }

        if (outputFile.exists() && !overwriteExistingFile) {
            LOGGER.info("File {} already exists. Skipping download.", outputFile.getPath());
            return outputFile;
        }

        LOGGER.info("Downloading replay {} to {}", replayId, outputFile.getPath());


        String fileUrl = BASE_URL + replayId;
        LOGGER.info("Trying to download {}", fileUrl);
        URL url = new URL(fileUrl);
        int tries = 0;
        int maxTries = 5;

        while (true) {
            HttpURLConnection connection = null;
            try {
                DOWNLOAD_SEMAPHORE.acquireUninterruptibly();
                connection = (HttpURLConnection) url.openConnection();

                // Add headers to simulate a browser request
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36");
                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
                connection.setRequestProperty("Connection", "keep-alive");

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Download the file
                    try (BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
                         FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer, 0, 1024)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    LOGGER.info("Downloaded: {}", outputFile.getAbsolutePath());
                    return outputFile; // Exit the loop after a successful download

                } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                    // Handle redirect (302 or 301)
                    String newUrl = connection.getHeaderField("Location");
                    if (newUrl == null) {
                        throw new IOException("Redirect response without a Location header");
                    }
                    LOGGER.debug("Redirected to: {}", newUrl);
                    url = new URL(newUrl); // Update URL to the new location

                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    throw new ReplayNotFoundException(MessageFormat.format("The replay with id \"{0}\" was not available!", replayId));

                } else if (responseCode == 429) {
                    try {
                        // Add a delay between requests
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    throw new IOException("HTTP response code: " + responseCode);
                }
            } catch (IOException e) {
                LOGGER.warn("Failed to download replay {}: {}", replayId, e.getMessage());
                try {
                    // Add a delay between requests
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException e2) {
                    Thread.currentThread().interrupt();
                }
                if (tries++ > maxTries) {
                    throw e;
                }

            } finally {
                DOWNLOAD_SEMAPHORE.release();
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    private static void ensureDirectoryExists(String downloadDirectory) throws FileNotFoundException {
        File dir = new File(downloadDirectory);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new FileNotFoundException("Failed to create base download directory: " + downloadDirectory);
        }
    }
}
