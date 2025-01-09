package de.needix.games.faf.replay.analyser.eventanalyser;

import de.needix.games.faf.replay.analyser.parser.Command;

public interface CommandAnalyser {
    void analyseCommand(Command command);

    void finalizeAnalysis();
}
