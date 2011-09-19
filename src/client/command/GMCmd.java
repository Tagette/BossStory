package client.command;

import client.IItem;
import client.ISkill;
import client.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleJob;
import client.MaplePet;
import client.MapleStat;
import client.SkillFactory;
import constants.ItemConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import net.server.Channel;
import net.server.Server;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import server.events.gm.MapleEvent;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;

public class GMCmd {
    
    private static String[] arguments;

    public static boolean executeCommand(MapleClient c, String[] args) {
        arguments = args;
        boolean handled = true;
        MapleCharacter player = c.getPlayer();
        Channel cserv = c.getChannelServer();
        Server srv = Server.getInstance();
        if (isCmd("ap")) {
            player.setRemainingAp(Integer.parseInt(args[1]));
        } else if (isCmd("buffme")) {
            final int[] array = {9001000, 9101002, 9101003, 9101008, 2001002, 1101007, 1005, 2301003, 5121009, 1111002, 4111001, 4111002, 4211003, 4211005, 1321000, 2321004, 3121002};
            for (int i : array) {
                SkillFactory.getSkill(i).getEffect(SkillFactory.getSkill(i).getMaxLevel()).applyTo(player);
            }
        } else if (isCmd("spawn")) {
            if (args.length > 2) {
                for (int i = 0; i < Integer.parseInt(args[2]); i++) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(Integer.parseInt(args[1])), player.getPosition());
                }
            } else {
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(Integer.parseInt(args[1])), player.getPosition());
            }
        } else if (isCmd("cleardrops")) {
            player.getMap().clearDrops(player);
        } else if (isCmd("dc")) {
            MapleCharacter chr = c.getWorldServer().getPlayerStorage().getCharacterByName(args[1]);
            if (player.gmLevel() > chr.gmLevel()) {
                chr.getClient().disconnect();
            }
        } else if (isCmd("exprate")) {
            c.getWorldServer().setExpRate((byte) (Byte.parseByte(args[1]) % 128));
            for (Channel cs : Server.getInstance().getChannelsFromWorld(player.getWorld())) {
                for (MapleCharacter mc : cs.getPlayerStorage().getAllCharacters()) {
                    mc.setRates();
                }
            }
        } else if (isCmd("fame")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(args[1]);
            victim.setFame(Integer.parseInt(args[2]));
            victim.updateSingleStat(MapleStat.FAME, victim.getFame());
        } else if (isCmd("giftnx")) {
            cserv.getPlayerStorage().getCharacterByName(args[1]).getCashShop().gainCash(1, Integer.parseInt(args[2]));
            player.message("Done");
        } else if (isCmd("gmshop")) {
            MapleShopFactory.getInstance().getShop(1337).sendShop(c);
        } else if (isCmd("heal")) {
            player.setHpMp(30000);
        } else if (isCmd("id")) {
            try {
                BufferedReader dis = new BufferedReader(new InputStreamReader(new URL("http://www.mapletip.com/search_java.php?search_value=" + args[1] + "&check=true").openConnection().getInputStream()));
                String s;
                while ((s = dis.readLine()) != null) {
                    player.message(s);
                }
                dis.close();
            } catch (Exception e) {
            }
        } else if (isCmd("item") || isCmd("drop")) {
            int itemId = Integer.parseInt(args[1]);
            short quantity = 1;
            try {
                quantity = Short.parseShort(args[2]);
            } catch (Exception e) {
            }
            if (isCmd("item")) {
                int petid = -1;
                if (ItemConstants.isPet(itemId)) {
                    petid = MaplePet.createPet(itemId);
                }
                MapleInventoryManipulator.addById(c, itemId, quantity, player.getName(), petid, -1);
            } else {
                IItem toDrop;
                if (MapleItemInformationProvider.getInstance().getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                    toDrop = MapleItemInformationProvider.getInstance().getEquipById(itemId);
                } else {
                    toDrop = new Item(itemId, (byte) 0, quantity);
                }
                c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);
            }
        } else if (isCmd("job")) {
            player.changeJob(MapleJob.getById(Integer.parseInt(args[1])));
            player.equipChanged();
        } else if (isCmd("kill")) {
            cserv.getPlayerStorage().getCharacterByName(args[1]).setHpMp(0);
        } else if (isCmd("killall")) {
            List<MapleMapObject> monsters = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
            for (MapleMapObject monstermo : monsters) {
                MapleMonster monster = (MapleMonster) monstermo;
                player.getMap().killMonster(monster, player, true);
                monster.giveExpToCharacter(player, monster.getExp() * c.getPlayer().getExpRate(), true, 1);
            }
            player.message("Killed " + monsters.size() + " monsters.");
        } else if (isCmd("unbug")) {
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.enableActions());
        } else if (isCmd("level")) {
            player.setLevel(Integer.parseInt(args[1]));
            player.gainExp(-player.getExp(), false, false);
            player.updateSingleStat(MapleStat.LEVEL, player.getLevel());
        } else if (isCmd("levelperson")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(args[1]);
            victim.setLevel(Integer.parseInt(args[2]));
            victim.gainExp(-victim.getExp(), false, false);
            victim.updateSingleStat(MapleStat.LEVEL, victim.getLevel());
        } else if (isCmd("levelpro")) {
            while (player.getLevel() < Math.min(255, Integer.parseInt(args[1]))) {
                player.levelUp(false);
            }
        } else if (isCmd("levelup")) {
            player.levelUp(false);
        } else if (isCmd("maxstat")) {
            final String[] s = {"setall", String.valueOf(Short.MAX_VALUE)};
            executeCommand(c, s);
            player.setLevel(255);
            player.setFame(13337);
            player.setMaxHp(30000);
            player.setMaxMp(30000);
            player.updateSingleStat(MapleStat.LEVEL, 255);
            player.updateSingleStat(MapleStat.FAME, 13337);
            player.updateSingleStat(MapleStat.MAXHP, 30000);
            player.updateSingleStat(MapleStat.MAXMP, 30000);
        } else if (isCmd("maxskills")) {
            for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren()) {
                try {
                    ISkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                    player.changeSkillLevel(skill, (byte) skill.getMaxLevel(), skill.getMaxLevel(), -1);
                } catch (NumberFormatException nfe) {
                    break;
                } catch (NullPointerException npe) {
                    continue;
                }
            }
        } else if (isCmd("mesoperson")) {
            cserv.getPlayerStorage().getCharacterByName(args[1]).gainMeso(Integer.parseInt(args[2]), true);
        } else if (isCmd("mesos")) {
            player.gainMeso(Integer.parseInt(args[1]), true);
        } else if (isCmd("notice")) {
            Server.getInstance().broadcastMessage(player.getWorld(), MaplePacketCreator.serverNotice(6, "[Notice] " + CommandProcessor.joinStringFrom(args, 1)));
        } else if (isCmd("openportal")) {
            player.getMap().getPortal(args[1]).setPortalState(true);
        } else if (isCmd("closeportal")) {
            player.getMap().getPortal(args[1]).setPortalState(false);
        } else if (isCmd("startevent")) {
            for (MapleCharacter chr : player.getMap().getCharacters()) {
                player.getMap().startEvent(chr);
            }
            c.getChannelServer().setEvent(null);
        } else if (isCmd("scheduleevent")) {
            if (c.getPlayer().getMap().hasEventNPC()) {
                if (args[1].equals("treasure")) {
                    c.getChannelServer().setEvent(new MapleEvent(109010000, 50));
                } else if (args[1].equals("ox")) {
                    c.getChannelServer().setEvent(new MapleEvent(109020001, 50));
                    srv.broadcastMessage(player.getWorld(), MaplePacketCreator.serverNotice(0, "Hello Scania let's play an event in " + player.getMap().getMapName() + " CH " + c.getChannel() + "! " + player.getMap().getEventNPC()));
                } else if (args[1].equals("ola")) {
                    c.getChannelServer().setEvent(new MapleEvent(109030101, 50)); // Wrong map but still Ola Ola
                    srv.broadcastMessage(player.getWorld(), MaplePacketCreator.serverNotice(0, "Hello Scania let's play an event in " + player.getMap().getMapName() + " CH " + c.getChannel() + "! " + player.getMap().getEventNPC()));
                } else if (args[1].equals("fitness")) {
                    c.getChannelServer().setEvent(new MapleEvent(109040000, 50));
                    srv.broadcastMessage(player.getWorld(), MaplePacketCreator.serverNotice(0, "Hello Scania let's play an event in " + player.getMap().getMapName() + " CH " + c.getChannel() + "! " + player.getMap().getEventNPC()));
                } else if (args[1].equals("snowball")) {
                    c.getChannelServer().setEvent(new MapleEvent(109060001, 50));
                    srv.broadcastMessage(player.getWorld(), MaplePacketCreator.serverNotice(0, "Hello Scania let's play an event in " + player.getMap().getMapName() + " CH " + c.getChannel() + "! " + player.getMap().getEventNPC()));
                } else if (args[1].equals("coconut")) {
                    c.getChannelServer().setEvent(new MapleEvent(109080000, 50));
                    srv.broadcastMessage(player.getWorld(), MaplePacketCreator.serverNotice(0, "Hello Scania let's play an event in " + player.getMap().getMapName() + " CH " + c.getChannel() + "! " + player.getMap().getEventNPC()));
                } else {
                    player.message("Wrong Syntax: /scheduleevent treasure, ox, ola, fitness, snowball or coconut");
                }
            } else {
                player.message("You can only use this command in the following maps: 60000, 104000000, 200000000, 220000000");
            }

        } else if (isCmd("online")) {
            for (Channel ch : srv.getChannelsFromWorld(player.getWorld())) {
                String s = "Characters online (Channel " + ch.getId() + " Online: " + ch.getPlayerStorage().getAllCharacters().size() + ") : ";
                if (ch.getPlayerStorage().getAllCharacters().size() < 50) {
                    for (MapleCharacter chr : ch.getPlayerStorage().getAllCharacters()) {
                        s += MapleCharacter.makeMapleReadable(chr.getName()) + ", ";
                    }
                    player.message(s.substring(0, s.length() - 2));
                }
            }
        } else if (isCmd("pap")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8500001), player.getPosition());
        } else if (isCmd("pianus")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8510000), player.getPosition());
        } else if (args[0].equalsIgnoreCase("search")) {
            StringBuilder sb = new StringBuilder();
            if (args.length > 2) {
                String search = CommandProcessor.joinStringFrom(args, 2);
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
            c.announce(MaplePacketCreator.getNPCTalk(9010000, (byte) 0, sb.toString(), "00 00", (byte) 0));
        } else if (isCmd("servermessage")) {
            for (Channel ch : srv.getChannelsFromWorld(player.getWorld())) {
                ch.setServerMessage(CommandProcessor.joinStringFrom(args, 1));
            }
        } else if (isCmd("warpsnowball")) {
            for (MapleCharacter chr : player.getMap().getCharacters()) {
                chr.changeMap(109060000, chr.getTeam());
            }
        } else if (isCmd("setall")) {
            final int x = Short.parseShort(args[1]);
            player.setStr(x);
            player.setDex(x);
            player.setInt(x);
            player.setLuk(x);
            player.updateSingleStat(MapleStat.STR, x);
            player.updateSingleStat(MapleStat.DEX, x);
            player.updateSingleStat(MapleStat.INT, x);
            player.updateSingleStat(MapleStat.LUK, x);
        } else if (isCmd("sp")) {
            player.setRemainingSp(Integer.parseInt(args[1]));
            player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
        } else if (isCmd("unban")) {
            try {
                PreparedStatement p = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = -1 WHERE id = " + MapleCharacter.getIdByName(args[1]));
                p.executeUpdate();
                p.close();
            } catch (Exception e) {
                player.message("Failed to unban " + args[1]);
                return true;
            }
            player.message("Unbanned " + args[1]);
        } else {
            handled = false;
        }
        return handled;
    }
    
    private static boolean isCmd(String label){
        return arguments[0].equalsIgnoreCase(label);
    }
}
