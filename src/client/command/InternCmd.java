package client.command;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.groups.MapleGroup;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.server.Channel;
import net.server.Server;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import scripting.npc.NPCScriptManager;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.StringUtil;

public class InternCmd extends CommandHandler {

    public InternCmd(MapleClient client, char header, String[] args) {
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
        if (MapleGroup.INTERN.atleast(chr)) {
            super.execute();
        }
    }
    
    @Command
    public void intern() {
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
            chr.message("== Intern Commands " + page + "/" + possiblePages + " ==");
            chr.message("For help with commands type '@help commands'.");
            for (CommandInfo info : commands) {
                if (info != null) {
                    chr.message(info.toString());
                }
            }
        } else {
            chr.message("Sorry no intern command help yet.");
        }
    }
    
    @Command
    @Syntax("!dc [name]")
    @Description("Disconnects [name] from the game.")
    public void dc() {
        MapleCharacter victim = world.getPlayerStorage().getCharacterByName(args[1]);
        if(victim != null) {
            victim.getClient().getSession().close();
            victim.getClient().disconnect();
            victim.saveToDB(true);
            world.removePlayer(victim);
        }
    }
    
    @Command
    @Description("Opens the gm shop.")
    public void shop() {
        MapleShopFactory.getInstance().getShop(1337).sendShop(client);
    }
    
    @Command
    @Syntax("!search [item|npc|mob|skill] [search]")
    @Description("Searches for ids that match [search]. (Shows in a NPC)")
    public void search() {
        StringBuilder sb = new StringBuilder();
        if (args.length > 2) {
            String search = StringUtil.joinStringFrom(args, 2, " ");
            long start = System.currentTimeMillis();//for the lulz
            MapleData data = null;
            MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File("wz/String.wz"));
            if (!args[1].equalsIgnoreCase("ITEM")) {
                if (args[1].equalsIgnoreCase("NPC")) {
                    data = dataProvider.getData("Npc.img");
                } else if (args[1].equalsIgnoreCase("MOB") || args[1].equalsIgnoreCase("MONSTER")) {
                    data = dataProvider.getData("Mob.img");
                } else if (args[1].equalsIgnoreCase("SKILL")) {
                    data = dataProvider.getData("Skill.img");
                } else if (args[1].equalsIgnoreCase("MAP")) {
                    sb.append("#bUse the '/m' command to find a map. If it finds a map with the same name, it will warp you to it.");
                } else {
                    sb.append("#bInvalid search.\r\nSyntax: '/search [type] [name]', where [type] is NPC, ITEM, MOB, or SKILL.");
                }
                if (data != null) {
                    String name;
                    for (MapleData searchData : data.getChildren()) {
                        name = MapleDataTool.getString(searchData.getChildByPath("name"), "NO-NAME");
                        if (name.toLowerCase().contains(search.toLowerCase())) {
                            sb.append("#b").append(Integer.parseInt(searchData.getName())).append("#k - #r").append(name).append("\r\n");
                        }
                    }
                }
            } else {
                for (Pair<Integer, String> itemPair : MapleItemInformationProvider.getInstance().getAllItems()) {
                    if (sb.length() < 32654) {//ohlol
                        if (itemPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                            //#v").append(id).append("# #k- 
                            sb.append("#b").append(itemPair.getLeft()).append("#k - #r").append(itemPair.getRight()).append("\r\n");
                        }
                    } else {
                        sb.append("#bCouldn't load all items, there are too many results.\r\n");
                        break;
                    }
                }
            }
            if (sb.length() == 0) {
                sb.append("#bNo ").append(args[1].toLowerCase()).append("s found.\r\n");
            }

            sb.append("\r\n#kLoaded within ").append((double) (System.currentTimeMillis() - start) / 1000).append(" seconds.");//because I can, and it's free

        } else {
            sb.append("#bInvalid search.\r\nSyntax: '/search [type] [name]', where [type] is NPC, ITEM, MOB, or SKILL.");
        }
        client.announce(MaplePacketCreator.getNPCTalk(9010000, (byte) 0, sb.toString(), "00 00", (byte) 0));
    }
    
    @Command
    @Syntax("!find [search]")
    @Description("Searches for ids that match [search].")
    public void find() {
        try {
            BufferedReader dis = new BufferedReader(new InputStreamReader(new URL("http://www.mapletip.com/search_java.php?search_value=" + args[1] + "&check=true").openConnection().getInputStream()));
            String s;
            while ((s = dis.readLine()) != null) {
                chr.message(s);
            }
            dis.close();
        } catch (Exception e) {
        }
    }
    
    @Command
    @Description("Shows all of the users who are online.")
    public void online() {
        for (Channel ch : server.getChannelsFromWorld(chr.getWorld())) {
            String s = "Characters online (Channel " + ch.getId() + " Online: " + ch.getPlayerStorage().getAllCharacters().size() + ") : ";
            if (ch.getPlayerStorage().getAllCharacters().size() < 50) {
                for (MapleCharacter p : ch.getPlayerStorage().getAllCharacters()) {
                    s += MapleCharacter.makeMapleReadable(p.getName()) + ", ";
                }
                chr.message(s.substring(0, s.length() - 2));
            }
        }
    }
    
    @Command
    @Syntax("!showhelp [name|id] (topic)")
    @Description("Opens the help NPC for [name|id] with (topic).")
    public void showhelp() {
//        if(args.length == 2) {
//            MapleCharacter victim = world.getPlayerStorage().getCharacterByName(args[1]);
//            if(victim == null) {
//                try {
//                    victim = world.getPlayerStorage().getCharacterById(Integer.parseInt(args[0]));
//                } catch(NumberFormatException nfe) {
//                }
//            }
//            if(victim != null) {
//                NPCScriptManager.getInstance().start(victim.getClient(), 9901000);//Tagette
//                victim.getClient().announce(MaplePacketCreator.enableActions());
//            } else {
//                chr.message("Could not find player by name or id of '" + args[1] + "'.");
//            }
//        } else {
//            chr.message("Please specify a player name or id.");
//        }
        chr.message("That command is disabled.");
    }
    
    @Command
    @Description("Shows a list of players that need help still.")
    @Alias({"helpwho"})
    public void whohelp() {
        int num = 0;
        chr.message("Needs help:");
        for (MapleCharacter p : world.getPlayerStorage().getAllCharacters()) {
            if (p.needsHelp()) {
                this.chr.message(" - " + p.getName() + " needs help!");
                num++;
            }
        }
        if(num == 0)
            chr.message("No-one needs help.");
    }
    
    @Command
    @Syntax("!help [name|id]")
    @Description("Warps you to [name|id] if they need help.")
    @Alias({"h"})
    public void help() {
        if(args.length == 2) {
            MapleCharacter victim = world.getPlayerStorage().getCharacterByName(args[1]);
            if(victim == null) {
                try {
                    victim = world.getPlayerStorage().getCharacterById(Integer.parseInt(args[1]));
                } catch(NumberFormatException nfe) {
                }
            }
            if(victim != null) {
                if(victim.needsHelp()) {
                    victim.message("A GM has arrived to help you.");
                    chr.message("Thanks for helping " + victim.getName() + ". Your awesome!");
                    victim.setNeedsHelp(false);
                    try {
                        Server.getInstance().broadcastGMMessage(chr.getWorld(), MaplePacketCreator.serverNotice(5, "[GM] : " + chr + " is helping " + victim.getName() + "."));
                    } catch(Exception e) {}
                    warpToCharacter(chr, victim);
                }
            } else {
                chr.message("Could not find player by name or id of '" + args[1] + "'.");
            }
        } else {
            chr.message("Please specify a player name or id.");
        }
    }
    
    @Command
    @Description("Makes you warp to the next person that needs help.")
    @Alias({"mine"})
    public void helpnext() {
        for(Channel cservs : world.getChannels()){
            for(MapleCharacter p : cservs.getPlayerStorage().getAllCharacters()){
                if(p.needsHelp()){
                    p.message("A GM has arrived to help you.");
                    chr.message("Thanks for helping " + p.getName() + ". Your awesome!");
                    p.setNeedsHelp(false);
                    try {
                        Server.getInstance().broadcastGMMessage(chr.getWorld(), MaplePacketCreator.serverNotice(5, "[GM] : " + chr + " is helping " + p.getName() + "."));
                    } catch(Exception e) {}
                    warpToCharacter(chr, p);
                    break;
                }
            }
        }
    }
    
    @Command
    @Syntax("!jail [name|id]")
    @Description("Sends a player with [name|id] to jail.")
    @Alias({"unjail"})
    public void jail() {
        int mapId = 200090300;
        String jail = "jail";
        if (args[0].equals("unjail")) {
            mapId = chr.getReturnMap();
            jail = "unjail";
        }
        MapleCharacter victim = channel.getPlayerStorage().getCharacterByName(args[1]);
        Connection con = DatabaseConnection.getConnection();
        if (victim != null) {
            if(victim != chr){
                if (chr.gmLevel() > victim.gmLevel()) {
                    victim.changeMap(mapId);
                    victim.saveToDB(true);
                    victim.message("You have been "+jail+"ed by a GM with id: "+chr.getId());
                    chr.message("You have "+jail+"ed "+victim.getName()+".");
                } else {
                    if (!jail.equals("jail")) {
                        victim.changeMap(mapId);
                        victim.saveToDB(true);
                        victim.message("You have been "+jail+"ed by a GM with id: "+chr.getId());
                        chr.message("You have "+jail+"ed "+victim.getName()+".");
                    } else
                        chr.message("You cannot jail a GM who has a higher or equal gm Level.");
                }
            } else {
                MapleMap map = channel.getMapFactory().getMap(mapId);
                chr.changeMap(map, map.getPortal("jail00"));
                chr.setReturnMap(mapId);
            }
        } else {
            int gmLevel = 0;
            try {
                PreparedStatement ps = con.prepareStatement("SELECT gm FROM characters WHERE name = ?");
                ps.setString(1, args[1]);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    gmLevel = rs.getInt("gm");
                    if (chr.gmLevel() > gmLevel) {
                        ps.close();
                        ps = con.prepareStatement("UPDATE characters SET map = ? WHERE name = ?");
                        ps.setInt(1, mapId);
                        ps.setString(2, args[1]);
                        ps.executeUpdate();
                        chr.message("You have offline "+jail+"ed "+args[1]+".");
                        chr.sendNote(args[1], "You have been "+jail+"ed.", (byte)0);
                    } else {
                        if (!jail.equals("jail")) {
                            ps.close();
                            ps = con.prepareStatement("UPDATE characters SET map = ? WHERE name = ?");
                            ps.setInt(1, mapId);
                            ps.setString(2, args[1]);
                            ps.executeUpdate();
                            chr.message("You have offline "+jail+"ed "+args[1]+".");
                            chr.sendNote(args[1], "You have been "+jail+"ed.", (byte)0);
                        } else
                            chr.message("You cannot jail a GM who has a higher or equal gm Level.");
                    }
                } else {
                    chr.message(args[1]+" doesn't exist!");
                    chr.message("Syntax: !"+jail+" [user]");
                }
                ps.close();
                rs.close();
            } catch(Exception e) {
                chr.message("Unable to offline "+jail+" "+args[1]+": "+e);
            }
        }
    }
    
    @Command
    @Syntax("![mute|unmute] [name]")
    @Description("Mutes or unmutes [name].")
    @Alias({"unmute"})
    public void mute() {
        boolean canTalk = true;
        String mute = "mute";
        if (args[0].equalsIgnoreCase("unmute")) {
            canTalk = false;
            mute = "unmute";
        }
        MapleCharacter victim = channel.getPlayerStorage().getCharacterByName(args[1]);
        Connection con = DatabaseConnection.getConnection();
        if (victim != null) {
            if (chr.gmLevel() > victim.gmLevel() || victim == chr) {
                victim.setCanTalk(canTalk);
                victim.saveToDB(true);
                victim.message("You have been "+mute+"d by a GM with id: "+chr.getId());
                chr.message("You have "+mute+"d "+victim.getName()+".");
            } else {
                if (!mute.equals("mute")) {
                    victim.setCanTalk(canTalk);
                    victim.saveToDB(true);
                    victim.message("You have been "+mute+"d by a GM with id: "+chr.getId());
                    chr.message("You have "+mute+"d "+victim.getName()+".");
                } else
                    chr.message("You cannot mute a GM who has a higher or equal gm Level.");
            }
        } else {
            int gmLevel = 0;
            try {
                PreparedStatement ps = con.prepareStatement("SELECT gm FROM characters WHERE name = ?");
                ps.setString(1, args[1]);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    gmLevel = rs.getInt("gm");
                    if (chr.gmLevel() > gmLevel) {
                        ps.close();
                        ps = con.prepareStatement("UPDATE characters SET cantalk = ? WHERE name = ?");
                        ps.setInt(1, canTalk ? 1 : 0);
                        ps.setString(2, args[1]);
                        ps.executeUpdate();
                        chr.message("You have offline "+mute+"d "+args[1]+".");
                    } else {
                        if (!mute.equals("mute")) {
                            ps.close();
                            ps = con.prepareStatement("UPDATE characters SET cantalk = ? WHERE name = ?");
                            ps.setInt(1, canTalk ? 1 : 0);
                            ps.setString(2, args[1]);
                            ps.executeUpdate();
                            chr.message("You have offline "+mute+"d "+args[1]+".");
                        } else
                            chr.message("You cannot mute a GM who has a higher or equal gm Level.");
                    }
                } else {
                    chr.message(args[1]+" doesn't exist!");
                    chr.message("Syntax: !"+mute+" [user]");
                }
                ps.close();
                rs.close();
            } catch(Exception e) {
                chr.message("Unable to offline "+mute+" "+args[1]+": "+e);
            }
        }
    }
    
    @Command
    @Description("Kills all of the monsters on the map.")
    @Alias({"butcher", "murder"})
    public void killall() {
        List<MapleMapObject> monsters = chr.getMap().getMapObjectsInRange(chr.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
        for (MapleMapObject monstermo : monsters) {
            chr.getMap().killMonster((MapleMonster) monstermo, chr, false);
        }
        chr.message("Killed " + monsters.size() + " monsters.");
    }
    
    @Command
    @Syntax("!kill ['everyone'|'map'|name]")
    @Description("Kills [everyone] in the world, [map] or someone with [name].")
    public void kill() {
        if (args[1].equals("everyone")) {
            for (MapleCharacter victim : world.getPlayerStorage().getAllCharacters()) {
                if(victim.gmLevel() < chr.gmLevel()){
                    victim.setHpMp(0);
                    victim.message(1, "Uh oh.");
                }
            }
            chr.message("You have killed everyone.");
        } else if (args[1].equals("map")) {
            for (MapleCharacter victim : world.getPlayerStorage().getAllCharacters()) {
                if(victim.gmLevel() < chr.gmLevel()) {
                    if (victim.getMapId() == chr.getMapId()) {
                        victim.setHpMp(0);
                    }
                }
            }
            chr.message("You have killed everyone in your map.");
        } else {
            MapleCharacter victim = world.getPlayerStorage().getCharacterByName(args[1]);
            if (victim != null) {
                if(victim.gmLevel() < chr.gmLevel()) {
                    victim.setHpMp(0);
                    victim.message(chr.getName() + " has killed you.");
                    chr.message("You have killed " + victim.getName() + ".");
                }
            } else {
                chr.message(args[1] + " is offline or doesnt exist.");
                chr.message("Syntax: !kill [everyone/map/user]");
            }
        }
    }
    
    @Command
    @Syntax("!job (name) [id]")
    @Description("Sets (name)'s job to [id].")
    public void job() {
        if(args.length == 1) {
            String text  = "Jobs: \r\n";
            for(MapleJob job : MapleJob.values()){
                text += job.getId() + "     " + job.getName() + "\r\n";
            }
            client.announce(MaplePacketCreator.getNPCTalk(9010000, (byte) 0, text, "00 00", (byte) 0));
        } else if(args.length == 2){
            MapleJob job = MapleJob.getById(Integer.parseInt(args[1]));
            if(job != null) {
                chr.changeJob(job);
                chr.equipChanged();
                chr.message("Your job has been changed to " + job.getName() + ".");
            } else {
                chr.message("A job doesn't exist with id " + args[1] + ".");
            }
        } else if(args.length == 3){
            MapleCharacter victim = client.getChannelServer().getPlayerStorage().getCharacterByName(args[1]);
            if(victim != null) {
                MapleJob job = MapleJob.getById(Integer.parseInt(args[2]));
                if(job != null) {
                    victim.changeJob(job);
                    victim.equipChanged();
                    victim.message("Your job has been changed to " + job.getName() + ".");
                } else {
                    chr.message("A job doesn't exist with id " + args[2] + ".");
                }
            } else {
                chr.message("Unable to find " + args[1] + ".");
            }
        }
    }
    
    @Command
    @Syntax("!warpmap [mapname]")
    @Description("Warps everyone in the map to [mapname].")
    @Alias({"telemap", "tpmap"})
    public void warpmap() {
        boolean isName = true;
        int id = 100000000;
        if(args[1].equals("henesys"))
            id = 100000000;
        else if(args[1].equals("ellinia"))
            id = 101000000;
        else if(args[1].equals("perion"))
            id = 102000000;
        else if(args[1].equals("kerning"))
            id = 103000000;
        else if(args[1].equals("lith"))
            id = 104000000;
        else if(args[1].equals("nautilus"))
            id = 120000000;
        else if(args[1].equals("sleepywood"))
            id = 105040300;
        else if(args[1].equals("orbis"))
            id = 200000000;
        else if(args[1].equals("elnath"))
            id = 211000000;
        else if(args[1].equals("ludi"))
            id = 220000000;
        else if(args[1].equals("omega"))
            id = 221000000;
        else if(args[1].equals("kft"))
            id = 222000000;
        else if(args[1].equals("haunted"))
            id = 229000000;
        else if(args[1].equals("aquarium"))
            id = 230000000;
        else if(args[1].equals("leafre"))
            id = 240000000;
        else if(args[1].equals("mulung"))
            id = 250000000;
        else if(args[1].equals("herbtown"))
            id = 251000000;
        else if(args[1].equals("ariant"))
            id = 260000000;
        else if(args[1].equals("happy"))
            id = 300000000;
        else if(args[1].equals("singapore"))
            id = 540000000;
        else if(args[1].equals("nlc"))
            id = 600000000;
        else if(args[1].equals("amoria"))
            id = 670000000;
        else if(args[1].equals("zipangu"))
            id = 800000000;
        else if(args[1].equals("fm"))
            id = 910000000;
        else if(args[1].equals("fm1"))
            id = 910000001;
        else if(args[1].equals("chimney"))
            id = 682000200;
        else if(args[1].equals("b1a"))
            id = 103000900;
        else if(args[1].equals("b1b"))
            id = 103000901;
        else if(args[1].equals("b1c"))
            id = 103000902;
        else if(args[1].equals("b2a"))
            id = 103000903;
        else if(args[1].equals("b2b"))
            id = 103000904;
        else if(args[1].equals("b2c"))
            id = 103000905;
        else if(args[1].equals("b3a"))
            id = 103000906;
        else if(args[1].equals("b3b"))
            id = 103000907;
        else if(args[1].equals("b3c"))
            id = 103000908;
        else if(args[1].equals("b3d"))
            id = 103000909;
        else if(args[1].equals("patience1") || args[1].equals("pat1"))
            id = 105040310;
        else if(args[1].equals("patience2") || args[1].equals("pat2"))
            id = 105040311;
        else if(args[1].equals("patience3") || args[1].equals("pat3"))
            id = 105040312;
        else if(args[1].equals("patience4") || args[1].equals("pat4"))
            id = 105040313;
        else if(args[1].equals("patience5") || args[1].equals("pat5"))
            id = 105040314;
        else if(args[1].equals("patience6") || args[1].equals("pat6"))
            id = 105040315;
        else if(args[1].equals("patience7") || args[1].equals("pat7"))
            id = 105040316;
        else if(args[1].equals("ox"))
            id = 109020001;
        else if(args[1].equals("ola1"))
            id = 109030001;
        else if(args[1].equals("ola2"))
            id = 109030002;
        else if(args[1].equals("ola3"))
            id = 109030003;
        else if(args[1].equals("physical1") || args[1].equals("phy1"))
            id = 109040000;
        else if(args[1].equals("physical2") || args[1].equals("phy2"))
            id = 109040001;
        else if(args[1].equals("physical3") || args[1].equals("phy3"))
            id = 109040002;
        else if(args[1].equals("physical4") || args[1].equals("phy4"))
            id = 109040003;
        else if(args[1].equals("physical5") || args[1].equals("phy5"))
            id = 109040004;
        else if(args[1].equals("reward"))
            id = 109050000;
        else if(args[1].equals("leave"))
            id = 109050001;
        else if(args[1].equals("gmroom"))
            id = 180000000;
        else
            isName = false;
        if(isName){
            if(args.length == 2){
                for(MapleCharacter victims : chr.getMap().getCharacters()){
                    victims.changeMap(id);
                    victims.message("You have been warped to "+channel.getMapFactory().getMap(id).getMapName()+".");
                }
                chr.message("You have warped the map to "+channel.getMapFactory().getMap(id).getMapName() + ". (" + id + ")");
            } else if(args.length == 3){
                for(MapleCharacter victims : chr.getMap().getCharacters()){
                    victims.changeMap(id, Integer.parseInt(args[2]));
                    victims.message("You have been warped to "+channel.getMapFactory().getMap(id).getMapName()+".");
                }
                chr.message("You have warped the map to "+channel.getMapFactory().getMap(id).getMapName() + ". (" + id + ")" + " Portal: " + args[2]);
            }
        } else {
            chr.message("You can only use map names with !map.");
            chr.message("For a list of map names type !map.");
        }
    }
}
