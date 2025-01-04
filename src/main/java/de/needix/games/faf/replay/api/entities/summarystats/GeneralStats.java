package de.needix.games.faf.replay.api.entities.summarystats;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Embeddable
@ToString
public class GeneralStats {
    public static final String KILLS_PREFIX = "kills_";
    public static final String LOST_PREFIX = "lost_";
    public static final String BUILT_PREFIX = "built_";

    // Getters and Setters
    @JsonProperty("lastupdatetick")
    private int lastUpdateTick;
    private int score;
    @JsonProperty("currentcap")
    private int currentCap;

    @JsonProperty("currentunits")
    private int currentUnits;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "mass", column = @jakarta.persistence.Column(name = LOST_PREFIX + "mass")),
            @AttributeOverride(name = "count", column = @jakarta.persistence.Column(name = LOST_PREFIX + "count")),
            @AttributeOverride(name = "energy", column = @jakarta.persistence.Column(name = LOST_PREFIX + "energy"))
    })
    private LostStats lost;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "mass", column = @jakarta.persistence.Column(name = KILLS_PREFIX + "mass")),
            @AttributeOverride(name = "count", column = @jakarta.persistence.Column(name = KILLS_PREFIX + "count")),
            @AttributeOverride(name = "energy", column = @jakarta.persistence.Column(name = KILLS_PREFIX + "energy"))
    })
    private KillStats kills;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "mass", column = @jakarta.persistence.Column(name = BUILT_PREFIX + "mass")),
            @AttributeOverride(name = "count", column = @jakarta.persistence.Column(name = BUILT_PREFIX + "count")),
            @AttributeOverride(name = "energy", column = @jakarta.persistence.Column(name = BUILT_PREFIX + "energy"))
    })
    private BuiltStats built;

}