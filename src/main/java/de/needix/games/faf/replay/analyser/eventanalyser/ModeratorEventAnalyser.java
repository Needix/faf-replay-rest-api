package de.needix.games.faf.replay.analyser.eventanalyser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.needix.games.faf.replay.analyser.parser.Command;
import de.needix.games.faf.replay.api.entities.replay.Replay;
import de.needix.games.faf.replay.api.entities.summarystats.ReplayPlayerSummary;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ToString
public class ModeratorEventAnalyser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModeratorEventAnalyser.class);

    private final static Pattern GPG_NET_SEND_PATTERN = Pattern.compile(".*?'(.*?)'.*'(.*)'");
    private final static Pattern GAME_RESULT_PATTERN = Pattern.compile(".*?(\\d+),(.*?) (-*)(\\d+),(.*)");

    private final Replay replayToFill;

    public ModeratorEventAnalyser(Replay replayToFill) {
        this.replayToFill = replayToFill;
    }

    public void handleModeratorEvent(Command command, Map<Object, Object> lua) {
        LOGGER.trace("ModeratorEvent: {}", lua);
        String message = lua.get("Message").toString();
        Object from = lua.get("From");

        if (message.startsWith("GpgNetSend")) {
            analyseGpgNetSendMessage(command, message);
        } else if (message.startsWith("Created a marker with the text: '")) {
            // TODO:
            String text = message.substring(33, message.length() - 1);
//            ChatAnalyser.handleChat(command, replayToFill, Map.of("text", text, "to", "all"), from.toString());

        } else if (message.startsWith("Created a ping of type ")) {
            // TODO:
            String pingType = message.substring(24, message.length() - 1);
        } else if (message.startsWith("Self-destructed ")) {
            // TODO:
            // Self-destructed 1 units
        } else if (message.startsWith("Switched focus army")) {
            // TODO:
            // Switched focus army from 4 to -1!
        } else if (message.startsWith("Is changing focus army")) {
            // TODO:
            // Ignoring moderator event message which does not start with 'GpgNetSend': Is changing focus army from 10 to 7 via ConExecute!

        } else {
            LOGGER.warn("Ignoring moderator event message which does not start with 'GpgNetSend': {}", message);
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

    private void analyseGpgNetSendMessage(Command command, String message) {
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
            case "OperationComplete":
                // TODO: Unknown GpgNetSend command: Command(tick=34957, commandType=CommandType.LUA_SIM_CALLBACK, playerId=2, commandData={lua={Message=GpgNetSend with command 'OperationComplete' and data 'true,true,00:58:15,', From=5.0}, type=lua_sim_callback, lua_name=ModeratorEvent}, isDesyncCommand=true)
                break;
            case "TeamkillHappened":
                // TODO: Unknown GpgNetSend command: Command(tick=53439, commandType=CommandType.LUA_SIM_CALLBACK, playerId=0, commandData={lua={Message=GpgNetSend with command 'TeamkillHappened' and data '5343.3999023438,507954,Dude_a_chu,507953,Shinsidious,', From=1.0}, type=lua_sim_callback, lua_name=ModeratorEvent}, isDesyncCommand=true)
                break;
            default:
                LOGGER.warn("Unknown GpgNetSend command: {}", command);
                LOGGER.debug("GpgNetSend data: {}", data);
                break;
        }
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

            replayPlayerSummaries.forEach(replayPlayerSummary -> replayPlayerSummary.setId(this.replayToFill.getId() + "_" + replayPlayerSummary.getName()));
            replayPlayerSummaries.forEach(replayPlayerSummary -> replayPlayerSummary.setReplay(this.replayToFill));

            Set<ReplayPlayerSummary> uniqueReplayPlayerSummaries = new LinkedHashSet<>(replayPlayerSummaries);

            LOGGER.debug("Deserialized replay player summaries: {}", uniqueReplayPlayerSummaries);

            this.replayToFill.setPlayerScores(uniqueReplayPlayerSummaries);
        } catch (Exception e) {
            LOGGER.error("Failed to process JsonStats \"{}\"! Exception: {}", jsonStats, e.getMessage(), e);
        }
    }

    private void handleGameEnded(String noData) {
        LOGGER.debug("GameEnded: {}", noData);
    }

    private void handleGameResult(String resultData) {
        LOGGER.debug("GameResult: {}", resultData);

        Matcher matcher = GAME_RESULT_PATTERN.matcher(resultData);
        if (!matcher.find()) {
            LOGGER.warn("GameResult did not match pattern: {}", resultData);
            return;
        }
        int playerId = Integer.parseInt(matcher.group(1));
        if (playerId < 1 || playerId > this.replayToFill.getPlayers().size()) {
            return;
        }

        String victoryOrDefeat = matcher.group(2);
        this.replayToFill.getPlayers().get(playerId - 1).setVictory("victory".equals(victoryOrDefeat));
    }

    private void handleEnforceRating(String noData) {
        LOGGER.debug("EnforceRating: {}", noData);
    }
}
