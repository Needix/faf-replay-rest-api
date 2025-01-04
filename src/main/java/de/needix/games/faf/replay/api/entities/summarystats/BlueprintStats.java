package de.needix.games.faf.replay.api.entities.summarystats;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Embeddable
@ToString
public class BlueprintStats {
    private int kills;
    private int built;
    private int lost;
    @JsonProperty("lowest_health")
    private int lowestHealth;
}