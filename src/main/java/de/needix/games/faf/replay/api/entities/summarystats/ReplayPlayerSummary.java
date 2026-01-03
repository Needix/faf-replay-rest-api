package de.needix.games.faf.replay.api.entities.summarystats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.needix.games.faf.replay.analyser.parser.BlueprintsDeserializer;
import de.needix.games.faf.replay.api.entities.player.Faction;
import de.needix.games.faf.replay.api.entities.player.Player;
import de.needix.games.faf.replay.api.entities.replay.Replay;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@Entity
public class ReplayPlayerSummary {
    @Id
    private String id;

    private String type; // Example: "Human"

    private String name; // Example: "N0Y0U"
    private Faction faction; // Example: 2

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Replay replay;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Player player;

    @JsonProperty("Defeated")
    private double defeated;

    @Embedded
    private GeneralStats general;

    @ElementCollection
    @CollectionTable(name = "blueprint_stats", joinColumns = @JoinColumn(name = "game_stat_id"))
    @MapKeyColumn(name = "blueprint_id")
    @Column(name = "stats")
    @JsonDeserialize(using = BlueprintsDeserializer.class)
    private Map<String, BlueprintStats> blueprints;

    @Embedded
    private ResourceStats resources;

    @Embedded
    private UnitStats units;

    public int hashCode() {
        return id.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReplayPlayerSummary that = (ReplayPlayerSummary) o;
        return id.equals(that.id);
    }

    @Override
    public String toString() {
        return "ReplayPlayerSummary{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", faction=" + faction +
                '}';
    }
}