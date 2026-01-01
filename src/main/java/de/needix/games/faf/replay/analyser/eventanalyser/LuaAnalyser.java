package de.needix.games.faf.replay.analyser.eventanalyser;

import de.needix.games.faf.replay.analyser.parser.Command;
import de.needix.games.faf.replay.analyser.parser.CommandType;
import de.needix.games.faf.replay.api.entities.replay.Replay;
import de.needix.games.faf.replay.api.entities.replay.ReplayPlayer;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@ToString
public class LuaAnalyser implements CommandAnalyser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LuaAnalyser.class);
    private final Replay replayToFill;
    private final ModeratorEventAnalyser moderatorEventAnalyser;

    public LuaAnalyser(Replay replayToFill) {
        this.replayToFill = Objects.requireNonNull(replayToFill, "replayToFill must not be null");
        this.moderatorEventAnalyser = new ModeratorEventAnalyser(replayToFill);
    }

    @Override
    public void analyseCommand(Command command) {
        if (command.getCommandType() != CommandType.LUA_SIM_CALLBACK) {
            return;
        }

        Map<String, Object> commandData = command.getCommandData();
        Object luaObject = commandData.get("lua");
        if (!(luaObject instanceof Map)) {
            LOGGER.debug("Lua object is not a map and will not be handled: {}", luaObject);
            return;
        }
        Map<Object, Object> lua = (Map<Object, Object>) luaObject;
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
            case "RingRadar":
                handleRingRadar(command, lua);
                break;
            case "RingArtilleryTech2":
                handleRingArtilleryTech2(command, lua);
                break;
            case "FlagShield":
                handleFlagShield(command, lua);
                break;
            case "ClearCommands":
                handleClearCommands(command, lua);
                break;
            case "SetResourceSharing":
                handleSetResourceSharing(command, lua);
                break;
            case "CheatSpawnUnit":
                handleCheatSpawnUnit(command, lua);
                break;
            case "ExtendAttackOrder":
                handleExtendAttackOrder(command, lua);
                break;
            case "SelectHighestEngineerAndAssist":
                handleSelectHighestEngineerAndAssist(command, lua);
                break;
            case "AbortNavigation":
                handleAbortNavigation(command, lua);
                break;
            case "RingArtilleryTech3Exp":
                handleRingArtilleryTech3Exp(command, lua);
                break;
            case "PingGroupClick":
                // TODO: PingGroupClick : {ID=1.0, Location={1.0=311.02167, 2.0=19.835938, 3.0=877.73975}}
                break;
            case "DiplomacyHandler":
                // TODO: DiplomacyHandler : {Action=offer, To=1.0, From=2.0}
                break;
            case "SetOfferDraw":
                // TODO: SetOfferDraw : {Army=4.0, Value=true}
                break;
            case "OnPlayerQuery":
                // TODO: OnPlayerQuery : {Args={ID=4.0, Volunteered=1.0}, To=-1.0, From=4.0, MsgId=2.0, Name=VolunteerVote}
                break;
            case "LoadIntoTransports":
                // TODO LoadIntoTransports : {ClearCommands=true}
                break;
            case "RequestAlliedVictory":
                // TODO RequestAlliedVictory : {Army=6.0, Value=false}
                break;
            case "GiveOrders":
                // TODO GiveOrders : {From=1.0, unit_orders={1.0={CommandType=Move, Position={1.0=216.43355, 2.0=18.972656, 3.0=648.32074}}, 2.0={CommandType=Move, Position={1.0=214.43355, 2.0=18.972656, 3.0=650.32074}}, 3.0={CommandType=Move, Position={1.0=214.43355, 2.0=18.972656, 3.0=646.32074}}, 4.0={CommandType=Move, Position={1.0=218.43355, 2.0=18.972656, 3.0=646.32074}}, 5.0={CommandType=Move, Position={1.0=218.43355, 2.0=18.972656, 3.0=650.32074}}, 6.0={CommandType=Move, Position={1.0=214.43355, 2.0=18.972656, 3.0=650.32074}}, 7.0={EntityId=18, CommandType=Guard}}, unit_id=5}
                break;
            case "SetStatByCallback":
                // TODO SetStatByCallback : {AutoDeploy=true}
                break;
            case "SimDialogueButtonPress":
                // TODO SimDialogueButtonPress : {presser=7.0, buttonID=1.0, ID=1.0}
                break;
            case "ToggleVeteranBuilding2":
                // TODO ToggleVeteranBuilding2 : {owner=1.0, units={1.0=247, 2.0=62, 3.0=241, 4.0=54, 6.0=88, 8.0=244, 12.0=50, 16.0=251, 17.0=94, 9.0=136, 13.0=180, 18.0=252, 19.0=207, 5.0=32, 7.0=25, 10.0=214, 14.0=176, 20.0=212, 11.0=48, 15.0=239}}
                break;
            case "BoxFormationSpawn":
                // TODO BoxFormationSpawn : {army=1.0, MeshOnly=false, pos={1.0=448.92157, 2.0=23.0, 3.0=686.98254}, UnitIconCameraMode=false, veterancy=0.0, bpId=led0019, count=1.0, CreateTarmac=true, yaw=360.0}
                break;
            case "SyncValueFromUi":
                // TODO SyncValueFromUi : {Specialization=ALL, id=36, AffectName=ArmorPerc}
                break;
            case "SpawnFireSupport":
                // TODO SpawnFireSupport : {ArmyIndex=4.0, pos={1.0=603.9528, 2.0=20.070312, 3.0=420.832}, yes=true, id=uafsas1001}
                break;
            case "SharePaintingBrushStroke":
                // TODO ShareablePainting={PaintingAdapterIdentifier=table: 20B52500, ShareId=1.0, PeerName=flyingpancake, Samples={2.0=29.28595, 8.0=31.242249, 32.0=31.46875, 33.0=803.8864, 34.0=686.038, 35.0=31.484375, 9.0=876.1807, 36.0=784.9886, 37.0=684.3175, 38.0=31.476562, 39.0=778.08746, 10.0=651.0537, 40.0=685.19116, 41.0=31.460938, 42.0=771.6006, 43.0=687.8022, 11.0=31.34375, 44.0=31.434814, 45.0=764.6601, 46.0=688.2467, 47.0=31.390747, 3.0=875.5052, 12.0=874.9199, 48.0=759.0112, 49.0=687.828, 50.0=31.308289, 51.0=750.73157, 13.0=662.09814, 52.0=683.051, 53.0=31.258179, 54.0=745.0475, 55.0=665.2069, 14.0=31.343018, 56.0=31.320312, 57.0=737.1775, 58.0=607.667, 59.0=31.300781, 15.0=872.8294, 60.0=732.79224, 61.0=592.94885, 62.0=31.273438, 63.0=739.80023, 4.0=593.54016, 1.0=592.5736, 16.0=671.0253, 64.0=568.0009, 65.0=31.257812, 66.0=755.51697, 67.0=556.3766, 17.0=31.335938, 68.0=31.197327, 69.0=760.7332, 70.0=542.6323, 71.0=30.683472, 18.0=869.0661, 72.0=770.6821, 73.0=537.4807, 74.0=30.015076, 75.0=778.00757, 19.0=673.1522, 76.0=536.848, 77.0=30.658691, 78.0=782.36206, 79.0=536.1017, 5.0=30.187927, 20.0=31.335938, 80.0=30.978577, 81.0=802.1598, 82.0=531.7509, 83.0=30.941345, 21.0=866.1372, 84.0=817.5486, 85.0=531.0696, 86.0=30.907959, 87.0=824.78937, 22.0=676.9804, 88.0=532.11127, 89.0=30.901917, 90.0=832.012, 91.0=534.4702, 23.0=31.34375, 92.0=31.00946, 93.0=839.63403, 94.0=538.0826, 95.0=31.141846, 6.0=875.8472, 24.0=858.1631, 96.0=846.387, 97.0=544.6581, 98.0=31.232788, 99.0=852.70435, 25.0=687.2763, 100.0=570.9406, 101.0=31.265625, 102.0=865.30237, 103.0=578.2623, 26.0=31.43335, 104.0=31.273438, 105.0=869.9096, 106.0=586.78394, 107.0=30.985535, 27.0=820.5325, 108.0=872.8576, 109.0=588.6523, 110.0=28.94757, 111.0=873.0183, 7.0=595.79315, 28.0=688.15027, 112.0=594.3026, 113.0=29.837097, 114.0=872.9481, 115.0=599.14905, 29.0=31.453125, 116.0=31.290283, 117.0=872.4174, 30.0=812.00616, 31.0=688.1647}}
                break;
            default:
                LOGGER.warn("Unknown Lua function: {} : {}", luaName, lua);
                break;
        }
    }

    @Override
    public void finalizeAnalysis() {

    }

    private void handleGiveResourcesToPlayer(Command command, Map<Object, Object> lua) {
        Map<String, Object> msgData = (Map<String, Object>) lua.get("Msg");
        Object sender = lua.get("Sender");

        if (msgData != null) {
            if (sender != null) {
                ChatAnalyser.handleChat(command, replayToFill, msgData, sender.toString());
            } else {
                LOGGER.warn("Sender is null for GiveResourcesToPlayer: {}", msgData);
            }
        } else {
            int toId = ((Float) lua.get("To")).intValue();
            int fromId = ((Float) lua.get("From")).intValue();
            float energy = (float) lua.get("Energy");
            float mass = (float) lua.get("Mass");

            ReplayPlayer fromPlayer = fromId - 1 < replayToFill.getPlayers().size() ? replayToFill.getPlayers().get(fromId - 1) : null;
            if (fromPlayer != null) {
                fromPlayer.increaseEnergyShared(energy);
                fromPlayer.increaseMassShared(mass);
                LOGGER.debug("Increased energy and mass shared by player {} by {} and {} respectively", fromId, energy, mass);
            }
            ReplayPlayer toPlayer = toId - 1 < replayToFill.getPlayers().size() ? replayToFill.getPlayers().get(toId - 1) : null;
            if (toPlayer != null) {
                toPlayer.increaseMassReceived(mass);
                toPlayer.increaseEnergyReceived(energy);
                LOGGER.debug("Increased energy and mass received of player {} by {} and {} respectively", toId, energy, mass);
            }
        }
    }

    private void handleCopyOrders(Command command, Map<Object, Object> lua) {
        // TODO:
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
        // TODO:
        LOGGER.trace("OnControlGroupAssign: {}", lua);
        /*
        {Float@1491} 1.0 -> "4194312"
        {Float@1493} 2.0 -> "4194307"
        {Float@1495} 3.0 -> "4194310"
         */
    }

    private void handleRebuild(Command command, Map<Object, Object> lua) {
        // TODO:
        LOGGER.trace("Rebuild: {}", lua);
        /*
        "entity" -> "11534342"
        "Clear" -> {Boolean@1494} true
         */
    }

    private void handleRingWithStorages(Command command, Map<Object, Object> lua) {
        // TODO:
        LOGGER.trace("RingWithStorages: {}", lua);
        /*
        target -> 11534350
         */
    }

    private void handleAttackMove(Command command, Map<Object, Object> lua) {
        // TODO:
        LOGGER.trace("AttackMove: {}", lua);
        /*
        Target={1.0=88.30132, 2.0=63.195312, 3.0=674.70685}, Rotation=216.51114, Clear=false
         */
    }

    private void handleToggleSelfDestruct(Command command, Map<Object, Object> lua) {
        // TODO:
        LOGGER.trace("ToggleSelfDestruct: {}", lua);
        /*
        owner=8.0, noDelay=false
         */
    }

    private void handleSetRecallVote(Command command, Map<Object, Object> lua) {
        // TODO:
        LOGGER.trace("SetRecallVote: {}", lua);
        /*
            Vote=true, From=12.0
         */
    }

    private void handleGiveUnitsToPlayer(Command command, Map<Object, Object> lua) {
        // TODO:
        LOGGER.trace("GiveUnitsToPlayer: {}", lua);
        /*
        To=12.0, From=10.0
         */
    }

    private void handleSpawnSpecialPing(Command command, Map<Object, Object> lua) {
        // TODO:
        LOGGER.trace("SpawnSpecialPing: {}", lua);
        /*
        Type=nuke, Owner=10.0, Lifetime=10.0, Ring=/textures/ui/common/game/marker/ring_nuke04-blur.dds, ArrowColor=red, Mesh=nuke_marker, Sound=Aeon_Select_Radar, Location={1.0=626.73724, 2.0=63.195312, 3.0=398.43472}
         */
    }

    private void handleSpawnPing(Command command, Map<Object, Object> lua) {
        // TODO:
        LOGGER.trace("SpawnPing: {}", lua);
        /*
        Type=Marker, Owner=2.0, Lifetime=5.0, Ring=/game/marker/ring_yellow02-blur.dds, Color=ff66ffcc, ArrowColor=yellow, Sound=UI_Main_IG_Click, Marker=true, Name=nuke, Location={1.0=624.9585, 2.0=63.195312, 3.0=395.40198}
         */
    }

    private void handleImmediateHiveUpgrade(Command command, Map<Object, Object> lua) {
        // TODO:
        LOGGER.trace("ImmediateHiveUpgrade: {}", lua);
        /*
        UpgradeTo=xrb0204
         */
    }

    private void handleUpdateMarker(Command command, Map<Object, Object> lua) {
        // TODO:
        LOGGER.trace("UpdateMarker: {}", lua);
        /*
        Owner=1.0, Action=delete, ID=3.0
         */
    }

    private void handleAutoOvercharge(Command command, Map<Object, Object> lua) {
        // TODO:
        LOGGER.trace("AutoOvercharge: {}", lua);
        /*
        auto=true
         */
    }

    private void handleDistributeOrders(Command command, Map<Object, Object> lua) {
        // TODO:
        LOGGER.trace("DistributeOrders: {}", lua);
        /*
            ClearCommands=true, Target=5242933
         */
    }

    private void handleRingWithFabricators(Command command, Map<Object, Object> lua) {
        // TODO:
        LOGGER.trace("RingWithFabricators: {}", lua);
        /*
        allFabricators=true, target=10485835
         */
    }

    private void handleValidateAssist(Command command, Map<Object, Object> lua) {
        // TODO:
        LOGGER.trace("ValidateAssist: {}", lua);
        /*
        target=6291592
         */
    }

    private void handleWeaponPriorities(Command command, Map<Object, Object> lua) {
        // TODO:
        LOGGER.trace("WeaponPriorities: {}", lua);
        /*
            SelectedUnits={1.0=2097350}, prioritiesTable={categories.EXPERIMENTAL}, name=EXP, exclusive=false
         */
    }

    private void handleRingRadar(Command command, Map<Object, Object> lua) {
        // TODO:

    }

    private void handleRingArtilleryTech2(Command command, Map<Object, Object> lua) {
        // TODO:
    }

    private void handleFlagShield(Command command, Map<Object, Object> lua) {
        // TODO:
        // {target=150}
    }

    private void handleClearCommands(Command command, Map<Object, Object> lua) {
        // TODO:
        // {ids={}}
    }

    private void handleSetResourceSharing(Command command, Map<Object, Object> lua) {
        // TODO:
        // {Army=4.0, Value=false}
    }

    private void handleCheatSpawnUnit(Command command, Map<Object, Object> lua) {
        // TODO:
        // {rand=0.0, army=1.0, pos={1.0=394.5, 2.0=74.259766, 3.0=962.5}, bpId=xrl0302, veterancy=0.0, count=1.0, yaw=6.283185}
    }

    private void handleExtendAttackOrder(Command command, Map<Object, Object> lua) {
        // TODO:
        // {Origin={1.0=968.8932, 2.0=17.5, 3.0=570.55066}, Radius=4.2142015}
    }

    private void handleSelectHighestEngineerAndAssist(Command command, Map<Object, Object> lua) {
        // TODO:
        //{TargetId=9437213}
    }

    private void handleAbortNavigation(Command command, Map<Object, Object> lua) {
        // TODO:
        // {}
    }

    private void handleRingArtilleryTech3Exp(Command command, Map<Object, Object> lua) {
        // TODO:
        // {target=7340072}
    }
}
