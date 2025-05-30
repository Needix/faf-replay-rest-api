package de.needix.games.faf.replay.api.entities.replay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.needix.games.faf.replay.api.JsonAttributeConverter;
import de.needix.games.faf.replay.api.entities.order.TargetOrder;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@Entity
public class ReplayPlayer {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int playerId;

    private double massShared;
    private double energyShared;
    private double massReceived;
    private double energyReceived;

    @Convert(converter = JsonAttributeConverter.class)
    @Column(columnDefinition = "json")
    private Map<String, Object> armyInformation = new HashMap<>();

    // Getters, setters, and other fields remain unchanged
    /**
     * {
     * "PlayerClan": "",
     * "Civilian": false,
     * "BadMap": false,
     * "PlayerName": "stevethelord1",
     * "Team": 2,
     * "Faction": 4,
     * "Human": true,
     * "MEAN": 1127.15,
     * "StartSpot": 1,
     * "ArmyName": "ARMY_1",
     * "ArmyColor": 15,
     * "Ready": true,
     * "PlayerColor": 15,
     * "DEV": 110.331,
     * "OwnerID": "423761",
     * "AIPersonality": "",
     * "Country": "ro",
     * "NG": 190,
     * "PL": 800,
     * "ObserverListIndex": -1
     * }
     */

    @OneToMany(cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<ReplayPlayerApm> apmPerMinute = new ArrayList<>();
    @OneToMany(cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<TargetOrder> targetOrders = new ArrayList<>();

    // Utility methods if needed to handle JSON conversion explicitly
    public static String toJson(Map<String, Object> map) throws JsonProcessingException {
        return objectMapper.writeValueAsString(map);
    }

    public static Map<String, Object> fromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, Map.class);
    }

    public void addApmPerMinute(int minute, double apm) {
        ReplayPlayerApm replayPlayerApm = new ReplayPlayerApm();
        replayPlayerApm.setMinute(minute);
        replayPlayerApm.setApm(apm);

        apmPerMinute.add(replayPlayerApm);
    }

    public void addTargetOrder(TargetOrder targetOrder) {
        // XXX: Currently disabled to reduce database size (x4)
        // targetOrders.add(targetOrder);
    }

    public void increaseMassShared(double amount) {
        massShared += amount;
    }

    public void increaseEnergyShared(double amount) {
        energyShared += amount;
    }

    public void increaseMassReceived(double amount) {
        massReceived += amount;
    }

    public void increaseEnergyReceived(double amount) {
        energyReceived += amount;
    }
}
