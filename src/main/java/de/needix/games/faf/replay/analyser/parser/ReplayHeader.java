package de.needix.games.faf.replay.analyser.parser;

import de.needix.games.faf.replay.api.entities.replay.Replay;
import de.needix.games.faf.replay.api.entities.replay.ReplayPlayer;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@ToString
public class ReplayHeader {
    private final Replay replayToFill;

    public ReplayHeader(ReplayParser reader, Replay replayToFill) {
        this.replayToFill = replayToFill;

        // Read version string
        String version = reader.readString();
        reader.read(3);// Skip 3 bytes

        // Read replay version and map name
        String[] versionAndMap = reader.readString().split("\r\n", 2);
        String replayVersion = versionAndMap[0];
        String mapName = versionAndMap[1];
        reader.read(4);// Skip 4 bytes

        // Read mods
        reader.readUnsignedInt();// mods_size
        Object mods = reader.readLua(null);

        // Read scenario
        reader.readUnsignedInt();// scenario_size
        Object scenario = reader.readLua(null);
        replayToFill.setScenarioInformation((Map<String, Serializable>) scenario);

        // Read players
        int sourcesNumber = reader.readByte();
        HashMap<Integer, ReplayPlayer> players = new HashMap<>();
        for (int i = 0; i < sourcesNumber; i++) {
            String name = reader.readString();
            int playerType = reader.readUnsignedInt();
            ReplayPlayer replayPlayer = new ReplayPlayer();
            replayPlayer.setName(name);
            replayPlayer.setPlayerId(i + 1);
            players.put(i + 1, replayPlayer);
        }

        // Read cheats enabled
        boolean cheatsEnabled = reader.readBool();

        // Read number of armies
        int numberOfArmies = reader.readByte();

        // Read armies data
        for (int i = 0; i < numberOfArmies; i++) {
            reader.readUnsignedInt();// player_data_size
            Map<String, Serializable> playerData = (Map<String, Serializable>) reader.readLua(null);
            int playerSource = reader.readByte();

            if (playerSource != 255) {
                players.get(playerSource + 1).setArmyInformation(playerData);
                reader.read(1); // Skip 1 byte
            }
        }

        // Read random seed
        int randomSeed = reader.readUnsignedInt();

        replayToFill.setSupComVersion(version);
        replayToFill.setMapName(mapName);
        replayToFill.setCheatsEnabled(cheatsEnabled);
        replayToFill.setRandomSeed(randomSeed);
        replayToFill.setPlayers(players);
    }

    // Getters for individual fields can be added if needed
}

