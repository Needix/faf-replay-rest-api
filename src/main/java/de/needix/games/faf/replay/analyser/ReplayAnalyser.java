package de.needix.games.faf.replay.analyser;

import com.github.luben.zstd.ZstdInputStream;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.needix.games.faf.replay.analyser.eventanalyser.LuaAnalyser;
import de.needix.games.faf.replay.analyser.parser.ReplayParser;
import de.needix.games.faf.replay.api.entities.replay.Replay;
import de.needix.games.faf.replay.exceptions.UnsupportedReplayException;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.zip.InflaterInputStream;

@ToString
public class ReplayAnalyser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReplayAnalyser.class);
    private final File file;
    private final Replay replayToFill;
    private final LuaAnalyser luaAnalyser;

    public ReplayAnalyser(File file, Replay replayToFill) throws IOException {
        this.file = Objects.requireNonNull(file, "file must not be null");
        this.replayToFill = Objects.requireNonNull(replayToFill, "replayToFill must not be null");
        this.luaAnalyser = new LuaAnalyser(replayToFill);
    }

    public void analyzeFAFReplay() throws IOException {
        long startTime = System.currentTimeMillis();

        String filePath = file.getAbsolutePath();
        if (!file.exists()) {
            LOGGER.error("File not found: {}", filePath);
            return;
        }

        LOGGER.info("Analyzing file: {}", filePath);
        LOGGER.info("File size: {} bytes", file.length());

        String jsonHeaderasString = getJsonHeader(file);
        JsonObject jsonHeader = getJsonHeaderAsJsonObject(jsonHeaderasString);

        int replayVersion = jsonHeader.get("version").getAsInt();
        String compression = jsonHeader.get("compression").getAsString();

        replayToFill.setId(jsonHeader.get("uid").getAsLong());
        replayToFill.setImportDate(new Date());
        replayToFill.setReplayCompression(compression);
        replayToFill.setReplayVersion(replayVersion);
        replayToFill.setReplayTitle(jsonHeader.get("title").getAsString());
        replayToFill.setComplete(jsonHeader.get("complete").getAsBoolean());
        replayToFill.setFeaturedMod(jsonHeader.get("featured_mod").getAsString());
        replayToFill.setGameType(jsonHeader.get("game_type").getAsString());
        replayToFill.setGameStart(jsonHeader.get("launched_at").getAsLong());
        replayToFill.setGameEnd(jsonHeader.get("game_end").getAsLong());
        replayToFill.setNumberOfPlayers(jsonHeader.get("num_players").getAsInt());
        replayToFill.setRecorder(jsonHeader.get("recorder").getAsString());

        // Read the rest of the file
        byte[] decompressedData;
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            inputStream.skip(jsonHeaderasString.length() + 1); // Skip the header and newline
            decompressedData = decompressData(file, inputStream, compression, replayVersion);
        }
        new ReplayParser(decompressedData, replayToFill);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Parsed replay in {} ms", System.currentTimeMillis() - startTime);
        }
    }

    private String getJsonHeader(File file) throws IOException {
        Objects.requireNonNull(file, "file must not be null");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
        }
    }

    private JsonObject getJsonHeaderAsJsonObject(String jsonHeader) {
        Objects.requireNonNull(jsonHeader, "jsonHeader must not be null");
        return JsonParser.parseString(jsonHeader).getAsJsonObject();
    }

    private byte[] decompressData(File file, InputStream inputStream, String compression, int version) throws IOException {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        Objects.requireNonNull(compression, "compression must not be null");

        InputStream decompressedStream;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        if ("zstd".equalsIgnoreCase(compression)) {
            LOGGER.debug("Decompressing with Zstandard...");
            decompressedStream = new ZstdInputStream(inputStream);
        } else if ("zlib".equalsIgnoreCase(compression) && version == 1) {
            LOGGER.debug("Decompressing with zlib...");
            decompressedStream = new InflaterInputStream(new Base64InputStream(file));
        } else {
            throw new UnsupportedReplayException("Unsupported compression or version: " + compression + ", version: " + version);
        }

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = decompressedStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        decompressedStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    // Helper class for Base64 decoding
    private static class Base64InputStream extends InputStream {
        private final Base64.Decoder decoder = Base64.getDecoder();
        private final File file;
        private ByteArrayInputStream decodedStream;

        public Base64InputStream(File file) {
            Objects.requireNonNull(file, "file must not be null");
            this.file = file;
        }

        @Override
        public int read() throws IOException {
            if (decodedStream == null || decodedStream.available() == 0) {
                byte[] buffer = Files.readAllBytes(file.toPath());
                if (buffer.length == 0) return -1;
                decodedStream = new ByteArrayInputStream(decoder.decode(buffer));
            }
            return decodedStream.read();
        }
    }
}

