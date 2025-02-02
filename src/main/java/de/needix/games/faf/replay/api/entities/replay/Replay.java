package de.needix.games.faf.replay.api.entities.replay;

import com.fasterxml.jackson.annotation.JsonGetter;
import de.needix.games.faf.replay.api.entities.chat.ReplayChatMessage;
import de.needix.games.faf.replay.api.entities.summarystats.ReplayPlayerSummary;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@ToString
public class Replay {
    @Id
    private Long id;

    private String replayTitle;
    private int replayVersion;
    private String replayCompression;
    private boolean complete;
    private String featuredMod;
    /**
     * 0: assination
     * 1: domination
     * 2: sandbox
     * 2: sandbox
     */
    private String gameType;
    private Long gameStart;
    private Long gameEnd;
    private int numberOfPlayers;
    private String recorder;

    private String supComVersion;
    private String mapName;
    private boolean cheatsEnabled;
    private int randomSeed;

    @OneToMany(cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private Map<Integer, ReplayPlayer> players;

    @OneToMany(cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<ReplayChatMessage> chatMessages = new ArrayList<>();

    @OneToMany(cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<ReplayPlayerSummary> playerScores;
    @ElementCollection
    @Lob
    private Map<String, Serializable> scenarioInformation = new HashMap<>();

    @JsonGetter("gameType")
    public String getGameType() {
        if (gameType == null) {
            return null;
        } else if (gameType.equals("0")) {
            return "assassination";
        } else if (gameType.equals("1")) {
            return "domination";
        } else if (gameType.equals("2") || gameType.equals("3")) {
            return "sandbox";
        }
        return "unknown";
    }
}
