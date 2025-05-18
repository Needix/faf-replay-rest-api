package de.needix.games.faf.replay.api.entities.replay;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.needix.games.faf.replay.api.JsonAttributeConverter;
import de.needix.games.faf.replay.api.entities.chat.ReplayChatMessage;
import de.needix.games.faf.replay.api.entities.summarystats.ReplayPlayerSummary;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

@Getter
@Setter
@Entity
@ToString
public class Replay {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date importDate;

    private String replayTitle;
    private int replayVersion;
    private String replayCompression;
    private boolean complete;
    private String featuredMod;
    /**
     * 0: assination
     * 1: domination
     * 2: sandbox
     * 2: sandbox
     */
    private String gameType;
    private Long gameStart;
    private Long gameEnd;
    private int numberOfPlayers;
    private String recorder;

    private String supComVersion;
    private String mapName;
    private boolean cheatsEnabled;
    private boolean ranked;
    private int randomSeed;

    @OneToMany(cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private Map<Integer, ReplayPlayer> players;

    @OneToMany(cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<ReplayChatMessage> chatMessages = new ArrayList<>();

    @OneToMany(cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<ReplayPlayerSummary> playerScores;
    /**
     * {
     * "preview": "",
     * "Options": {
     * "naturalReclaimModifier": 1,
     * "side_mexes": 1,
     * "Unranked": "No",
     * "SpawnMex": {},
     * "CheatMult": "1.5",
     * "Timeouts": "3",
     * "DisconnectionDelay02": "30",
     * "AIReplacement": "Off",
     * "Ratings": {
     * * * "stevethelord1": 800,
     * * * "DippinHigh": 400,
     * * * "AncientOne": 500,
     * * * "sudokuspess": 700,
     * * * "derek69": 500,
     * * * "Hronos24": 1000,
     * * * "IGrilex": 800,
     * * * "xEPICx": 1200,
     * * * "BOOM2115": 900,
     * * * "projekcinis": 400,
     * * * "kiroskp": 900,
     * * * "bigboyboomeroo": 800
     * },
     * "dynamic_spawn": 1,
     * "optional_wreckage": 1,
     * "TMLRandom": "0",
     * * * * * * * * * * * * * "Quality": 90.05,
     * "CivilianAlliance": "enemy",
     * "middle_mexes": 1,
     * "optional_XAXAXA": 1,
     * "NoRushOption": "Off",
     * * * * * * * * * * * * * "Victory": "demoralization",
     * "optional_replicas": 1,
     * "AllowObservers": false,
     * "ScenarioFile": "/maps/dualgap_adaptive.v0014/dualgap_adaptive_scenario.lua",
     * "FogOfWar": "explored",
     * "OmniCheat": "on",
     * "ClanTags": {
     * * *     "stevethelord1": "",
     * * *    "DippinHigh": "",
     * * *     "AncientOne": "",
     * * *     "sudokuspess": "",
     * * *     "derek69": "",
     * * *     "Hronos24": "",
     * * *     "IGrilex": "",
     * * *     "xEPICx": "",
     * * *     "BOOM2115": "",
     * * *     "projekcinis": "",
     * * *     "kiroskp": "",
     * * * "bigboyboomeroo": "GGI"
     * },
     * "ShareUnitCap": "allies",
     * "GameSpeed": "normal",
     * "RandomMap": "Off",
     * "PrebuiltUnits": "Off",
     * "TeamLock": "locked",
     * "NavalExpansionsAllowed": "4",
     * "UnitCap": "1000",
     * "CommonArmy": "Off",
     * "AutoTeams": "pvsi",
     * "Score": "no",
     * "TeamSpawn": "fixed",
     * "ManualUnitShare": "all",
     * "BuildMult": "1.5",
     * "CheatsEnabled": "false",
     * "RevealCivilians": "Yes",
     * "LandExpansionsAllowed": "5",
     * "Share": "ShareUntilDeath",
     * "underwater_mexes": 1
     * },
     * "AdaptiveMap": true,
     * "save": "/maps/dualgap_adaptive.v0014/DualGap_Adaptive_save.lua",
     * "description": "Made Robustness. Adaptive spawns: If youre not playing a 6v6, please use the Close/Close spawn mex option. Closed slots will not spawn any mex. Script by CookieNoob and KeyBlue, speed2, Super. AI marker+",
     * "type": "skirmish",
     * "script": "/maps/dualgap_adaptive.v0014/DualGap_Adaptive_script.lua",
     * "size": {
     * "1.0": 1024,
     * "2.0": 1024
     * },
     * "name": "DualGap Adaptive",
     * "Configurations": {
     * "standard": {
     * "customprops": {
     * "ExtraArmies": "ARMY_17 NEUTRAL_CIVILIAN"
     * },
     * "teams": {
     * "1.0": {
     * "armies": {
     * "1.0": "ARMY_1",
     * "2.0": "ARMY_2",
     * "3.0": "ARMY_3",
     * "4.0": "ARMY_4",
     * "6.0": "ARMY_6",
     * "8.0": "ARMY_8",
     * "12.0": "ARMY_12",
     * "9.0": "ARMY_9",
     * "5.0": "ARMY_5",
     * "7.0": "ARMY_7",
     * "10.0": "ARMY_10",
     * "11.0": "ARMY_11"
     * },
     * "name": "FFA"
     * }
     * }
     * }
     * },
     * "map_version": 14,
     * "norushradius": 50,
     * "starts": true,
     * "map": "/maps/dualgap_adaptive.v0014/DualGap_Adaptive.scmap"
     * }
     */

    @Convert(converter = JsonAttributeConverter.class)
    @Column(columnDefinition = "json")
    private Map<String, Object> scenarioInformation = new HashMap<>();

    // Getters, setters, and other fields remain unchanged

    // Utility methods if needed to handle JSON conversion explicitly
    public static String toJson(Map<String, Object> map) throws JsonProcessingException {
        return objectMapper.writeValueAsString(map);
    }

    public static Map<String, Object> fromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, Map.class);
    }


    @JsonGetter("gameType")
    public String getGameType() {
        if (gameType == null) {
            return null;
        } else if (gameType.equals("0")) {
            return "assassination";
        } else if (gameType.equals("1")) {
            return "domination";
        } else if (gameType.equals("2") || gameType.equals("3")) {
            return "sandbox";
        }
        return "unknown";
    }
}
