package de.needix.games.faf.replay.analyser.eventanalyser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.needix.games.faf.replay.analyser.parser.Command;
import de.needix.games.faf.replay.api.entities.replay.Replay;
import de.needix.games.faf.replay.api.entities.summarystats.ReplayPlayerSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModeratorEventAnalyser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModeratorEventAnalyser.class);

    private final static Pattern GPG_NET_SEND_PATTERN = Pattern.compile(".*?'(.*?)'.*'(.*)'");

    private final Replay replayToFill;

    public ModeratorEventAnalyser(Replay replayToFill) {
        this.replayToFill = replayToFill;
    }

    public void handleModeratorEvent(Command command, Map<Object, Object> lua) {
        LOGGER.trace("ModeratorEvent: {}", lua);
        String message = lua.get("Message").toString();
        Object from = lua.get("From");

        if (!message.startsWith("GpgNetSend")) {
            return;
        }

        Matcher matcher = GPG_NET_SEND_PATTERN.matcher(message);
        if (!matcher.find() || matcher.groupCount() != 2) {
            LOGGER.warn("GpgNetSend message did not match pattern: {}", message);
            return;
        }

        String netSendName = matcher.group(1);
        String data = matcher.group(2);
        switch (netSendName) {
            case "JsonStats":
                handleJsonStats(data);
                break;
            case "GameEnded":
                handleGameEnded(data);
                break;
            case "GameResult":
                handleGameResult(data);
                break;
            case "EnforceRating":
                handleEnforceRating(data);
                break;
            default:
                LOGGER.warn("Unknown GpgNetSend command: {}", command);
                LOGGER.debug("GpgNetSend data: {}", data);
                break;
        }

        /*
            Message=Self-destructed 1 units, From=8.0
            Message=Created a marker with the text: 'anti tele', From=3.0
            Message=GpgNetSend with command 'EnforceRating' and data '', From=2.0
            Message=GpgNetSend with command 'GameResult' and data '4,defeat -10,', From=1.0
            Message=GpgNetSend with command 'JsonStats' and data '...', From=1.0
            Message=GpgNetSend with command 'GameEnded' and data '', From=12.0
         */
    }

    private void handleJsonStats(String jsonStats) {
        LOGGER.debug("JsonStats: {}", jsonStats);

        try {
            // Deserialize the incoming JSON string into a list of GameStatDTO
            ObjectMapper objectMapper = new ObjectMapper();
            List<ReplayPlayerSummary> replayPlayerSummaries = objectMapper.readValue(
                    objectMapper.readTree(jsonStats).get("stats").toString(),
                    new TypeReference<>() {
                    }
            );

            this.replayToFill.setPlayerScores(replayPlayerSummaries);

            LOGGER.info("Data successfully sent to REST API");
        } catch (Exception e) {
            LOGGER.error("Failed to process JsonStats: {}", e.getMessage(), e);
        }
    }

    private void handleGameEnded(String noData) {
        LOGGER.debug("GameEnded: {}", noData);
    }

    private void handleGameResult(String resultData) {
        LOGGER.debug("GameResult: {}", resultData);
    }

    private void handleEnforceRating(String noData) {
        LOGGER.debug("EnforceRating: {}", noData);
    }
}
