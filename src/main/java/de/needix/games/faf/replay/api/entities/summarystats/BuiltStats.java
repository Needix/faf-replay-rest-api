package de.needix.games.faf.replay.api.entities.summarystats;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Embeddable
@ToString
public class BuiltStats {
    private double mass;
    private int count;
    private double energy;
}
