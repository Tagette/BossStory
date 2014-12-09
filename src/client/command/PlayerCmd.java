package client.command;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.command.CommandHandler.CmdSortASC;
import client.groups.MapleGroup;
import client.powerskills.Politics;
import client.powerskills.PowerSkillType;
import constants.ServerConstants;
import java.util.Collections;
import java.util.List;
import net.server.Channel;
import net.server.Server;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.Randomizer;

public class PlayerCmd extends CommandHandler {
    
    public PlayerCmd(MapleClient client, char header, String[] args) {
        super(client, header, args);
    }

    /**
     * Allows for any code to be run before the command is executed. Note:
     * super.execute() is required for the command to execute.
     *
     * @throws Exception Throws any exception that may occur while the command
     * is executed.
     */
    @Override
    public void execute() throws Exception {
        // Checks to see if the character is in the correct group. (gm level)
        if (MapleGroup.PLAYER.atleast(chr)) {
            if ((!chr.canTalk() || chr.inJail()) && !chr.isGM()) {
                chr.message(1, "Your chat has been disabled.");
                setHandled(true);
            } else if (chr.inTutorial() && hasCommand(args[0])
                    && !hasTag(args[0], "tutorial") && !chr.isGM()) {
                chr.message("Please wait until you are out of the tutorial to "
                        + "use that command. If you need help type @help or @callgm.");
                setHandled(true);
            } else {
                super.execute();
            }
        }
    }
    
