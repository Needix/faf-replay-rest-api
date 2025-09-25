package de.needix.games.faf.replay.api.entities.player;

import de.needix.games.faf.replay.api.entities.replay.ReplayPlayer;
import de.needix.games.faf.replay.api.entities.summarystats.ReplayPlayerSummary;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@ToString
public class Player {
    @Id
    private String ownerId;

    private String name;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    private List<ReplayPlayer> replayPlayers = new ArrayList<>();

    @OneToMany(cascade = jakarta.persistence.CascadeType.ALL, mappedBy = "player", fetch = FetchType.EAGER)
    private List<ReplayPlayerSummary> replayPlayerSummaries = new ArrayList<>();

    @OneToOne(cascade = jakarta.persistence.CascadeType.ALL)
    @JoinColumn(name = "player_summary_id")
    private PlayerSummary playerSummary;
}
