package de.needix.games.faf.replay.api.entities.replay;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Setter
@Getter
@Entity
public class ReplayPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int playerId;

    private double massShared;
    private double energyShared;
    private double massReceived;
    private double energyReceived;

    @OneToMany(cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<ReplayPlayerApm> apmPerMinute = new ArrayList<>();

    public void addApmPerMinute(int minute, double apm) {
        ReplayPlayerApm replayPlayerApm = new ReplayPlayerApm();
        replayPlayerApm.setMinute(minute);
        replayPlayerApm.setApm(apm);

        apmPerMinute.add(replayPlayerApm);
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
