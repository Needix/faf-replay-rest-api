package de.needix.games.faf.replay.analyser.eventanalyser;

import de.needix.games.faf.replay.analyser.parser.Command;
import de.needix.games.faf.replay.analyser.parser.CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CommandLogger implements CommandAnalyser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLogger.class);
    private static final Set<CommandType> COMMAND_TYPE_NOT_TO_LOG = new HashSet<>(
            Arrays.asList(CommandType.ADVANCE, CommandType.SET_COMMAND_SOURCE));

    @Override
    public void analyseCommand(Command command) {
        if (LOGGER.isDebugEnabled() && !COMMAND_TYPE_NOT_TO_LOG.contains(command.getCommandType())) {
            LOGGER.debug("Command: {}", command);
        }
    }

    @Override
    public void finalizeAnalysis() {

    }
}
