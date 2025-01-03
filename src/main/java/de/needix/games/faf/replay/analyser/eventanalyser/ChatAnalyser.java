package de.needix.games.faf.replay.analyser.eventanalyser;

import de.needix.games.faf.replay.analyser.parser.Command;
import de.needix.games.faf.replay.api.entities.chat.ReplayChatMessage;
import de.needix.games.faf.replay.api.entities.replay.Replay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ChatAnalyser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatAnalyser.class);

    public static void handleChat(Command command, Replay replayToFill, Map<String, Object> message, String senderName) {
        LOGGER.trace("Chat message received: {}, from: {}", message, senderName);

        String to = message.get("to").toString(); // allies notify name
        String text = message.get("text").toString();

        ReplayChatMessage replayChatMessage = new ReplayChatMessage();
        replayChatMessage.setSender(senderName);
        replayChatMessage.setReceiver(to);
        replayChatMessage.setMessage(text);

        replayToFill.getChatMessages().add(replayChatMessage);
    }
}
