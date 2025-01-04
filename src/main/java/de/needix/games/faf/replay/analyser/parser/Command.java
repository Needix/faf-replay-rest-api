package de.needix.games.faf.replay.analyser.parser;

import lombok.Getter;
import lombok.ToString;

import java.util.Map;

/**
 * Created by Need on 01.01.2025.
 */
@Getter
@ToString
public class Command {
    private final int tick;
    private final CommandType commandType;
    private final int playerId;
    private final Map<String, Object> commandData;
    private final boolean isDesyncCommand;

    public Command(int tick, boolean isDesyncCommand, CommandType commandType, int playerId, Map<String, Object> commandData) {
        this.tick = tick;
        this.isDesyncCommand = isDesyncCommand;
        this.commandType = commandType;
        this.playerId = playerId;
        this.commandData = commandData;
    }
}
