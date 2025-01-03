package de.needix.games.faf.replay.analyser.eventanalyser;

import de.needix.games.faf.replay.analyser.parser.Command;
import de.needix.games.faf.replay.api.entities.replay.Replay;
import de.needix.games.faf.replay.api.entities.replay.ReplayPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LuaAnalyser {
    private static final Logger logger = LoggerFactory.getLogger(LuaAnalyser.class);
    private final Replay replayToFill;
    private final ModeratorEventAnalyser moderatorEventAnalyser;

    public LuaAnalyser(Replay replayToFill) {
        this.replayToFill = Objects.requireNonNull(replayToFill, "replayToFill must not be null");
        this.moderatorEventAnalyser = new ModeratorEventAnalyser(replayToFill);
    }

    public void analyzeLua(Command command) {
        Map<String, Object> commandData = command.getCommandData();
        Map<Object, Object> lua = (Map<Object, Object>) commandData.get("lua");
        String luaName = commandData.get("lua_name").toString();
        switch (luaName) {
            case "GiveResourcesToPlayer":
                handleGiveResourcesToPlayer(command, lua);
                break;
            case "CopyOrders":
                handleCopyOrders(command, lua);
                break;
            case "OnControlGroupAssign":
                handleOnControlGroupAssign(command, lua);
                break;
            case "Rebuild":
                handleRebuild(command, lua);
                break;
            case "RingWithStorages":
                handleRingWithStorages(command, lua);
                break;
            case "AttackMove":
                handleAttackMove(command, lua);
                break;
            case "ToggleSelfDestruct":
                handleToggleSelfDestruct(command, lua);
                break;
            case "ModeratorEvent":
                moderatorEventAnalyser.handleModeratorEvent(command, lua);
                break;
            case "SetRecallVote":
                handleSetRecallVote(command, lua);
                break;
            case "GiveUnitsToPlayer":
                handleGiveUnitsToPlayer(command, lua);
                break;
            case "SpawnSpecialPing":
                handleSpawnSpecialPing(command, lua);
                break;
            case "SpawnPing":
                handleSpawnPing(command, lua);
                break;
            case "ImmediateHiveUpgrade":
                handleImmediateHiveUpgrade(command, lua);
                break;
            case "UpdateMarker":
                handleUpdateMarker(command, lua);
                break;
            case "AutoOvercharge":
                handleAutoOvercharge(command, lua);
                break;
            case "DistributeOrders":
                handleDistributeOrders(command, lua);
                break;
            case "RingWithFabricators":
                handleRingWithFabricators(command, lua);
                break;
            case "ValidateAssist":
                handleValidateAssist(command, lua);
                break;
            case "WeaponPriorities":
                handleWeaponPriorities(command, lua);
                break;
            default:
                logger.warn("Unknown Lua function: {} : {}", luaName, lua);
                break;
        }
    }

    private void handleGiveResourcesToPlayer(Command command, Map<Object, Object> lua) {
        Map<String, Object> msgData = (Map<String, Object>) lua.get("Msg");
        Object sender = lua.get("Sender");

        if (msgData != null) {
            ChatAnalyser.handleChat(command, replayToFill, msgData, sender.toString());
        } else {
            int toId = ((Float) lua.get("To")).intValue();
            int fromId = ((Float) lua.get("From")).intValue();
            float energy = (float) lua.get("Energy");
            float mass = (float) lua.get("Mass");

            ReplayPlayer fromPlayer = replayToFill.getPlayers().get(fromId);
            fromPlayer.increaseEnergyShared(energy);
            fromPlayer.increaseMassShared(mass);
            ReplayPlayer toPlayer = replayToFill.getPlayers().get(toId);
            toPlayer.increaseMassReceived(mass);
            toPlayer.increaseEnergyReceived(energy);
        }
    }

    private void handleCopyOrders(Command command, Map<Object, Object> lua) {
        /*
        "ClearCommands" -> {Boolean@1505} true
        "Target" -> "10485789"
         */
        Set<Map.Entry<Object, Object>> entries = lua.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
            entry.getKey();
            entry.getValue();
        }
    }

    private void handleOnControlGroupAssign(Command command, Map<Object, Object> lua) {
        logger.trace("OnControlGroupAssign: {}", lua);
        /*
        {Float@1491} 1.0 -> "4194312"
        {Float@1493} 2.0 -> "4194307"
        {Float@1495} 3.0 -> "4194310"
         */
    }

    private void handleRebuild(Command command, Map<Object, Object> lua) {
        logger.trace("Rebuild: {}", lua);
        /*
        "entity" -> "11534342"
        "Clear" -> {Boolean@1494} true
         */
    }

    private void handleRingWithStorages(Command command, Map<Object, Object> lua) {
        logger.trace("RingWithStorages: {}", lua);
        /*
        target -> 11534350
         */
    }

    private void handleAttackMove(Command command, Map<Object, Object> lua) {
        logger.trace("AttackMove: {}", lua);
        /*
        Target={1.0=88.30132, 2.0=63.195312, 3.0=674.70685}, Rotation=216.51114, Clear=false
         */
    }

    private void handleToggleSelfDestruct(Command command, Map<Object, Object> lua) {
        logger.trace("ToggleSelfDestruct: {}", lua);
        /*
        owner=8.0, noDelay=false
         */
    }

    private void handleSetRecallVote(Command command, Map<Object, Object> lua) {
        logger.trace("SetRecallVote: {}", lua);
        /*
            Vote=true, From=12.0
         */
    }

    private void handleGiveUnitsToPlayer(Command command, Map<Object, Object> lua) {
        logger.trace("GiveUnitsToPlayer: {}", lua);
        /*
        To=12.0, From=10.0
         */
    }

    private void handleSpawnSpecialPing(Command command, Map<Object, Object> lua) {
        logger.trace("SpawnSpecialPing: {}", lua);
        /*
        Type=nuke, Owner=10.0, Lifetime=10.0, Ring=/textures/ui/common/game/marker/ring_nuke04-blur.dds, ArrowColor=red, Mesh=nuke_marker, Sound=Aeon_Select_Radar, Location={1.0=626.73724, 2.0=63.195312, 3.0=398.43472}
         */
    }

    private void handleSpawnPing(Command command, Map<Object, Object> lua) {
        logger.trace("SpawnPing: {}", lua);
        /*
        Type=Marker, Owner=2.0, Lifetime=5.0, Ring=/game/marker/ring_yellow02-blur.dds, Color=ff66ffcc, ArrowColor=yellow, Sound=UI_Main_IG_Click, Marker=true, Name=nuke, Location={1.0=624.9585, 2.0=63.195312, 3.0=395.40198}
         */
    }

    private void handleImmediateHiveUpgrade(Command command, Map<Object, Object> lua) {
        logger.trace("ImmediateHiveUpgrade: {}", lua);
        /*
        UpgradeTo=xrb0204
         */
    }

    private void handleUpdateMarker(Command command, Map<Object, Object> lua) {
        logger.trace("UpdateMarker: {}", lua);
        /*
        Owner=1.0, Action=delete, ID=3.0
         */
    }

    private void handleAutoOvercharge(Command command, Map<Object, Object> lua) {
        logger.trace("AutoOvercharge: {}", lua);
        /*
        auto=true
         */
    }

    private void handleDistributeOrders(Command command, Map<Object, Object> lua) {
        logger.trace("DistributeOrders: {}", lua);
        /*
            ClearCommands=true, Target=5242933
         */
    }

    private void handleRingWithFabricators(Command command, Map<Object, Object> lua) {
        logger.trace("RingWithFabricators: {}", lua);
        /*
        allFabricators=true, target=10485835
         */
    }

    private void handleValidateAssist(Command command, Map<Object, Object> lua) {
        logger.trace("ValidateAssist: {}", lua);
        /*
        target=6291592
         */
    }

    private void handleWeaponPriorities(Command command, Map<Object, Object> lua) {
        logger.trace("WeaponPriorities: {}", lua);
        /*
            SelectedUnits={1.0=2097350}, prioritiesTable={categories.EXPERIMENTAL}, name=EXP, exclusive=false
         */
    }
}