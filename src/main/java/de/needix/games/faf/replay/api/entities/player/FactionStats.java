package de.needix.games.faf.replay.api.entities.player;

import de.needix.games.faf.replay.api.entities.summarystats.ResourceStats;
import de.needix.games.faf.replay.api.entities.summarystats.UnitStats;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@ToString
public class FactionStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private Faction faction;
    private int totalReplays;

    private int totalWins;

    private UnitStats unitStats = new UnitStats();
    private ResourceStats resourceStats = new ResourceStats();

    @ElementCollection
    private List<Double> defeatedStats = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "played_game_type_count", joinColumns = @JoinColumn(name = "faction_stats_id"))
    @MapKeyColumn(name = "game_type")
    @Column(name = "count")
    private Map<String, Integer> playedGameTypeCount = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "won_game_type_count", joinColumns = @JoinColumn(name = "faction_stats_id"))
    @MapKeyColumn(name = "game_type")
    @Column(name = "count")
    private Map<String, Integer> wonGameTypeCount = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "played_color_count", joinColumns = @JoinColumn(name = "faction_stats_id"))
    @MapKeyColumn(name = "color")
    @Column(name = "count")
    private Map<Integer, Integer> playedColorCount = new HashMap<>();

    private double totalMassShared;
    private double totalEnergyShared;
    private double totalMassReceived;
    private double totalEnergyReceived;
}
