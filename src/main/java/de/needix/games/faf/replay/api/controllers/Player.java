package de.needix.games.faf.replay.api.controllers;

import java.util.Date;
import java.util.Map;

public class Player {
    private String name;

    private Map<String, Integer> playedGameTypeCount;
    private Map<String, Integer> wonGameTypeCount;

    private Map<Integer, Integer> playedColorCount;

    private Map<Date, String> playerNameHistory;
    private Map<Date, Integer> gamesPlayedHistory;
    private Map<Date, Integer> ratingHistory;

    private Map<Integer, Integer> playedFactionCount;
    private Map<Integer, Integer> wonFactionCount;

    private double totalMassShared;
    private double totalEnergyShared;
    private double totalMassReceived;
    private double totalEnergyReceived;


}
