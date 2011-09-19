package client.command;

import client.MapleCharacter;
import client.MapleClient;
import constants.ServerConstants;
import net.server.Channel;
import scripting.npc.NPCScriptManager;
import tools.MaplePacketCreator;

public class PlayerCmd {

    private static String[] arguments;

    public static boolean executeCommand(MapleClient c, String[] args) {
        arguments = args;
        boolean handled = true;
        MapleCharacter chr = c.getPlayer();
        Channel channel = c.getChannelServer();

        if (isCmd("dispose")) {
            NPCScriptManager.getInstance().dispose(c);
            c.announce(MaplePacketCreator.enableActions());
            chr.message("Done.");
        } else if (isCmd("rebirth")) {
            if (chr.getLevel() >= ServerConstants.REBIRTH_LEVEL || (chr.getLevel() >= ServerConstants.KOC_REBIRTH_LEVEL && chr.isCygnus())) {
                chr.rebirth();
                chr.message("Congratulations you have rebirthed! You now have " + chr.getRebirths() + " rebirths!");
            } else {
                chr.message("You are not high enough level to rebirth.");
            }
        } else if (isCmd("rape")) {
            if (chr.canPolRape()) {
                if (args.length == 1) {
                    chr.rape();
                    chr.message("You raped yourself... Wha???");
                } else if (args.length == 2) {
                    String targetName = args[1];
                    MapleCharacter target = channel.getPlayerStorage().getCharacterByName(targetName);
                    if (target != null) {
                        target.rape();
                        target.message("You have been rapped by " + chr.getName() + "!");
                        chr.message("You have raped " + target.getName() + "!");
                    } else {
                        chr.message("Unable to find a player named " + targetName + ".");
                    }
                } else {
                    handled = false;
                }
            } else {
                chr.message("You cannot use this command yet.");
                chr.message("For help with SSkills type '@help sskills'.");
            }
        } else {
            handled = false;
        }
        return handled;
    }

    private static boolean isCmd(String label) {
        return arguments[0].equalsIgnoreCase(label);
    }
}

/*
 *
- @kill → Kills a specified player.
- @rape → Makes a specified player lay down and unable to move for a short time.
- @heal → Heals a specified player.
- @curse → Puts a lot of curses on a player.
- @kick → Kicks a specified player out of the town.
- @bomb → Creates a bomb on the floor that will explode.
- @mimic → Changes your clothes and body to mimic what a player looks like. 
 *
 */
