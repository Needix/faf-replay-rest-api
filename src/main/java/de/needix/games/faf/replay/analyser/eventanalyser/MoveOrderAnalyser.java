package de.needix.games.faf.replay.analyser.eventanalyser;

import de.needix.games.faf.replay.analyser.parser.Command;
import de.needix.games.faf.replay.analyser.parser.CommandParser;
import de.needix.games.faf.replay.analyser.parser.CommandType;
import de.needix.games.faf.replay.api.entities.order.TargetOrder;
import de.needix.games.faf.replay.api.entities.replay.Replay;

import java.util.Map;

public class MoveOrderAnalyser implements CommandAnalyser {
    private final Replay replay;

    public MoveOrderAnalyser(Replay replayToFill) {
        this.replay = replayToFill;
    }

    @Override
    public void analyseCommand(Command command) {
        if (command.getCommandType() != CommandType.ISSUE_COMMAND) {
            return;
        }

        Map<String, Object> commandData = command.getCommandData();
        Map<String, Object> completeCommandData = (Map<String, Object>) commandData.get("cmd_data");
        if (completeCommandData == null) {
            return;
        }

        CommandParser.CommandOrderType commandOrderType = (CommandParser.CommandOrderType) completeCommandData.get("command_type");

        Map<String, Object> targetData = (Map<String, Object>) completeCommandData.get("target");
        if (targetData == null) {
            return;
        }

        Map<String, Object> targetPositionData = (Map<String, Object>) targetData.get("position");
        if (targetPositionData == null) {
            return;
        }

        float x = (float) targetPositionData.get("x");
        float y = (float) targetPositionData.get("y");
        float z = (float) targetPositionData.get("z");

        TargetOrder targetOrder = TargetOrder.builder()
                .orderType(commandOrderType).targetX(x).targetY(y).targetZ(z).tick(command.getTick())
                .build();
        replay.getPlayers().get(command.getPlayerId() + 1).addTargetOrder(targetOrder);
    }

    @Override
    public void finalizeAnalysis() {

    }
}
