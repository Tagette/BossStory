package client.command;

import client.MapleCharacter;
import client.MapleClient;
import tools.StringUtil;

public class CommandProcessor {

    public static boolean processCommand(MapleClient c, char heading, String[] args) {
        boolean isCommand = true;
        MapleCharacter chr = c.getPlayer();
        
        try {
            PlayerCmd playerCmd = new PlayerCmd(c, '@', args);
            DonatorCmd donatorCmd = new DonatorCmd(c, '@', args);
            VIPCmd vipCmd = new VIPCmd(c, '@', args);
            InternCmd internCmd = new InternCmd(c, '!', args);
            GMCmd gmCmd = new GMCmd(c, '!', args);
            SuperCmd superCmd = new SuperCmd(c, '!', args);
            AdminCmd adminCmd = new AdminCmd(c, '!', args);

            String command = heading + StringUtil.joinStringFrom(args, 0);
            switch (heading) {
                case '!':
                    if (chr.isGM()) {
                        adminCmd.execute();
                        if (!adminCmd.handled()) {
                            superCmd.execute();
                            if (!superCmd.handled()) {
                                gmCmd.execute();
                                if (!gmCmd.handled()) {
                                    internCmd.execute();
                                    if (!internCmd.handled()) {
                                        chr.message("Unknown command: '" + command + "'");
                                    } else {
                                        System.out.println("Intern command executed: '" + command + "'");
                                    }
                                } else {
                                    System.out.println("GM command executed: '" + command + "'");
                                }
                            } else {
                                System.out.println("Super command executed: '" + command + "'");
                            }
                        } else {
                            System.out.println("Admin command executed: '" + command + "'");
                        }
                    } else {
                        isCommand = false;
                    }
                    break;
                case '@':
                    playerCmd.execute();
                    if (!playerCmd.handled()) {
                        vipCmd.execute();
                        if(!vipCmd.handled()) {
                            donatorCmd.execute();
                            if(!donatorCmd.handled()) {
                                chr.message("Unknown command: '" + command + "'");
                            }
                        }
                    }
                    break;
                default:
                    isCommand = false;
                    break;
            }
        } catch (Exception ex) {
            chr.message("An error occured while performing that command. Please report this.");
            ex.printStackTrace();
        }
        return isCommand;
    }
}