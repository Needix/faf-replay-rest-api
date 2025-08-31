package de.needix.games.faf.replay.analyser.eventanalyser;

import de.needix.games.faf.replay.analyser.parser.Command;
import de.needix.games.faf.replay.api.entities.chat.ReplayChatMessage;
import de.needix.games.faf.replay.api.entities.replay.Replay;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@ToString
public class ChatAnalyser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatAnalyser.class);

    public static void handleChat(Command command, Replay replayToFill, Map<String, Object> message, String senderName) {
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

        List<ReplayChatMessage> chatMessages = replayToFill.getChatMessages();
        if (!chatMessages.isEmpty()) {
            ReplayChatMessage lastChatMessage = chatMessages.get(chatMessages.size() - 1);
            if (lastChatMessage.getMessage().equals(text) && lastChatMessage.getSender().equals(senderName) && lastChatMessage.getReceiver().equals(to)) {
                // For some reason (probably p2p stuff) chat messages are duplicated with slightly different ticks. Ignore duplicated messages
                return;
            }
        }

        ReplayChatMessage replayChatMessage = new ReplayChatMessage();
        replayChatMessage.setTick(command.getTick());
        replayChatMessage.setSender(senderName);
        replayChatMessage.setReceiver(to);
        replayChatMessage.setMessage(text);
        replayChatMessage.setMarker(message.get("camera") != null);

        chatMessages.add(replayChatMessage);

        LOGGER.debug("Created chat message: {}", replayChatMessage);
    }
}
