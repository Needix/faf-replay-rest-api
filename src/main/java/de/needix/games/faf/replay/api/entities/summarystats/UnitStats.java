package de.needix.games.faf.replay.api.entities.summarystats;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@Embeddable
@ToString
public class UnitStats {
    // Prefix constants for different unit types
    public static final String AIR_PREFIX = "air_";
    public static final String TECH3_PREFIX = "tech3_";
    public static final String EXPERIMENTAL_PREFIX = "experimental_";
    public static final String TECH2_PREFIX = "tech2_";
    public static final String TECH1_PREFIX = "tech1_";
    public static final String TRANSPORTATION_PREFIX = "transportation_";
    public static final String LAND_PREFIX = "land_";
    public static final String STRUCTURES_PREFIX = "structures_";
    public static final String SACU_PREFIX = "sacu_";
    public static final String NAVAL_PREFIX = "naval_";
    public static final String ENGINEER_PREFIX = "engineer_";
    public static final String CDR_PREFIX = "cdr_";

    // Suffix constants for unit fields
    private static final String LOST_SUFFIX = "lost";
    private static final String KILLS_SUFFIX = "kills";
    private static final String BUILT_SUFFIX = "built";

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = LOST_SUFFIX, column = @jakarta.persistence.Column(name = AIR_PREFIX + LOST_SUFFIX)),
            @AttributeOverride(name = KILLS_SUFFIX, column = @jakarta.persistence.Column(name = AIR_PREFIX + KILLS_SUFFIX)),
            @AttributeOverride(name = BUILT_SUFFIX, column = @jakarta.persistence.Column(name = AIR_PREFIX + BUILT_SUFFIX))
    })
    private UnitDetail air = new UnitDetail();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = LOST_SUFFIX, column = @jakarta.persistence.Column(name = TECH3_PREFIX + LOST_SUFFIX)),
            @AttributeOverride(name = KILLS_SUFFIX, column = @jakarta.persistence.Column(name = TECH3_PREFIX + KILLS_SUFFIX)),
            @AttributeOverride(name = BUILT_SUFFIX, column = @jakarta.persistence.Column(name = TECH3_PREFIX + BUILT_SUFFIX))
    })
    private UnitDetail tech3 = new UnitDetail();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = LOST_SUFFIX, column = @jakarta.persistence.Column(name = EXPERIMENTAL_PREFIX + LOST_SUFFIX)),
            @AttributeOverride(name = KILLS_SUFFIX, column = @jakarta.persistence.Column(name = EXPERIMENTAL_PREFIX + KILLS_SUFFIX)),
            @AttributeOverride(name = BUILT_SUFFIX, column = @jakarta.persistence.Column(name = EXPERIMENTAL_PREFIX + BUILT_SUFFIX))
    })
    private UnitDetail experimental = new UnitDetail();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = LOST_SUFFIX, column = @jakarta.persistence.Column(name = TECH2_PREFIX + LOST_SUFFIX)),
            @AttributeOverride(name = KILLS_SUFFIX, column = @jakarta.persistence.Column(name = TECH2_PREFIX + KILLS_SUFFIX)),
            @AttributeOverride(name = BUILT_SUFFIX, column = @jakarta.persistence.Column(name = TECH2_PREFIX + BUILT_SUFFIX))
    })
    private UnitDetail tech2 = new UnitDetail();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = LOST_SUFFIX, column = @jakarta.persistence.Column(name = TECH1_PREFIX + LOST_SUFFIX)),
            @AttributeOverride(name = KILLS_SUFFIX, column = @jakarta.persistence.Column(name = TECH1_PREFIX + KILLS_SUFFIX)),
            @AttributeOverride(name = BUILT_SUFFIX, column = @jakarta.persistence.Column(name = TECH1_PREFIX + BUILT_SUFFIX))
    })
    private UnitDetail tech1 = new UnitDetail();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = LOST_SUFFIX, column = @jakarta.persistence.Column(name = LAND_PREFIX + LOST_SUFFIX)),
            @AttributeOverride(name = KILLS_SUFFIX, column = @jakarta.persistence.Column(name = LAND_PREFIX + KILLS_SUFFIX)),
            @AttributeOverride(name = BUILT_SUFFIX, column = @jakarta.persistence.Column(name = LAND_PREFIX + BUILT_SUFFIX))
    })
    private UnitDetail land = new UnitDetail();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = LOST_SUFFIX, column = @jakarta.persistence.Column(name = NAVAL_PREFIX + LOST_SUFFIX)),
            @AttributeOverride(name = KILLS_SUFFIX, column = @jakarta.persistence.Column(name = NAVAL_PREFIX + KILLS_SUFFIX)),
            @AttributeOverride(name = BUILT_SUFFIX, column = @jakarta.persistence.Column(name = NAVAL_PREFIX + BUILT_SUFFIX))
    })
    private UnitDetail naval = new UnitDetail();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = LOST_SUFFIX, column = @jakarta.persistence.Column(name = TRANSPORTATION_PREFIX + LOST_SUFFIX)),
            @AttributeOverride(name = KILLS_SUFFIX, column = @jakarta.persistence.Column(name = TRANSPORTATION_PREFIX + KILLS_SUFFIX)),
            @AttributeOverride(name = BUILT_SUFFIX, column = @jakarta.persistence.Column(name = TRANSPORTATION_PREFIX + BUILT_SUFFIX))
    })
    private UnitDetail transportation = new UnitDetail();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = LOST_SUFFIX, column = @jakarta.persistence.Column(name = STRUCTURES_PREFIX + LOST_SUFFIX)),
            @AttributeOverride(name = KILLS_SUFFIX, column = @jakarta.persistence.Column(name = STRUCTURES_PREFIX + KILLS_SUFFIX)),
            @AttributeOverride(name = BUILT_SUFFIX, column = @jakarta.persistence.Column(name = STRUCTURES_PREFIX + BUILT_SUFFIX))
    })
    private UnitDetail structures = new UnitDetail();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = LOST_SUFFIX, column = @jakarta.persistence.Column(name = SACU_PREFIX + LOST_SUFFIX)),
            @AttributeOverride(name = KILLS_SUFFIX, column = @jakarta.persistence.Column(name = SACU_PREFIX + KILLS_SUFFIX)),
            @AttributeOverride(name = BUILT_SUFFIX, column = @jakarta.persistence.Column(name = SACU_PREFIX + BUILT_SUFFIX))
    })
    private UnitDetail sacu = new UnitDetail();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = LOST_SUFFIX, column = @jakarta.persistence.Column(name = ENGINEER_PREFIX + LOST_SUFFIX)),
            @AttributeOverride(name = KILLS_SUFFIX, column = @jakarta.persistence.Column(name = ENGINEER_PREFIX + KILLS_SUFFIX)),
            @AttributeOverride(name = BUILT_SUFFIX, column = @jakarta.persistence.Column(name = ENGINEER_PREFIX + BUILT_SUFFIX))
    })
    private UnitDetail engineer = new UnitDetail();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = LOST_SUFFIX, column = @jakarta.persistence.Column(name = CDR_PREFIX + LOST_SUFFIX)),
            @AttributeOverride(name = KILLS_SUFFIX, column = @jakarta.persistence.Column(name = CDR_PREFIX + KILLS_SUFFIX)),
            @AttributeOverride(name = BUILT_SUFFIX, column = @jakarta.persistence.Column(name = CDR_PREFIX + BUILT_SUFFIX))
    })
    private UnitDetail cdr = new UnitDetail(); // Commander

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("air", air != null ? air.toMap() : null);
        result.put("tech1", tech1 != null ? tech1.toMap() : null);
        result.put("tech2", tech2 != null ? tech2.toMap() : null);
        result.put("tech3", tech3 != null ? tech3.toMap() : null);
        result.put("experimental", experimental != null ? experimental.toMap() : null);
        result.put("structures", structures != null ? structures.toMap() : null);
        result.put("land", land != null ? land.toMap() : null);
        result.put("naval", naval != null ? naval.toMap() : null);
        result.put("engineer", engineer != null ? engineer.toMap() : null);
        result.put("sacu", sacu != null ? sacu.toMap() : null);
        result.put("transportation", transportation != null ? transportation.toMap() : null);
        return result;
    }

    @Setter
    @Getter
    @Embeddable
    public static class UnitDetail {
        private int lost;
        private int kills;
        private int built;

        public Map<String, Object> toMap() {
            return Map.of(
                    "lost", lost,
                    "kills", kills,
                    "built", built
            );
        }
    }
}