package de.needix.games.faf.replay.api.entities.order;

import de.needix.games.faf.replay.analyser.parser.CommandParser;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TargetOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int tick;
    private double targetX;
    private double targetY;
    private double targetZ;
    private CommandParser.CommandOrderType orderType;
}
