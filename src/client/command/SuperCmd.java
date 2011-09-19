package client.command;

import client.MapleCharacter;
import client.MapleClient;
import net.server.Channel;

public class SuperCmd {
    
    private static String[] arguments;

    public static boolean executeCommand(MapleClient c, String[] args) {
        arguments = args;
        boolean handled = true;
        MapleCharacter chr = c.getPlayer();
        Channel channel = c.getChannelServer();

        if (isCmd("")) {
        } else {
            handled = false;
        }
        return handled;
    }
    
    private static boolean isCmd(String label){
        return arguments[0].equalsIgnoreCase(label);
    }
}
