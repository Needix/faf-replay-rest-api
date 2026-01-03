package de.needix.games.faf.replay.api.entities.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.needix.games.faf.replay.api.entities.replay.ReplayPlayer;
import de.needix.games.faf.replay.api.entities.summarystats.ReplayPlayerSummary;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class Player {
    @Id
    private String ownerId;

    private String name;

    @JsonIgnore
    @OneToMany(mappedBy = "player")
    private Set<ReplayPlayer> replayPlayers = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "player")
    private Set<ReplayPlayerSummary> replayPlayerSummaries = new LinkedHashSet<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "player_summary_id")
    private PlayerSummary playerSummary;

    @Override
    public String toString() {
        return "Player{" +
                "ownerId='" + ownerId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
