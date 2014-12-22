package client.command;

import client.MapleCharacter;
import client.MapleClient;
import client.groups.MapleGroup;
import constants.ServerConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import net.server.Server;
import server.TimerManager;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.maps.*;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.StringUtil;

public class AdminCmd extends CommandHandler {

    public AdminCmd(MapleClient client, char header, String[] args) {
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
        if (MapleGroup.ADMIN.atleast(chr)) {
            super.execute();
        }
    }
    
    @Command
    public void admin() {
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
            chr.message("== Admin Commands " + page + "/" + possiblePages + " ==");
            chr.message("For help with commands type '@help commands'.");
            for (CommandInfo info : commands) {
                if (info != null) {
                    chr.message(info.toString());
                }
            }
        } else {
            chr.message("Sorry no Admin command help yet.");
        }
    }

    @Command
    @Syntax("!pmob [mobid] [mobtime]")
    @Description("Creates a permanent monster spawn where your standing.")
    @SuppressWarnings("CallToThreadDumpStack")
    public void pmob() {
        int mobId = Integer.parseInt(args[1]);
        int mobTime = Integer.parseInt(args[2]);
        int xpos = chr.getPosition().x;
        int ypos = chr.getPosition().y;
        int fh = chr.getMap().getFootholds().findBelow(chr.getPosition()).getId();
        if (args[2] == null) {
            mobTime = 0;
        }
        MapleMonster mob = MapleLifeFactory.getMonster(mobId);
        if (mob != null && !mob.getName().equals("MISSINGNO")) {
            mob.setPosition(chr.getPosition());
            mob.setCy(ypos);
            mob.setRx0(xpos + 50);
            mob.setRx1(xpos - 50);
            mob.setFh(fh);
            mob.setCustom(true);
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid, mobtime ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                ps.setInt(1, mobId);
                ps.setInt(2, 0);
                ps.setInt(3, fh);
                ps.setInt(4, ypos);
                ps.setInt(5, xpos + 50);
                ps.setInt(6, xpos - 50);
                ps.setString(7, "m");
                ps.setInt(8, xpos);
                ps.setInt(9, ypos);
                ps.setInt(10, chr.getMapId());
                ps.setInt(11, mobTime);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                chr.message("Failed to save MOB to the database.");
            }
            chr.getMap().addMonsterSpawn(mob, mobTime, 0);
        } else {
            chr.message("You have entered an invalid Mob-Id");
        }
    }

    @Command
    @Syntax("!pnpc [npcid]")
    @Description("Creates a permanent npc where you are standing.")
    public void pnpc() {
        int npcId = Integer.parseInt(args[1]);
        MapleNPC npc = MapleLifeFactory.getNPC(npcId);
        int xpos = chr.getPosition().x;
        int ypos = chr.getPosition().y;
        int fh = chr.getMap().getFootholds().findBelow(chr.getPosition()).getId();
        if (npc != null && !npc.getName().equals("MISSINGNO")) {
            npc.setPosition(chr.getPosition());
            npc.setCy(ypos);
            npc.setRx0(xpos + 50);
            npc.setRx1(xpos - 50);
            npc.setFh(fh);
            npc.setCustom(true);
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                ps.setInt(1, npcId);
                ps.setInt(2, 0);
                ps.setInt(3, fh);
                ps.setInt(4, ypos);
                ps.setInt(5, xpos + 50);
                ps.setInt(6, xpos - 50);
                ps.setString(7, "n");
                ps.setInt(8, xpos);
                ps.setInt(9, ypos);
                ps.setInt(10, chr.getMapId());
                ps.executeUpdate();
            } catch (SQLException e) {
                chr.message("Failed to save NPC to the database");
            }
            chr.getMap().addMapObject(npc);
            chr.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
        } else {
            chr.message("You have entered an invalid Npc-Id");
        }
    }

    @Command
    @Syntax("!removenpcs|removenearnpcs")
    @Description("Removes (near) npc's. Permanent and temperary.")
    @Alias({"removenearnpcs"})
    public void removenpcs() {
        double range = Double.POSITIVE_INFINITY;
        if (args[0].equalsIgnoreCase("removenearnpcs")) {
            range = 10;
        }
        List<MapleMapObject> npcs = chr.getMap().getMapObjectsInRange(client.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.NPC));
        for (MapleMapObject npcmo : npcs) {
            MapleNPC npc = (MapleNPC) npcmo;
            if (npc.isCustom()) {
                chr.getMap().removeMapObject(npc.getObjectId());
                Connection con = DatabaseConnection.getConnection();
                try {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM spawns WHERE idd=? AND mid=? LIMIT 1");
                    ps.setInt(1, npc.getId());
                    ps.setInt(2, chr.getMapId());
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    chr.message("Unable to delete from database. (" + npc.getId() + ")");
                    chr.message(e.getLocalizedMessage());
                }
            }
        }
    }

    @Command
    @Syntax("!removemobs|removenearmobs")
    @Description("Removes (near) mobs. Permanent and temperary.")
    @Alias({"removenearmobs"})
    public void removemobs() {
        double range = Double.POSITIVE_INFINITY;
        if (args[0].equalsIgnoreCase("removenearmobs")) {
            range = 10;
        }
        List<MapleMapObject> mobs = chr.getMap().getMapObjectsInRange(client.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER));
        for (MapleMapObject mobmo : mobs) {
            MapleMonster mob = (MapleMonster) mobmo;
            if (mob.isCustom()) {
                chr.getMap().removeMapObject(mob.getObjectId());
                Connection con = DatabaseConnection.getConnection();
                try {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM spawns WHERE idd=? AND mid=? LIMIT 1");
                    ps.setInt(1, mob.getId());
                    ps.setInt(2, chr.getMapId());
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    chr.message("Unable to delete from database. (" + mob.getId() + ")");
                    chr.message(e.getLocalizedMessage());
                }
            }
        }
    }

    @Command
    @Syntax("!packet [packet]")
    @Description("Broadcasts a packet in your map.")
    public void packet() {
        chr.getMap().broadcastMessage(MaplePacketCreator.customPacket(StringUtil.joinStringFrom(args, 1, " ")));
    }

    @Command
    @Syntax("!npc [npcid]")
    @Description("Spawns a npc with [npcid] where your standing.")
    public void npc() {
        MapleNPC npc = MapleLifeFactory.getNPC(Integer.parseInt(args[1]));
        if (npc != null) {
            npc.setPosition(chr.getPosition());
            npc.setCy(chr.getPosition().y);
            npc.setRx0(chr.getPosition().x + 50);
            npc.setRx1(chr.getPosition().x - 50);
            npc.setFh(chr.getMap().getFootholds().findBelow(client.getPlayer().getPosition()).getId());
            chr.getMap().addMapObject(npc);
            chr.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
        }
    }

    @Command
    @Syntax("!reactor [reactorid]")
    @Description("Spawns a reactor with [reactorid] where your standing.")
    public void reactor() {
        int reactorId = -1;
        try {
            reactorId = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
        }
        if (MapleReactorFactory.getReactor(reactorId) != null) {
            MapleReactor reactor = new MapleReactor(MapleReactorFactory.getReactor(reactorId), reactorId);
            reactor.setPosition(chr.getPosition());
            chr.getMap().spawnReactor(reactor);
            chr.getMap().broadcastMessage(MaplePacketCreator.spawnReactor(reactor));
        } else {
            chr.message("Unable to find reactor with id '" + args[1] + "' in Command: '" + command + "'");
        }
    }

    @Command
    @Syntax("!playernpc [name] [npcid]")
    @Description("Spawns a player npc for [name] with [npcid].")
    public void playernpc() {
        MapleCharacter victim = world.getPlayerStorage().getCharacterByName(args[1]);
        int script = Integer.parseInt(args[2]);
        if (script != -1) {
            if (script >= 9901000 && script <= 9901319) {
                if ((victim != null && PlayerNPC.createPlayer(victim, script)) || PlayerNPC.createPlayer(args[1], script)) {
                    chr.message("You created a player npc for " + args[1] + ".");
                } else {
                    chr.message(args[1] + " doesn't exist.");
                }
            } else {
                chr.message("Please select a script id between 9901000 and 9901319.");
            }
        } else {
            chr.message("Unrecognized npc id: " + args[2]);
        }
    }

    @Command
    @Syntax("!setgmlevel [name] [level]")
    @Description("Sets the gm level for [name] to [level].")
    public void setgmlevel() {
        MapleCharacter victim = world.getPlayerStorage().getCharacterByName(args[1]);
        if (victim != null) {
            int gm = Integer.parseInt(args[2]);
            if (gm != -1) {
                victim.setGMLevel(gm);
                chr.message("You set " + victim.getName() + "'s gm level to " + gm + ".");
            } else {
                chr.message("Unrecognized gm level: " + gm + ".");
            }
        } else {
            chr.message("Unable to find " + victim.getName() + " online.");
        }
    }

    @Command
    @Syntax("!restartserver (minutes)")
    @Description("Restarts the server in (minutes) minutes.")
    public void restartserver() {
        int min = 1;
        if (args.length == 2) {
            min = Integer.parseInt(args[1]);
        }
        if (min < 0) {
            min = 0;
        }
        if (min > 60) {
            min = 60;
        }
        long delay = 1000 * 60 * min;
        try {
            int[] alertTimes = {1, 2, 3, 4, 5, 10, 15, 20, 30, 45, 60};
            final MapleCharacter s_player = chr;

            int step = 0;
            for (int a = min; a > 0; a--) {
                final int s_min = a;
                boolean canAlert = false;
                for (int b = 0; b < alertTimes.length; b++) {
                    if (alertTimes[b] == a) {
                        canAlert = true;
                        break;
                    }
                }
                if (canAlert) {
                    ScheduledFuture tempTimer = TimerManager.getInstance().schedule(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Server.getInstance().broadcastMessage(s_player.getWorld(), MaplePacketCreator.serverNotice(1, "Server will be shutting down in " + s_min + " minute(s)."));
                                Server.getInstance().broadcastMessage(s_player.getWorld(), MaplePacketCreator.serverNotice(5, "[Notice] Server will be shutting down in " + s_min + " minute(s)."));
                                world.setWorldMessage("Server will be shutting down in " + s_min + " minute(s).");
                            } catch (Exception e) {
                            }
                        }
                    }, delay - (a * 60000));
                    Server.getInstance().setRestartAlert(step, tempTimer);
                    step++;
                }
            }
            Server.getInstance().shutdown(delay);
        } catch (Exception e) {
            chr.message(e.getMessage());
        }
    }

    @Command
    @Description("Cancels a restart if one's in progress.")
    public void cancelrestart() {
        Server.getInstance().cancelShutdown();
        try {
            Server.getInstance().broadcastMessage(chr.getWorld(), MaplePacketCreator.serverNotice(1, "[Notice] Server restart canceled. You may continue with what your doing."));
            Server.getInstance().broadcastMessage(chr.getWorld(), MaplePacketCreator.serverNotice(5, "[Notice] Server restart canceled. You may continue with what your doing."));
            world.setWorldMessage("Server restart canceled. | " + ServerConstants.SERVER_MESSAGE);
        } catch (Exception e) {
        }
    }

    @Command
    @Syntax("!sql [query]")
    @Description("Performs a database query with [query].")
    public void sql() {
        final String query = StringUtil.joinStringFrom(args, 1, " ");
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(query);
            ps.executeUpdate();
            ps.close();
            chr.message("Done " + query);
        } catch (SQLException e) {
            chr.message("Query Failed: " + query);
        }
    }

    @Command
    @Syntax("!sqlwithresult [result] [query]")
    @Description("Performs a database query with [query] and displays the results of [result].")
    public void sqlwithresult() {
        String name = args[1];
        final String query = StringUtil.joinStringFrom(args, 2, " ");
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                chr.message(String.valueOf(rs.getObject(name)));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            chr.message("Query Failed: " + query);
        }
    }
}
