package de.needix.games.faf.replay.api.entities.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Setter
@Getter
@ToString
public class ReplayChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int tick;
    private String sender;
    private String receiver;
    @Column(name = "message", length = 5000)
    private String message;
    private boolean isMarker;


}