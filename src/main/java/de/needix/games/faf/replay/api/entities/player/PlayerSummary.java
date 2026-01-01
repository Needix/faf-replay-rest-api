package de.needix.games.faf.replay.api.entities.player;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

@Getter
@Setter
@Entity
@ToString
public class PlayerSummary {
    @Id
    private String ownerId;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyEnumerated(EnumType.STRING)
    private Map<Faction, FactionStats> factionStats = new EnumMap<>(Faction.class);

    @ElementCollection
    @CollectionTable(name = "player_name_history", joinColumns = @JoinColumn(name = "player_summary_id"))
    @MapKeyColumn(name = "change_date")
    @Column(name = "player_name")
    private Map<Date, String> playerNameHistory = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "games_played_history", joinColumns = @JoinColumn(name = "player_summary_id"))
    @Column(name = "play_date")
    private List<Date> gamesPlayedHistory = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "player_summary_id")
    private List<PlayerRating> ratingHistory = new ArrayList<>();

//    List<ReplayPlayerApm> apmPerMinute = replayPlayer.getApmPerMinute();
//    Map<String, Object> armyInformation = replayPlayer.getArmyInformation();

    public long getTotalReplays() {
        return factionStats.values().stream().mapToLong(FactionStats::getTotalReplays).sum();
    }
}
