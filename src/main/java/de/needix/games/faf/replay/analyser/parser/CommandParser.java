package de.needix.games.faf.replay.analyser.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CommandParser {
    private static final Logger logger = LoggerFactory.getLogger(CommandParser.class);

    private static final Map<CommandType, CommandFunction> COMMAND_PARSERS = new HashMap<>();

    static {
        COMMAND_PARSERS.put(CommandType.ADVANCE, CommandParser::commandAdvance);
        COMMAND_PARSERS.put(CommandType.SET_COMMAND_SOURCE, CommandParser::commandSetCommandSource);
        COMMAND_PARSERS.put(CommandType.COMMAND_SOURCE_TERMINATED, CommandParser::commandSourceTerminated);
        COMMAND_PARSERS.put(CommandType.VERIFY_CHECKSUM, CommandParser::commandVerifyChecksum);
        COMMAND_PARSERS.put(CommandType.REQUEST_PAUSE, CommandParser::commandRequestPause);
        COMMAND_PARSERS.put(CommandType.RESUME, CommandParser::commandResume);
        COMMAND_PARSERS.put(CommandType.SINGLE_STEP, CommandParser::commandSingleStep);
        COMMAND_PARSERS.put(CommandType.CREATE_UNIT, CommandParser::commandCreateUnit);
        COMMAND_PARSERS.put(CommandType.CREATE_PROP, CommandParser::commandCreateProp);
        COMMAND_PARSERS.put(CommandType.DESTROY_UNIT, CommandParser::commandDestroyEntity);
        COMMAND_PARSERS.put(CommandType.WARP_ENTITY, CommandParser::commandWarpEntity);
        COMMAND_PARSERS.put(CommandType.PROCESS_INFO_PAIR, CommandParser::commandProcessInfoPair);
        COMMAND_PARSERS.put(CommandType.ISSUE_COMMAND, CommandParser::commandIssue);
        COMMAND_PARSERS.put(CommandType.ISSUE_FACTORY_COMMAND, CommandParser::commandFactoryIssue);
        COMMAND_PARSERS.put(CommandType.INCREASE_COMMAND_COUNT, CommandParser::commandCommandCountIncrease);
        COMMAND_PARSERS.put(CommandType.DECREASE_COMMAND_COUNT, CommandParser::commandCommandCountDecrease);
        COMMAND_PARSERS.put(CommandType.SET_COMMAND_TARGET, CommandParser::commandSetCommandTarget);
        COMMAND_PARSERS.put(CommandType.SET_COMMAND_TYPE, CommandParser::commandSetCommandType);
        COMMAND_PARSERS.put(CommandType.SET_COMMAND_CELLS, CommandParser::commandSetCommandCells);
        COMMAND_PARSERS.put(CommandType.REMOVE_COMMAND_FROM_QUEUE, CommandParser::commandRemoveFromQueue);
        COMMAND_PARSERS.put(CommandType.DEBUG_COMMAND, CommandParser::commandDebugCommand);
        COMMAND_PARSERS.put(CommandType.EXECUTE_LUA_IN_SIM, CommandParser::commandExecuteLuaInSim);
        COMMAND_PARSERS.put(CommandType.LUA_SIM_CALLBACK, CommandParser::commandLuaSimCallback);
        COMMAND_PARSERS.put(CommandType.END_GAME, CommandParser::commandEndGame);
    }

    public static Map<String, Object> parseCommand(CommandType command, ReplayParser reader) {
        CommandFunction commandFunction = COMMAND_PARSERS.get(command);
        if (commandFunction == null) {
            logger.debug("Command not handled by parser: " + command);
            return Collections.emptyMap();
        }
        return commandFunction.parse(reader);
    }

    // Command parser methods
    public static Map<String, Object> commandAdvance(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "advance");
        result.put("advance", reader.readUnsignedInt());
        return result;
    }

    public static Map<String, Object> commandSetCommandSource(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "set_command_source");
        result.put("player_id", reader.readByte());
        return result;
    }

    public static Map<String, Object> commandSourceTerminated(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "command_source_terminated");
        return result;
    }

    public static Map<String, Object> commandVerifyChecksum(ReplayParser reader) {
        StringBuilder checksum = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            checksum.append(String.format("%02X", reader.readByte()));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("type", "verify_checksum");
        result.put("checksum", checksum.toString());
        result.put("tick", reader.readUnsignedInt());
        return result;
    }

    public static Map<String, Object> commandRequestPause(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "request_pause");
        return result;
    }

    public static Map<String, Object> commandResume(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "resume");
        return result;
    }

    public static Map<String, Object> commandSingleStep(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "single_step");
        return result;
    }

    public static Map<String, Object> commandCreateUnit(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "create_unit");
        result.put("army_index", reader.readByte());
        result.put("blueprint_id", reader.readString());
        result.put("vector", readVector(reader));
        return result;
    }

    private static Map<String, Object> readVector(ReplayParser reader) {
        Map<String, Object> vector = new HashMap<>();
        vector.put("x", reader.readFloat());
        vector.put("y", reader.readFloat());
        vector.put("z", reader.readFloat());
        return vector;
    }

    public static Map<String, Object> commandCreateProp(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "create_prop");
        result.put("name", reader.readString());
        result.put("vector", readVector(reader));
        return result;
    }

    public static Map<String, Object> commandDestroyEntity(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "destroy_entity");
        result.put("entity_id", reader.readInt());
        return result;
    }

    public static Map<String, Object> commandWarpEntity(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "warp_entity");
        result.put("entity_id", reader.readInt());
        result.put("vector", readVector(reader));
        return result;
    }

    public static Map<String, Object> commandProcessInfoPair(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "process_info_pair");
        result.put("entity_id", reader.readInt());
        result.put("arg1", reader.readString());
        result.put("arg2", reader.readString());
        return result;
    }

    public static Map<String, Object> commandIssue(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "issue");
        result.put("entity_ids_set", parseEntityIdsSet(reader));
        result.put("cmd_data", parseCommandData(reader));
        return result;
    }

    private static Map<String, Object> parseEntityIdsSet(ReplayParser reader) {
        int unitsNumber = reader.readUnsignedInt();
        List<Integer> unitIds = new ArrayList<>();
        for (int i = 0; i < unitsNumber; i++) {
            unitIds.add(reader.readUnsignedInt());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("units_number", unitsNumber);
        result.put("unit_ids", unitIds);
        return result;
    }

    private static Map<String, Object> parseCommandData(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("command_id", reader.readInt());
        result.put("arg1", reader.readInt());
        result.put("command_type", reader.readByte());
        result.put("arg2", reader.readInt());
        result.put("target", parseTarget(reader));
        result.put("arg3", reader.readBool());
        result.put("formation", parseFormation(reader));
        result.put("blueprint_id", reader.readString());
        result.put("arg4", reader.read(12));
        result.put("arg5", null);
        result.put("cells", reader.readLua(null));
        return result;
    }

    private static Map<String, Object> parseTarget(ReplayParser reader) {
        int target = reader.readByte();
        Map<String, Object> result = new HashMap<>();
        result.put("target", target);
        if (target == TargetType.ENTITY) {
            result.put("entity_id", reader.readInt());
        } else if (target == TargetType.POSITION) {
            result.put("position", readVector(reader));
        }
        return result;
    }

    private static Map<String, Object> parseFormation(ReplayParser reader) {
        int formation = reader.readInt();
        if (formation != -1) {
            Map<String, Object> result = new HashMap<>();
            result.put("w", reader.readFloat());
            result.put("position", readVector(reader));
            result.put("scale", reader.readFloat());
            return result;
        }
        return null;
    }

    public static Map<String, Object> commandFactoryIssue(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "factory_issue");
        result.put("entity_ids_set", parseEntityIdsSet(reader));
        result.put("cmd_data", parseCommandData(reader));
        return result;
    }

    public static Map<String, Object> commandCommandCountIncrease(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "command_count_increase");
        result.put("command_id", reader.readUnsignedInt());
        result.put("delta", reader.readInt());
        return result;
    }

    public static Map<String, Object> commandCommandCountDecrease(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "command_count_decrease");
        result.put("command_id", reader.readUnsignedInt());
        result.put("delta", reader.readInt());
        return result;
    }

    public static Map<String, Object> commandSetCommandTarget(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "set_command_target");
        result.put("command_id", reader.readUnsignedInt());
        result.put("target", parseTarget(reader));
        return result;
    }

    public static Map<String, Object> commandSetCommandType(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "set_command_type");
        result.put("command_id", reader.readUnsignedInt());
        result.put("target_id", reader.readInt());
        return result;
    }

    public static Map<String, Object> commandSetCommandCells(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "set_command_cells");
        result.put("command_id", reader.readUnsignedInt());
        result.put("cells", reader.readLua(null));
        result.put("vector", readVector(reader));
        return result;
    }

    public static Map<String, Object> commandRemoveFromQueue(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "remove_from_queue");
        result.put("command_id", reader.readUnsignedInt());
        result.put("unit_id", reader.readInt());
        return result;
    }

    public static Map<String, Object> commandDebugCommand(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "debug_command");
        result.put("debug_command", reader.readString());
        result.put("vector", readVector(reader));
        result.put("focus_army_index", reader.readByte());
        result.put("entity_ids_set", parseEntityIdsSet(reader));
        return result;
    }

    public static Map<String, Object> commandExecuteLuaInSim(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "execute_lua_in_sim");
        result.put("lua", reader.readString());
        return result;
    }

    public static Map<String, Object> commandLuaSimCallback(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "lua_sim_callback");
        result.put("lua_name", reader.readString());
        result.put("lua", reader.readLua(null));
        return result;
    }

    public static Map<String, Object> commandEndGame(ReplayParser reader) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "end_game");
        return result;
    }

    // Additional helper methods for parsing targets, formations, etc., can be added here.

    public interface CommandFunction {
        Map<String, Object> parse(ReplayParser reader);
    }

    private static class TargetType {
        public static final int ENTITY = 1;
        public static final int POSITION = 2;
    }
}
