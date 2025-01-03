package de.needix.games.faf.replay.api.entities.summarystats;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Embeddable
public class KillStats {
    private double mass;
    private int count;
    private double energy;
}
