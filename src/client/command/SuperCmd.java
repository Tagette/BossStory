package client.command;

import client.MapleCharacter;
import client.MapleClient;
import client.groups.MapleGroup;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import net.server.Server;
import server.life.MapleLifeFactory;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.StringUtil;

public class SuperCmd extends CommandHandler {

    public SuperCmd(MapleClient client, char header, String[] args) {
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
        // Checks to see if the character is in the correct group. (gm level)
        if (MapleGroup.SUPER.atleast(chr)) {
            super.execute();
        }
    }
    
    @Command
    public void supergm() {
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
            chr.message("== Super GM Commands " + page + "/" + possiblePages + " ==");
            chr.message("For help with commands type '@help commands'.");
            for (CommandInfo info : commands) {
                if (info != null) {
                    chr.message(info.toString());
                }
            }
        } else {
            chr.message("Sorry no Super GM command help yet.");
        }
    }

    @Command
    @Syntax("![promote|demote] [name]")
    @Description("Promotes or demotes [name] up or down 1 gm level.")
    @Alias({"demote"})
    public void promote() {
        MapleCharacter victim = world.getPlayerStorage().getCharacterByName(args[1]);
        int newLevel = 0;
        boolean promote = true;
        args[0] = args[0].toLowerCase();
        if (args[0].equalsIgnoreCase("demote")) {
            promote = false;
        }
        Connection con = DatabaseConnection.getConnection();
        if (victim != null) {
            newLevel = victim.gmLevel();
            if (promote) {
                newLevel++;
            } else {
                newLevel--;
            }
            if (newLevel > MapleGroup.ADMIN.getId()) {
                chr.message("That user is already an Admin.");
                victim.message(chr.getName() + " has attemped to promote you.");
            } else if (newLevel < 0) {
                chr.message("That user is already a Non-GM.");
            } else {
                if (chr.gmLevel() > victim.gmLevel()) {
                    victim.setGMLevel(newLevel);
                    if (promote) {
                        victim.message("Congratulations you have been " + args[0] + "d to " + MapleGroup.getById(newLevel) + " by " + chr.getName() + "!");
                    } else {
                        victim.message("You have been " + args[0] + "d to " + MapleGroup.getById(newLevel) + " by " + chr.getName() + ".");
                    }
                    chr.message("You " + args[0] + "d " + victim.getName() + " to " + MapleGroup.getById(newLevel) + ".");
                } else {
                    chr.message("You must have a higher gm level then " + victim.getName() + " to change his/her gm level.");
                    victim.message(chr + " has attemped to " + args[0] + " you.");
                }
            }
        } else {
            String msg = "";
            try {
                PreparedStatement ps = con.prepareStatement("SELECT gm FROM characters WHERE name = ?");
                ps.setString(1, args[1]);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    newLevel = rs.getInt("gm");
                    if (chr.gmLevel() > newLevel) {
                        if (promote) {
                            newLevel++;
                        } else {
                            newLevel--;
                        }
                        if (newLevel > MapleGroup.ADMIN.getId()) {
                            chr.message("That user is already an Admin.");
                        } else if (newLevel < 0) {
                            chr.message("That user is already a Non-GM.");
                        } else {
                            ps.close();
                            ps = con.prepareStatement("UPDATE characters SET gm = ?, lastGm = ? WHERE name = ?");
                            ps.setInt(1, newLevel);
                            ps.setInt(2, newLevel);
                            ps.setString(3, args[1]);
                            ps.executeUpdate();
                            if (promote) {
                                chr.message("You have offline promoted " + args[1] + ".");
                                msg = "Congratulations you have been promoted to " + MapleGroup.getById(newLevel) + " by " + chr.getName() + "!";
                            } else {
                                chr.message("You have offline demoted " + args[1] + ".");
                                msg = "You have been demoted to " + MapleGroup.getById(newLevel) + " by " + chr.getName() + ".";
                            }
                            chr.sendNote(args[1], "BossStory", msg);
                        }
                    } else {
                        chr.message("You can only change the gm level of a user who has a lower gm level then you.");
                        msg = chr.getName() + " attempted to offline " + args[0] + " you.";
                        chr.sendNote(args[1], "BossStory", msg);
                    }
                } else {
                    chr.message(args[1] + " doesn't exist!");
                    chr.message("Syntax: !" + args[0] + " [user]");
                    chr.message("Warning: You can only change the gm level of a user who has a lower gm level then you.");
                }
                rs.close();
                ps.close();
            } catch (Exception e) {
                chr.message("Unable to offline " + args[0] + " " + args[1] + ": " + e);
            }
        }
    }

    @Command
    @Syntax("!speak [name] [message]")
    @Description("Makes [name] say [message].")
    public void speak() {
        MapleCharacter victim = world.getPlayerStorage().getCharacterByName(args[1]);
        if (victim != null) {
            victim.forceSpeak(StringUtil.joinStringFrom(args, 2, " "), victim.isGM(), 0);
        } else {
            chr.message("The user is offline or doesn't exist.");
        }
    }

    @Command
    @Syntax("!cnotice|cnnotice [type] [message]")
    @Description("Sends a custom notice to the server with [type]. !cnotice for a list.")
    @Alias({"cnnotice", "cn", "cnn"})
    public void cnotice() {
        if (args.length == 1) {
            chr.message("Message types:");
            chr.message("orange - Orange Text");
            chr.message("pink - Pink Text");
            chr.message("purple - Purple Text");
            chr.message("lgreen - Light Green Text");
            chr.message("green - Green Text");
            chr.message("red - Red Text");
            chr.message("blue - Blue Text");
            chr.message("yellow - Yellow Text");
            chr.message("popup - A blue popup shows with your message.");
        } else {
            String type = "[Notice] ";
            if (args[0].equalsIgnoreCase("cnotice") || args[0].equalsIgnoreCase("cn")) {
                type = "[" + chr.getName() + "] ";
            }
            String text = StringUtil.joinStringFrom(args, 2);
            int color = -1;
            //MultiChat
            if (args[1].equalsIgnoreCase("orange")) {
                color = 1;
            } else if (args[1].equalsIgnoreCase("pink")) {
                color = 2;
            } else if (args[1].equalsIgnoreCase("purple")) {
                color = 3;
            } else if (args[1].equalsIgnoreCase("lgreen")) {
                color = 4;
                //ServerNotice
            } else if (args[1].equalsIgnoreCase("red")) {
                color = 5;
            } else if (args[1].equalsIgnoreCase("blue")) {
                color = 6;
                //Whisper
            } else if (args[1].equalsIgnoreCase("green")) {
                color = 8;
                //MapleTip
            } else if (args[1].equalsIgnoreCase("yellow")) {
                color = 9;
            } else if (args[1].equalsIgnoreCase("popup")) {
                color = 10;
            } else {
                chr.message("Syntax: !cnotice - Displays a list of chat types.");
                chr.message("Syntax: !cnotice [type] [message] - Sends a custom message to the world.");
                chr.message("Syntax: !cnnotice [type] [message] - Sends a custom notice to the world.");
            }
            if (color >= 0 && color < 10) {
                switch (color) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        //MultiChat
                        Server.getInstance().broadcastMessage(chr.getWorld(), MaplePacketCreator.multiChat(type, text, color - 1));
                        break;
                    case 5:
                    case 6:
                        //Server Notice
                        Server.getInstance().broadcastMessage(chr.getWorld(), MaplePacketCreator.serverNotice(color, type + text));
                        break;
                    case 8:
                        //Whisper
                        Server.getInstance().broadcastMessage(chr.getWorld(), MaplePacketCreator.getWhisper(type, client.getChannel(), text));
                        break;
                    case 9:
                        //MapleTip
                        Server.getInstance().broadcastMessage(chr.getWorld(), MaplePacketCreator.sendYellowTip(type + text));
                        break;
                }
            } else if (color >= 10) {
                try {
                    Server.getInstance().broadcastMessage(chr.getWorld(), MaplePacketCreator.serverNotice(1, type + text));
                } catch (Exception e) {
                }
            }
        }
    }

    @Command
    @Description("Saves everyones account to the database.")
    public void saveall() {
        chr.message("Saving...");
        world.saveAll();
        chr.message("All saved.");
    }

    @Command
    @Syntax("!addvp (name) [amount]")
    @Description("Adds [amount] vote points for (name).")
    public void addvp() {
        if (args.length == 2) {
            try {
                int amount = Integer.parseInt(args[1]);
                chr.setVotePoints(amount + chr.getVotePoints());
            } catch (Exception e) {
            }

        } else if (args.length == 3) {
            try {
                String name = args[1];
                int amount = Integer.parseInt(args[2]);
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE name = ?");
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int accountId = rs.getInt("accountid");
                    rs.close();
                    ps.close();
                    ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
                    ps.setInt(1, accountId);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        int cVp = rs.getInt("votepoints");
                        rs.close();
                        ps.close();
                        ps = con.prepareStatement("UPDATE accounts SET votepoints = ? WHERE id = ?");
                        ps.setInt(1, cVp + amount);
                        ps.setInt(2, accountId);
                        ps.executeUpdate();
                    }
                } else {
                    chr.message(name + " was not found.");
                }
                ps.close();
            } catch (Exception e) {
            }
        }
    }

    @Command
    @Description("Summons horntail where your standing.")
    public void horntail() {
        chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8810026), chr.getPosition());
    }

    @Command
    @Description("Summons a pink bean where your standing.")
    public void pinkbean() {
        chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8820009), chr.getPosition());
    }

    @Command
    @Description("Summons zakum where you are standing.")
    public void zakum() {
        chr.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), chr.getPosition());
        for (int x = 8800003; x < 8800011; x++) {
            chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(x), chr.getPosition());
        }
    }

    @Command
    @Syntax("!takecharacter [name]")
    @Description("Takes a character from [name]'s account to yours.")
    @SuppressWarnings("CallToThreadDumpStack")
    public void takecharacter() {
        String name = args[1];
        try {
            MapleCharacter victim = world.getPlayerStorage().getCharacterByName(name);
            if (victim != null && victim.getAccountID() == victim.getOriginalAccountID()) {
                // DC the player
                victim.getClient().getSession().close();
                victim.getClient().disconnect();
                victim.getClient().takeCharacter();
                victim.saveToDB(true);
                world.removePlayer(victim);
            }

            int victimAId = -1, victimCID = -1;
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                victimAId = rs.getInt("origaccid");
                victimCID = rs.getInt("accountid");
                if (victimAId == victimCID) {
                    ps = con.prepareStatement("UPDATE characters SET accountid = ? WHERE name = ?");
                    ps.setInt(1, client.getAccID());
                    ps.setString(2, name);
                    ps.executeUpdate();
                    ps.close();

                    chr.message(name + " was successfully transfered to your account. Log out to log onto " + name + "'s character.");
                } else {
                    chr.message("This character has already been taken by an admin.");
                }
            } else {
                chr.message("There is no character in the database with that name. Name: " + name);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            chr.message("Unable to transfer character. Name: " + name);
            System.out.println(e.getSQLState());
            e.printStackTrace();
        }
    }

    @Command
    @Syntax("!returncharacter [name]")
    @Description("Returns a character of [name]'s back to thier account.")
    @SuppressWarnings("CallToThreadDumpStack")
    public void returncharacter() {
        String name = args[1];
        try {
            Connection con = DatabaseConnection.getConnection();
            MapleCharacter victim = world.getPlayerStorage().getCharacterByName(name);
            if (victim != null && victim.getAccountID() != victim.getOriginalAccountID()) {
                victim.getClient().getSession().close();
                victim.getClient().disconnect();
                victim.setNeedsHelp(false);
                victim.getClient().returnCharacter();
                victim.saveToDB(true);
                world.removePlayer(victim);
            }
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt("accountid") != rs.getInt("origaccid")) {
                    int victimAId = rs.getInt("origaccid");
                    ps = con.prepareStatement("UPDATE characters SET accountid = ? WHERE name = ?");
                    ps.setInt(1, victimAId);
                    ps.setString(2, name);
                    ps.executeUpdate();
                    ps.close();

                    chr.message(name + " was successfully returned.");
                } else {
                    chr.message(name + " was not taken by an admin.");
                }
            } else {
                chr.message("There is no character in the database with that name. Name: " + name);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            chr.message("Unable to transfer character. Name: " + name);
            System.out.println(e.getSQLState());
            e.printStackTrace();
        }
    }
}
