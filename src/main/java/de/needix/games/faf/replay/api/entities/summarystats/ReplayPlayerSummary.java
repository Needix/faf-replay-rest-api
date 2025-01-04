package de.needix.games.faf.replay.api.entities.summarystats;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Setter
@Getter
@Entity
@ToString
public class ReplayPlayerSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // Example: "Human"

    private String name; // Example: "N0Y0U"
    private int faction; // Example: 2

    @JsonProperty("Defeated")
    private double defeated;

    @Embedded
    private GeneralStats general;

    @ElementCollection
    @CollectionTable(name = "blueprint_stats", joinColumns = @JoinColumn(name = "game_stat_id"))
    @MapKeyColumn(name = "blueprint_id")
    @Column(name = "stats")
    private Map<String, BlueprintStats> blueprints;

    @Embedded
    private ResourceStats resources;

    @Embedded
    private UnitStats units;
}