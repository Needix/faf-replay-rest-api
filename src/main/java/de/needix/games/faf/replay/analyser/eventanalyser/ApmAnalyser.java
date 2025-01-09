package de.needix.games.faf.replay.analyser.eventanalyser;

import de.needix.games.faf.replay.analyser.parser.Command;
import de.needix.games.faf.replay.analyser.parser.CommandType;
import de.needix.games.faf.replay.api.entities.replay.Replay;
import de.needix.games.faf.replay.api.entities.replay.ReplayPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApmAnalyser implements CommandAnalyser {

    private final Replay replay;
    private final Map<Integer, List<Integer>> playerTickActions = new HashMap<>();
    private final long realTimeDurationInSeconds;

    public ApmAnalyser(Replay replay) {
        this.replay = replay;
        this.realTimeDurationInSeconds = replay.getGameEnd() - replay.getGameStart();
    }

    @Override
    public void analyseCommand(Command command) {
        int playerId = command.getPlayerId();
        int tick = command.getTick();

        if (command.getCommandType() == CommandType.SET_COMMAND_SOURCE
                || command.getCommandType() == CommandType.ADVANCE
                || command.getCommandType() == CommandType.VERIFY_CHECKSUM
                || command.getCommandType() == CommandType.COMMAND_SOURCE_TERMINATED) {
            return;
        }

        // Update the playerTickActions map with the tick where the action occurred
        playerTickActions
                .computeIfAbsent(playerId, k -> new ArrayList<>())
                .add(tick);

        // Capture the last tick from the last parsed command
        if (command.getCommandType() == CommandType.END_GAME) {
            calculateFinalApm(tick);
        }
    }

    private void calculateFinalApm(int lastTick) {
        // After parsing, calculate ticksPerMinute
        double ticksPerMinute = (double) lastTick / (realTimeDurationInSeconds / 60.0);

        // Map to store APM buckets for each player
        Map<Integer, List<Double>> playerApms = new HashMap<>();

        // Loop over each player's raw tick list and bucket their actions into minutes
        for (Map.Entry<Integer, List<Integer>> entry : playerTickActions.entrySet()) {
            int playerId = entry.getKey();
            List<Integer> playerTicks = entry.getValue();

            // Prepare APM buckets (default all 0 APMs initially)
            int totalMinutes = (int) Math.ceil((double) lastTick / ticksPerMinute);
            int[] actionsPerMinute = new int[totalMinutes];

            // Populate actions per minute bucket
            for (int tick : playerTicks) {
                int currentMinute = (int) (tick / ticksPerMinute);
                actionsPerMinute[currentMinute]++;
            }

            // Convert actions to APM and store them
            List<Double> apms = new ArrayList<>();
            for (int actions : actionsPerMinute) {
                double apm = (double) actions * 60 / (ticksPerMinute / 10); // Scale actions to APM
                apms.add(apm);
            }

            playerApms.put(playerId, apms);
        }

        // Persist the calculated APMs back to ReplayPlayer objects
        for (ReplayPlayer player : replay.getPlayers().values()) {
            int playerId = player.getPlayerId();
            List<Double> playerApmsList = playerApms.getOrDefault(playerId, new ArrayList<>());
            for (int minute = 0; minute < playerApmsList.size(); minute++) {
                player.addApmPerMinute(minute, playerApmsList.get(minute));
            }
        }
    }

    @Override
    public void finalizeAnalysis() {

    }
}