    @Command
    @Tags({"tutorial"})
    public void commands() {
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
            chr.message("== Player Commands " + page + "/" + possiblePages + " ==");
            chr.message("For help with commands type '@help commands'.");
            for (CommandInfo info : commands) {
                if (info != null) {
                    chr.message(info.toString());
                }
            }
        } else {
            chr.message("Sorry no player command help yet.");
        }
    }
    
    @Command
    @Description("Fixes you if you are stuck.")
    @Alias({"fix", "unbug"})
    @Tags({"tutorial"})
    public void dispose() {
        NPCScriptManager.getInstance().dispose(client);
        client.announce(MaplePacketCreator.enableActions());
        chr.message("You have been fixed.");
    }
    
    @Command
    @Description("Saves your account.")
    @Tags({"tutorial"})
    public void save() {
        long saveStart = System.currentTimeMillis();
        chr.saveToDB(true);
        chr.message("Saved in " + ((double)((System.currentTimeMillis() - saveStart) / 1000)) + " seconds.");
    }
    
    @Command
    @Description("Lists all players currently online.")
    @Tags({"tutorial"})
    public void online() {
        String message;
        int totalOnline = 0, online = 0;
        for (MapleCharacter p : world.getPlayerStorage().getAllCharacters()) {
            if (!p.isGM()) {
                totalOnline++;
            }
        }
        if (totalOnline > 1) {
            chr.message("Characters online(" + totalOnline + "):");
            for (Channel cservs : world.getChannels()) {
                online = 0;
                for (MapleCharacter p : cservs.getPlayerStorage().getAllCharacters()) {
                    if (!p.isGM()) {
                        online++;
                    }
                }
                if (online > 0) {
                    chr.message("Channel " + cservs.getId() + "(" + online + "):");
                    message = "- ";
                    for (MapleCharacter p : cservs.getPlayerStorage().getAllCharacters()) {
                        if (!p.isGM()) {
                            message += p.getName() + ", ";
                        }
                    }
                    chr.message(message.substring(0, message.length() - 2));
                }
            }
        } else {
            chr.message("You are the only one on.");
        }
    }
    
    @Command
    @Description("Opens the rebirth NPC. '@help rebirth'")
    @Alias({"rb"})
    public void rebirth() {
        if (chr.getLevel() >= ServerConstants.REBIRTH_LEVEL) {
            chr.addTransferedInfo("codyrebirth", "rebirth");
            NPCScriptManager.getInstance().start(client, 9200000); //Cody
            client.getSession().write(MaplePacketCreator.enableActions());
        } else {
            chr.message("You are not level " + ServerConstants.REBIRTH_LEVEL + ".");
        }
    }
    
    @Command
    @Description("Performs a rebirth without opening an NPC. '@help rebirth'")
    @Alias({"frb"})
    public void fastrebirth() {
        if (chr.getLevel() >= ServerConstants.REBIRTH_LEVEL) {
            chr.rebirth();
        } else {
            chr.message("You are not level " + ServerConstants.REBIRTH_LEVEL + ".");
        }
    }
    
    @Command
    @Syntax("@help (topic)")
    @Description("Opens the help npc for help with (topic).")
    @Tags({"tutorial"})
    public void help() {
        NPCScriptManager.getInstance().start(client, 22000);
        client.announce(MaplePacketCreator.enableActions());
        chr.message("For commands type '@commands'.");
    }
    
    @Command
    @Syntax("@rape (name)")
    @Description("Rapes (name). '@help politics'")
    public void rape() {
        Politics politics = (Politics) chr.getPowerSkill(PowerSkillType.POLITICS);
        if (politics.useRape()) {
            if (args.length == 1) {
                chr.rape(politics.getRapeDuration());
                chr.getMap().broadcastMessage(chr, MaplePacketCreator.serverNotice(5, chr.getName() + " raped " + chr.getName() + "... Wha??"));
            } else if (args.length == 2) {
                String targetName = args[1];
                MapleCharacter target = chr.getMap().getCharacterByName(targetName);
                if (target != null) {
                    target.rape(politics.getRapeDuration());
                    chr.getMap().broadcastMessage(chr, MaplePacketCreator.serverNotice(5, chr.getName() + " raped " + target.getName() + "."));
                } else {
                    chr.message("Unable to find a player named " + targetName + " in your map.");
                }
            } else {
                chr.message("Syntax: @rape (name)");
            }
        } else {
            chr.message("You cannot use this command yet.");
            chr.message("For help with power skills type '@help powerskills'.");
        }
    }
    
    @Command
    @Syntax("@heal (name)")
    @Description("Heals (name). '@help politics'")
    public void heal() {
        Politics politics = (Politics) chr.getPowerSkill(PowerSkillType.POLITICS);
        if (politics.useHeal()) {
            if (args.length == 1) {
                chr.setHpMp(30000);
            } else if (args.length == 2) {
                String targetName = args[1];
                MapleCharacter target = channel.getPlayerStorage().getCharacterByName(targetName);
                if (target != null) {
                    target.setHpMp(30000);
                    target.message("You been healed by " + chr.getName() + ".");
                    chr.message("You have healed " + target.getName() + ".");
                } else {
                    chr.message("Unable to find a player named " + targetName + ".");
                }
            } else {
                chr.message("Syntax: " + this.getCommand(label).syntax);
            }
        } else {
            chr.message("You cannot use this command yet.");
            chr.message("For help with power skills type '@help powerskills'.");
        }
    }
    
    @Command
    @Syntax("@kill [name]")
    @Description("Kills [name]. '@help politics'")
    public void kill() {
        Politics politics = (Politics) chr.getPowerSkill(PowerSkillType.POLITICS);
        if (politics.useKill()) {
            if (chr.inTown()) {
                if (args.length == 1) {
                    chr.message("You killed yourself.. why?");
                    chr.setHpMp(0);
                } else if (args.length == 2) {
                    String targetName = args[1];
                    MapleCharacter target = channel.getPlayerStorage().getCharacterByName(targetName);
                    if (target != null) {
                        if (chr.getMapId() == target.getMapId()) {
                            target.setHpMp(0);
                            target.message("You been killed by " + chr.getName() + ".");
                            chr.message("You have killed " + target.getName() + ".");
                        } else {
                            chr.message("That person must be in the same map as you.");
                        }
                    } else {
                        chr.message("Unable to find a player named " + targetName + ".");
                    }
                } else {
                    chr.message("Syntax: @kill (name)");
                }
            } else {
                chr.message("Politics are only useful inside town.");
            }
        } else {
            chr.message("You cannot use this command yet.");
            chr.message("For help with power skills type '@help powerskills'.");
        }
    }
    
    @Command
    @Syntax("@kick [name]")
    @Description("Kicks [name] out of town. '@help politics'")
    public void kick() {
        Politics politics = (Politics) chr.getPowerSkill(PowerSkillType.POLITICS);
        if (politics.useKick()) {
            if (args.length == 2) {
                if (chr.inTown()) {
                    String targetName = args[1];
                    MapleCharacter target = channel.getPlayerStorage().getCharacterByName(targetName);
                    if (target != null) {
                        if (chr.getMapId() == target.getMapId()) {
                            target.changeMap(910000001); // FM1
                            target.message("You been kicked out of town by " + chr.getName() + ".");
                            chr.message("You have kicked " + target.getName() + " out of town.");
                        } else {
                            chr.message("That person must be in the same map as you.");
                        }
                    } else {
                        chr.message("Unable to find a player named " + targetName + ".");
                    }
                } else {
                    chr.message("You must be in a town before you can kick someone out of town silly.");
                }
            }
        } else {
            chr.message("You cannot use this command yet.");
            chr.message("For help with power skills type '@help powerskills'.");
        }
    }
    
    @Command
    @Syntax("@[str|dex|int|luk] [amount]")
    @Description("Adds [amount] to the selected stat. '@help stat'")
    @Alias({"dex", "int", "luk"})
    @Tags({"tutorial"})
    public void str() {
        int x = 0, max = ServerConstants.MAX_STAT;
        if (args[1].equalsIgnoreCase("all")) {
            x = chr.getRemainingAp();
        } else {
            x = Integer.parseInt(args[1]);
        }
        if (x > 0 && x <= chr.getRemainingAp() && x < max - 4) {
            if (args[0].equalsIgnoreCase("str") && x + chr.getStr() < max) {
                chr.addStat(1, x);
            } else if (args[0].equalsIgnoreCase("dex") && x + chr.getDex() < max) {
                chr.addStat(2, x);
            } else if (args[0].equalsIgnoreCase("int") && x + chr.getInt() < max) {
                chr.addStat(3, x);
            } else if (args[0].equalsIgnoreCase("luk") && x + chr.getLuk() < max) {
                chr.addStat(4, x);
            } else {
                chr.message("The stat cannot exceed " + max + ".");
                return;
            }
            chr.message(x + " AP has been moved to " + args[0].toUpperCase() + ".");
            chr.setRemainingAp(chr.getRemainingAp() - x);
            chr.updateSingleStat(MapleStat.AVAILABLEAP, chr.getRemainingAp());
        } else {
            chr.message("You do not have enough AP.");
        }
    }
    
    @Command
    @Syntax("@autoap [str|dex|int|luk]")
    @Description("Automatically adds stats when leveling to the selected stat.")
    @Tags({"tutorial"})
    public void autoap() {
        int newAP = 0;
        if (args[1].equalsIgnoreCase("str")) {
            chr.setAutoAp(1);
            chr.addStat(1, chr.getRemainingAp());
            chr.message("Your ap will automatically go to str.");
        } else if (args[1].equalsIgnoreCase("dex")) {
            chr.setAutoAp(2);
            chr.addStat(2, chr.getRemainingAp());
            chr.message("Your ap will automatically go to dex.");
        } else if (args[1].equalsIgnoreCase("int")) {
            chr.setAutoAp(3);
            chr.addStat(3, chr.getRemainingAp());
            chr.message("Your ap will automatically go to int.");
        } else if (args[1].equalsIgnoreCase("luk")) {
            chr.setAutoAp(4);
            chr.addStat(4, chr.getRemainingAp());
            chr.message("Your ap will automatically go to luk.");
        } else if (args[1].equalsIgnoreCase("store")) {
            chr.setAutoAp(5);
            chr.updateStoredAp(chr.getRemainingAp());
            chr.message("Your ap will automatically go to ap storage.");
        } else if (args[1].equalsIgnoreCase("remove")) {
            chr.removeAutoAp();
            newAP = chr.getRemainingAp();
            chr.message("Auto-ap removed.");
        } else {
            newAP = chr.getRemainingAp();
            chr.message("Unknown command: " + command);
            chr.message("@autoap [str/dex/int/luk/store/remove]");
        }
        chr.setRemainingAp(newAP);
        chr.updateSingleStat(MapleStat.AVAILABLEAP, chr.getRemainingAp());
    }
    
    @Command
    @Syntax("@resetap [str|dex|int|luk]")
    @Description("Resets the selected stat to 4. '@help resetap'")
    @Tags({"tutorial"})
    public void resetap() {
        int newAP = chr.getRemainingAp();
        if (args.length == 1) {
            newAP += chr.getStr() + chr.getDex() + chr.getInt() + chr.getLuk() - 16;
            chr.setStr(4);
            chr.updateSingleStat(MapleStat.STR, 4);
            chr.setDex(4);
            chr.updateSingleStat(MapleStat.DEX, 4);
            chr.setInt(4);
            chr.updateSingleStat(MapleStat.INT, 4);
            chr.setLuk(4);
            chr.updateSingleStat(MapleStat.LUK, 4);
            chr.message("You have reset your stats to 4.");
        } else if (args[1].equalsIgnoreCase("str")) {
            newAP += chr.getStr() - 4;
            chr.setStr(4);
            chr.updateSingleStat(MapleStat.STR, 4);
            chr.message("You have reset your Strength to 4.");
        } else if (args[1].equalsIgnoreCase("dex")) {
            newAP += chr.getDex() - 4;
            chr.setDex(4);
            chr.updateSingleStat(MapleStat.DEX, 4);
            chr.message("You have reset your Dexterity to 4.");
        } else if (args[1].equalsIgnoreCase("int")) {
            newAP += chr.getInt() - 4;
            chr.setInt(4);
            chr.updateSingleStat(MapleStat.INT, 4);
            chr.message("You have reset your Intelligence to 4.");
        } else if (args[1].equalsIgnoreCase("luk")) {
            newAP += chr.getLuk() - 4;
            chr.setLuk(4);
            chr.updateSingleStat(MapleStat.LUK, 4);
            chr.message("You have reset your Luck to 4.");
        } else if (args[1].equalsIgnoreCase("all")) {
            newAP += chr.getStr() + chr.getDex() + chr.getInt() + chr.getLuk() - 16;
            chr.setStr(4);
            chr.updateSingleStat(MapleStat.STR, 4);
            chr.setDex(4);
            chr.updateSingleStat(MapleStat.DEX, 4);
            chr.setInt(4);
            chr.updateSingleStat(MapleStat.INT, 4);
            chr.setLuk(4);
            chr.updateSingleStat(MapleStat.LUK, 4);
            chr.message("You have reset your stats to 4.");
        } else {
            chr.message("Unknown command: " + command);
            chr.message("@autoap [str/dex/int/luk/store/remove]");
        }
        if (newAP > ServerConstants.MAX_STAT) {
            int toStore = newAP - ServerConstants.MAX_STAT;
            chr.setStoredAp(chr.getStoredAp() + toStore);
            chr.message("You have maxed how much unused ap you can hold at a time. " + toStore + " ap has been stored for a total of " + chr.getStoredAp() + " ap in storage.");
            chr.message("To manage your stored ap talk to Cody in Henesys.");
            newAP = ServerConstants.MAX_STAT;
        }
        chr.setRemainingAp(newAP);
        chr.updateSingleStat(MapleStat.AVAILABLEAP, chr.getRemainingAp());
    }
    
    @Command
    @Syntax("@checkstats (name)")
    @Description("Opens the stats for (name). '@help checkstats'")
    @Alias({"charinfo"})
    @Tags({"tutorial"})
    public void checkstats() {
        if(args.length > 1)
            chr.addTransferedInfo("checkstats", args[1]);
        NPCScriptManager.getInstance().start(client, 2091001);//Do Gong
        client.announce(MaplePacketCreator.enableActions());
    }
    
    @Command
    @Description("Sends a message to all online GM's for help. '@help callgm'")
    @Tags({"tutorial"})
    public void callgm() {
        if (!chr.isGM() && !chr.needsHelp()) {
            chr.setNeedsHelp(true);
            try {
                Server.getInstance().broadcastGMMessage(chr.getWorld(), MaplePacketCreator.serverNotice(5, "[GM] : " + chr.getName() + " needs your help!"));
                chr.message("You have sent a message to all gm's online. To cancel type @nogm.");
            } catch (Exception e) {
            }
        } else if (chr.needsHelp()) {
            chr.message("You have already sent a message to all online gm's. We will help you when we can. To cancel type @nogm.");
        }
    }
    
    @Command
    @Description("Cancels a request for help from the GM's. '@help nogm'")
    @Tags({"tutorial"})
    public void nogm() {
        if (!chr.isGM() && chr.needsHelp()) {
            chr.setNeedsHelp(false);
            try {
                Server.getInstance().broadcastGMMessage(chr.getWorld(), MaplePacketCreator.serverNotice(5, "[GM] : " + chr.getName() + " no longer needs your help."));
                chr.message("You have canceled the GM request.");
            } catch (Exception e) {
            }
        } else if (!chr.needsHelp()) {
            chr.message("You did not send a request to GM's for help.");
        }
    }
    
    @Command
    @Description("Seriously.. You get free money!")
    public void freemoney() {
        if (Randomizer.nextInt(10000) != 59764 + Randomizer.nextInt(20) - 10) {
            chr.setHpMp(0);
            chr.message("Theres no such thing as free money...");
        } else {
            chr.message("OMG FREE MONEY NO WAY!");
            MapleInventoryManipulator.addById(client, ServerConstants.SILVER_$_ID, (short) 1);
        }
    }
}
