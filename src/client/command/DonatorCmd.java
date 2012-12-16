package client.command;

import client.MapleClient;
import java.util.Collections;
import java.util.List;

public class DonatorCmd extends CommandHandler {


    public DonatorCmd(MapleClient client, char header, String[] args) {
        super(client, header, args);
    }

    /**
     * Allows for any code to be run before the command is executed.
     * Note: super.execute() is required for the command to execute.
     * 
     * @throws Exception Throws any exception that may occur while the command is executed.
     */
    @Override
    public void execute() throws Exception {
        if (client.isDonator()) {
            super.execute();
        }
    }
    
    @Command
    public void donator() {
        int pageMax = 8;
        int page = 1;
        try {
            page = Integer.parseInt(args[1]);
        } catch (Exception e) {
        }
        List<CommandInfo> commands = getCommandInfo();
        Collections.sort(commands, new CmdSortASC());
        int possiblePages = (int) (commands.size() / pageMax);
        if (commands.size() % pageMax > 0) {
            possiblePages++;
        }
        if (page > possiblePages) {
            page = possiblePages;
        } else if (page < 1) {
            page = 1;
        }
        if (commands.size() > 0) {
            int lastOfPage = (commands.size() - Math.max(0, commands.size() - (page * pageMax))) - 1;
            commands = commands.subList((page - 1) * pageMax, lastOfPage);
            chr.message("== Donator Commands " + page + "/" + possiblePages + " ==");
            chr.message("For help with commands type '@help commands'.");
            for (CommandInfo info : commands) {
                if (info != null) {
                    chr.message(info.toString());
                }
            }
        } else {
            chr.message("Sorry no donator command help yet.");
        }
    }
}
