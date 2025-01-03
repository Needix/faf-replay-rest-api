package de.needix.games.faf.replay.api.entities.replay;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

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
