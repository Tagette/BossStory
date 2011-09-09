package client.command;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import constants.ServerConstants;
import java.util.ArrayList;
import java.util.List;
import scripting.npc.NPCScriptManager;
import tools.MaplePacketCreator;
import tools.Pair;

public class PlayerCmd {

    public static boolean executeCommand(MapleClient c, String[] args) {
        boolean handled = true;
        MapleCharacter chr = c.getPlayer();

        if (args[0].equals("dispose")) {
            NPCScriptManager.getInstance().dispose(c);
            c.announce(MaplePacketCreator.enableActions());
            chr.message("Done.");
        } else if (args[0].equals("rape")) {
            List<Pair<MapleBuffStat, Integer>> list = new ArrayList<Pair<MapleBuffStat, Integer>>();
            list.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, 8));
            list.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.CONFUSE, 1));
            chr.announce(MaplePacketCreator.giveBuff(0, 0, list));
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.giveForeignBuff(chr.getId(), list));
        } else if (args[0].equals("rebirth")) {
            if(chr.getLevel() >= ServerConstants.REBIRTH_LEVEL || (chr.getLevel() >= ServerConstants.KOC_REBIRTH_LEVEL && chr.isCygnus())){
                chr.rebirth();
                chr.message("Congratulations you have rebirthed! You now have " + chr.getRebirths() + " rebirths!");
            } else {
                chr.message("You are not high enough level to rebirth.");
            }
        } else {
            handled = false;
        }
        return handled;
    }
}
