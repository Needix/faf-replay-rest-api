package de.needix.games.faf.replay.analyser.parser;


import de.needix.games.faf.replay.analyser.eventanalyser.CommandAnalyser;
import de.needix.games.faf.replay.api.entities.replay.Replay;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@ToString
public class ReplayBody {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReplayBody.class);

    private final ReplayParser replayReader;
    private final Replay replayToFill;

    private int lastDesyncTick;
    private int tick;
    private int playerId;

    private int previousTick;
    private String previousChecksum;

    public ReplayBody(ReplayParser reader, Replay replayToFill) {
        this.replayReader = Objects.requireNonNull(reader, "reader must not be null");
        this.replayToFill = Objects.requireNonNull(replayToFill, "replayToFill must not be null");

        this.tick = 0;
        this.lastDesyncTick = -1;
        this.playerId = -1;

        this.previousTick = -1;
        this.previousChecksum = null;
    }

    public void parse(List<CommandAnalyser> commandConsumers) {
        int replayBufferSize = replayReader.size();
        while (replayReader.offset() + 3 <= replayBufferSize) {
            Command command = parseCommandAndGetData();
            commandConsumers.forEach(consumer -> consumer.analyseCommand(command));
        }
        commandConsumers.forEach(CommandAnalyser::finalizeAnalysis);
        LOGGER.debug("Parsed all commands");
    }

    private Command parseCommandAndGetData() {
        byte[] commandTypeByte = replayReader.read(1);
        byte[] commandLengthByte = replayReader.read(2);

        CommandType command = CommandType.getFromIndex(Byte.toUnsignedInt(commandTypeByte[0]));
        int commandLength = Byte.toUnsignedInt(commandLengthByte[1]) << 8 | Byte.toUnsignedInt(commandLengthByte[0]);

        int bytesToRead = commandLength - 3;
        int lastOffset = replayReader.offset();

        Command parsedNextCommand = parseNextCommand(command);
        int currentOffset = replayReader.offset();
        int bytesRead = currentOffset - lastOffset;
        if (bytesRead < bytesToRead) {
            int bytesToSkip = bytesToRead - bytesRead;
            LOGGER.debug("Command {} was expected to read {} bytes, but read {} bytes. Skipping {} bytes.", parsedNextCommand, bytesToRead, bytesRead, bytesToSkip);
            byte[] skippedBytesArray = replayReader.read(bytesToSkip);
            LOGGER.debug("Skipped {} bytes: String: \"{}\"; Raw bytes: {}", bytesToSkip, new String(skippedBytesArray), skippedBytesArray);

        } else if (bytesRead > bytesToRead) {
            LOGGER.error("Command {} was expected to read {} bytes, but read {} bytes. This is most likely a bug.", parsedNextCommand, bytesToRead, bytesRead);

        }
        return parsedNextCommand;
    }

    private Command parseNextCommand(CommandType command) {
        Map<String, Object> parsedCommandData = CommandParser.parseCommand(command, replayReader);
        return processCommand(command, parsedCommandData);
    }

    private Command processCommand(CommandType commandType, Map<String, Object> commandData) {
        lastDesyncTick = -1;

        if (commandType == CommandType.ADVANCE) {
            tick += (int) commandData.get("advance");
        } else if (commandType == CommandType.SET_COMMAND_SOURCE) {
            playerId = (int) commandData.get("player_id");
        } else if (commandType == CommandType.COMMAND_SOURCE_TERMINATED) {
            playerId = -1;
        } else if (commandType == CommandType.VERIFY_CHECKSUM) {
            String checksum = commandData.get("checksum").toString();
            int tickValue = (int) commandData.get("tick");
            if (tickValue == previousTick && !Objects.equals(checksum, previousChecksum)) {
                lastDesyncTick = tick;
            }
            previousTick = tickValue;
            previousChecksum = checksum;
        }

        return new Command(tick, lastDesyncTick != 1, commandType, playerId, commandData);
    }
}
