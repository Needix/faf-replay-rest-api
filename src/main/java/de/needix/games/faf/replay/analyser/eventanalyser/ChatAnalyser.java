package de.needix.games.faf.replay.analyser.eventanalyser;

import de.needix.games.faf.replay.analyser.parser.Command;
import de.needix.games.faf.replay.api.entities.chat.ReplayChatMessage;
import de.needix.games.faf.replay.api.entities.replay.Replay;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@ToString
public class ChatAnalyser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatAnalyser.class);

    public static void handleChat(Command command, Replay replayToFill, Map<String, Object> message, String senderName) {
        LOGGER.trace("Chat message received: {}, from: {}", message, senderName);

        String to = message.get("to").toString(); // allies notify name
        int playerId = command.getPlayerId();

        if (to.equals("all")) {
            if (playerId != 0) {
                return;
            }
        } else if (to.equals("ally") || to.equals("notify")) {
            if (playerId != 0 && playerId != 1) {
                return;
            }
        }

        String text = message.get("text").toString();

        ReplayChatMessage replayChatMessage = new ReplayChatMessage();
        replayChatMessage.setTick(command.getTick());
        replayChatMessage.setSender(senderName);
        replayChatMessage.setReceiver(to);
        replayChatMessage.setMessage(text);
        replayChatMessage.setMarker(message.get("camera") != null);

        replayToFill.getChatMessages().add(replayChatMessage);
    }
}
