package de.needix.games.faf.replay.analyser.parser;

import lombok.ToString;

/**
 * Created by Need on 31.12.2024.
 */
@ToString
public enum CommandType {
    ADVANCE,
    SET_COMMAND_SOURCE,
    COMMAND_SOURCE_TERMINATED,
    VERIFY_CHECKSUM,
    REQUEST_PAUSE,
    RESUME,
    SINGLE_STEP,
    CREATE_UNIT,
    CREATE_PROP,
    DESTROY_UNIT,
    WARP_ENTITY,
    PROCESS_INFO_PAIR,
    ISSUE_COMMAND,
    ISSUE_FACTORY_COMMAND,
    INCREASE_COMMAND_COUNT,
    DECREASE_COMMAND_COUNT,
    SET_COMMAND_TARGET,
    SET_COMMAND_TYPE,
    SET_COMMAND_CELLS,
    REMOVE_COMMAND_FROM_QUEUE,
    DEBUG_COMMAND,
    EXECUTE_LUA_IN_SIM,
    LUA_SIM_CALLBACK,
    END_GAME;

    private static final CommandType[] VALUES = values();

    public static CommandType getFromIndex(int index) {
        return VALUES[index];
    }
}
