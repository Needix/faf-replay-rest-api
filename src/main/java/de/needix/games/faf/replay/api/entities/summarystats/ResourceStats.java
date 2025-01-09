package de.needix.games.faf.replay.api.entities.summarystats;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@Embeddable
@ToString
public class ResourceStats {
    private static final String IN = "in";
    private static final String OUT = "out";

    private static final String TOTAL = "total";
    private static final String RATE = "rate";
    private static final String EXCESS = "excess";
    private static final String RECLAIMED = "reclaimed";
    private static final String RECLAIM_RATE = "reclaimRate";

    private static final String MASS_PREFIX = "mass_";
    private static final String ENERGY_PREFIX = "energy_";
    private static final String STORAGE_PREFIX = "storage_";

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = TOTAL, column = @jakarta.persistence.Column(name = MASS_PREFIX + IN + TOTAL)),
            @AttributeOverride(name = RECLAIMED, column = @jakarta.persistence.Column(name = MASS_PREFIX + IN + RECLAIMED)),
            @AttributeOverride(name = RECLAIM_RATE, column = @jakarta.persistence.Column(name = MASS_PREFIX + IN + RECLAIM_RATE)),
            @AttributeOverride(name = RATE, column = @jakarta.persistence.Column(name = MASS_PREFIX + IN + RATE))
    })
    @JsonProperty("massin")
    private InStats massIn;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = TOTAL, column = @jakarta.persistence.Column(name = MASS_PREFIX + OUT + TOTAL)),
            @AttributeOverride(name = RATE, column = @jakarta.persistence.Column(name = MASS_PREFIX + OUT + RATE)),
            @AttributeOverride(name = EXCESS, column = @jakarta.persistence.Column(name = MASS_PREFIX + OUT + EXCESS))
    })
    @JsonProperty("massout")
    private OutStats massOut;


    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = TOTAL, column = @jakarta.persistence.Column(name = ENERGY_PREFIX + IN + TOTAL)),
            @AttributeOverride(name = RECLAIMED, column = @jakarta.persistence.Column(name = ENERGY_PREFIX + IN + RECLAIMED)),
            @AttributeOverride(name = RECLAIM_RATE, column = @jakarta.persistence.Column(name = ENERGY_PREFIX + IN + RECLAIM_RATE)),
            @AttributeOverride(name = RATE, column = @jakarta.persistence.Column(name = ENERGY_PREFIX + IN + RATE))
    })
    @JsonProperty("energyin")
    private InStats energyIn;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = TOTAL, column = @jakarta.persistence.Column(name = ENERGY_PREFIX + OUT + TOTAL)),
            @AttributeOverride(name = RATE, column = @jakarta.persistence.Column(name = ENERGY_PREFIX + OUT + RATE)),
            @AttributeOverride(name = EXCESS, column = @jakarta.persistence.Column(name = ENERGY_PREFIX + OUT + EXCESS))
    })
    @JsonProperty("energyout")
    private OutStats energyOut;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "maxMass", column = @jakarta.persistence.Column(name = STORAGE_PREFIX + "maxMass")),
            @AttributeOverride(name = "storedMass", column = @jakarta.persistence.Column(name = STORAGE_PREFIX + "storedMass")),
            @AttributeOverride(name = "maxEnergy", column = @jakarta.persistence.Column(name = STORAGE_PREFIX + "maxEnergy")),
            @AttributeOverride(name = "storedEnergy", column = @jakarta.persistence.Column(name = STORAGE_PREFIX + "storedEnergy"))
    })
    private StorageStats storage;

    public Map<String, Object> toMap() {
        return Map.of(
                "massIn", massIn != null ? massIn.toMap() : null,
                "massOut", massOut != null ? massOut.toMap() : null,
                "energyIn", energyIn != null ? energyIn.toMap() : null,
                "energyOut", energyOut != null ? energyOut.toMap() : null,
                "storage", storage != null ? storage.toMap() : null
        );
    }

    @Getter
    @Setter
    @Embeddable
    public static class InStats {
        private double total;
        private double reclaimed;
        private double reclaimRate;
        private double rate;

        public Map<String, Object> toMap() {
            return Map.of(
                    "total", total,
                    "reclaimed", reclaimed,
                    "reclaimRate", reclaimRate,
                    "rate", rate
            );
        }
    }

    @Getter
    @Setter
    @Embeddable
    public static class OutStats {
        private double total;
        private double rate;
        private double excess;

        public Map<String, Object> toMap() {
            return Map.of(
                    "total", total,
                    "rate", rate,
                    "excess", excess
            );
        }
    }

    @Getter
    @Setter
    @Embeddable
    public static class StorageStats {
        private double maxMass;
        private double storedMass;
        private double maxEnergy;
        private double storedEnergy;

        public Map<String, Object> toMap() {
            return Map.of(
                    "maxMass", maxMass,
                    "storedMass", storedMass,
                    "maxEnergy", maxEnergy,
                    "storedEnergy", storedEnergy
            );
        }
    }
}