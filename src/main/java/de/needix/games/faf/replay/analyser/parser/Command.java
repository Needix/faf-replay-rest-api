package de.needix.games.faf.replay.analyser.parser;

import lombok.Getter;

import java.util.Map;

/**
 * Created by Need on 01.01.2025.
 */
@Getter
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

    @Override
    public String toString() {
        return "Command{" +
                "tick=" + tick +
                ", commandType=" + commandType +
                ", playerId=" + playerId +
                ", commandData=" + commandData +
                ", isDesyncCommand=" + isDesyncCommand +
                '}';
    }
}
