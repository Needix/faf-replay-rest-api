package de.needix.games.faf.replay.analyser.eventanalyser;

import de.needix.games.faf.replay.analyser.parser.Command;

public class UnitBuildOrderAnalyser implements CommandAnalyser {
    @Override
    public void analyseCommand(Command command) {
        // 2025-01-05T05:31:14.635+01:00 DEBUG 98680 --- [replay-rest-api (Dev)] [http-nio-8080-exec-1] d.n.g.f.replay.analyser.ReplayAnalyser   : Analysing command: 21287 CommandType.ISSUE_COMMAND 5 {entity_ids_set={units_number=11, unit_ids=[5242935, 5243049, 5243061, 5243062, 5243083, 5243100, 5243112, 5243116, 5243121, 5243124, 5243126]}, cmd_data={arg3=false, arg2=-1, command_id=83886084, arg5=null, arg4=[B@335e3a3b, cells=null, command_type=UNITCOMMAND_BuildMobile, arg1=-1, formation=null, target={position={x=386.0, y=62.3, z=718.0}, target=2}, blueprint_id=xsb0103}, type=issue}
    }

    @Override
    public void finalizeAnalysis() {

    }
}
