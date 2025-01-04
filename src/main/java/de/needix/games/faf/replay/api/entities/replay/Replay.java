package de.needix.games.faf.replay.api.entities.replay;

import de.needix.games.faf.replay.api.entities.chat.ReplayChatMessage;
import de.needix.games.faf.replay.api.entities.summarystats.ReplayPlayerSummary;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
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
}
