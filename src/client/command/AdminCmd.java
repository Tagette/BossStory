package client.command;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.server.Channel;
import net.server.Server;
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

public class AdminCmd {
    
    private static String[] arguments;

    public static boolean executeCommand(MapleClient c, String[] args) {
        arguments = args;
        boolean handled = true;
        MapleCharacter chr = c.getPlayer();
        Channel channel = c.getChannelServer();
        
        if (isCmd("horntail")) {
            chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8810026), chr.getPosition());
        } else if (isCmd("packet")) {
            chr.getMap().broadcastMessage(MaplePacketCreator.customPacket(CommandProcessor.joinStringFrom(args, 1)));
        } else if (isCmd("warpworld")) {
            Server server = Server.getInstance();
            byte world = Byte.parseByte(args[1]);
            if (world <= (server.getWorlds().size() - 1)) {
                try {
                    String[] socket = server.getIP(world, c.getChannel()).split(":");
                    c.getWorldServer().removePlayer(chr);
                    chr.getMap().removePlayer(chr);//LOL FORGOT THIS ><
                    server.getPlayerStorage().addPlayer(chr);
                    c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
                    chr.setWorld(world);
                    chr.saveToDB(true);//To set the new world :O (true because else 2 chr instances are created, one in both worlds)
                    c.announce(MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
                } catch (Exception ex) {
                    chr.message("Error when trying to change worlds, are you sure the world you are trying to warp to has the same amount of channels?");
                }

            } else {
                chr.message("Invalid world; highest number available: " + (server.getWorlds().size() - 1));
            }
        } else if (isCmd("npc")) {
            MapleNPC npc = MapleLifeFactory.getNPC(Integer.parseInt(args[1]));
            if (npc != null) {
                npc.setPosition(chr.getPosition());
                npc.setCy(chr.getPosition().y);
                npc.setRx0(chr.getPosition().x + 50);
                npc.setRx1(chr.getPosition().x - 50);
                npc.setFh(chr.getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
                chr.getMap().addMapObject(npc);
                chr.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
            }
        } else if (isCmd("jobperson")) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(args[1]);
            victim.changeJob(MapleJob.getById(Integer.parseInt(args[2])));
            chr.equipChanged();
        } else if (isCmd("pinkbean")) {
            chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8820009), chr.getPosition());
        } else if (isCmd("playernpc")) {
            chr.playerNPC(c.getChannelServer().getPlayerStorage().getCharacterByName(args[1]), Integer.parseInt(args[2]));
        } else if (isCmd("setgmlevel")) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(args[1]);
            victim.setGM(Integer.parseInt(args[2]));
            chr.message("Done.");
            victim.getClient().disconnect();
        } else if (isCmd("shutdown") || isCmd("shutdownnow")) {
            int time = 60000;
            if (isCmd("shutdownnow")) {
                time = 1;
            } else if (args.length > 1) {
                time *= Integer.parseInt(args[1]);
            }
            for (Channel cs : Server.getInstance().getAllChannels()) {
                cs.shutdown(time);
            }
        } else if (isCmd("sql")) {
            final String query = CommandProcessor.joinStringFrom(args, 1);
            try {
                PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(query);
                ps.executeUpdate();
                ps.close();
                chr.message("Done " + query);
            } catch (SQLException e) {
                chr.message("Query Failed: " + query);
            }
        } else if (isCmd("sqlwithresult")) {
            String name = args[1];
            final String query = CommandProcessor.joinStringFrom(args, 2);
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
        } else if (isCmd("zakum")) {
            chr.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), chr.getPosition());
            for (int x = 8800003; x < 8800011; x++) {
                chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(x), chr.getPosition());
            }
        } else {
            handled = false;
        }
        return handled;
    }
    
    private static boolean isCmd(String label){
        return arguments[0].equalsIgnoreCase(label);
    }
}
