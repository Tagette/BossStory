package client.command;

import client.*;
import client.groups.MapleGroup;
import client.powerskills.PowerSkill;
import client.powerskills.PowerSkillType;
import constants.ExpTable;
import constants.ItemConstants;
import constants.ServerConstants;
import java.io.File;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.server.Channel;
import net.server.Server;
import net.server.handlers.channel.ItemPickupHandler;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import server.events.gm.MapleEvent;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.StringUtil;

public class GMCmd extends CommandHandler {

    public GMCmd(MapleClient client, char header, String[] args) {
        super(client, header, args);
    }

    /**
     * Allows for any code to be run before the command is executed. Note: super.execute() is required for the command to execute.
     * 
     * @throws Exception Throws any exception that may occur while the command is executed.
     */
    @Override
    public void execute() throws Exception {
        if (MapleGroup.GM.atleast(chr)) {
            super.execute();
        }
    }
    
    @Command
    public void gm() {
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
            chr.message("== GM Commands " + page + "/" + possiblePages + " ==");
            chr.message("For help with commands type '@help commands'.");
            for (CommandInfo info : commands) {
                if (info != null) {
                    chr.message(info.toString());
                }
            }
        } else {
            chr.message("Sorry no GM command help yet.");
        }
    }

    @Command
    @Syntax("!ap [amount]")
    @Description("Sets your ap to [amount].")
    public void ap() {
        chr.setRemainingAp(Integer.parseInt(args[1]));
    }

    @Command
    @Description("Gives you a lot of buffs.")
    public void buffme() {
        final int[] array = {9001000, 9101002, 9101003, 9101008, 2001002, 1101007, 1005, 2301003, 5121009, 1111002, 4111001, 4111002, 4211003, 4211005, 1321000, 2321004, 3121002};
        for (int i : array) {
            SkillFactory.getSkill(i).getEffect(SkillFactory.getSkill(i).getMaxLevel()).applyTo(chr, false);
        }
    }

    @Command
    @Description("Tells you your current position in your map.")
    public void pos() {
        chr.message("Map: " + chr.getMap().getId() + ", Position: (" + chr.getPosition().x + ", " + chr.getPosition().y + ") FH: " + chr.getMap().getFootholds().findBelow(chr.getPosition()).getId());
    }

    @Command
    @Syntax("!spawn [mobid|mobname] [amount]")
    @Description("Spawns [amount] [mobid|mobname] where you standing. '!spawn' for list of mobs.")
    @Alias({"summon"})
    public void spawn() {
        int[] id = {};
        int quantity = 1;
        boolean isName = false;
        if (args.length == 1) {
            chr.message("Syntax: !spawn [mobId/mobName] [amount] - only include [amount] if you use an id.");
            chr.message(5, "---------- Mob Names ----------");
            chr.message(5, "balrog - spawns a balrog");
            chr.message(5, "mushmom - spawns a mushmom");
            chr.message(5, "nxslimes - spawns 10 nx slimes");
            chr.message(5, "pap - spawns a papulatus");
            chr.message(5, "pianus - spawns a pianus");
        } else if (args.length == 2) {
            if (args[1].equals("balrog")) {
                int[] newId = {8130100, 8150000, 9400536};
                id = newId;
                isName = true;
            } else if (args[1].equals("mushmom")) {
                int[] newId = {6130101, 6300005, 9400205};
                id = newId;
                isName = true;
            } else if (args[1].equals("nxslimes")) {
                int[] newId = {9400202};
                id = newId;
                quantity = 10;
                isName = true;
            } else if (args[1].equals("pap")) {
                int[] newId = {8500001};
                id = newId;
                isName = true;
            } else if (args[1].equals("pianus")) {
                int[] newId = {8510000};
                id = newId;
                isName = true;
            }
            if (isName) {
                for (int a = 0; a < quantity; a++) {
                    for (int b : id) {
                        chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(b), chr.getPosition());
                    }
                }
            } else {
                MapleMonster mob = MapleLifeFactory.getMonster(Integer.parseInt(args[1]));
                if (mob != null) {
                    for (int i = 0; i < quantity; i++) {
                        chr.getMap().spawnMonsterOnGroudBelow(mob, chr.getPosition());
                    }
                } else {
                    chr.message("In command: " + command);
                    chr.message("Unable to find monster id: " + args[1]);
                    chr.message("Syntax: !spawn [mobId/mobName] [amount] - only include [amount] if you use an id.");
                }
            }
        } else if (args.length == 3) {
            if (args[1].equals("balrog")) {
                int[] newId = {8130100, 8150000, 9400536};
                id = newId;
                isName = true;
            } else if (args[1].equals("mushmom")) {
                int[] newId = {6130101, 6300005, 9400205};
                id = newId;
                isName = true;
            } else if (args[1].equals("nxslimes")) {
                int[] newId = {9400202};
                id = newId;
                quantity = 3;
                isName = true;
            } else if (args[1].equals("pap")) {
                int[] newId = {8500001};
                id = newId;
                isName = true;
            } else if (args[1].equals("pianus")) {
                int[] newId = {8510000};
                id = newId;
                isName = true;
            }
            if (isName) {
                for (int a = 0; a < quantity; a++) {
                    for (int b : id) {
                        chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(b), chr.getPosition());
                    }
                }
            } else {
                try {
                    quantity = Integer.parseInt(args[2]);
                    if (quantity > 100) {
                        quantity = 100;
                    }
                } catch (Exception e) {
                    quantity = 1;
                }
                MapleMonster mob = MapleLifeFactory.getMonster(Integer.parseInt(args[1]));
                if (mob != null) {
                    for (int i = 0; i < quantity; i++) {
                        mob = MapleLifeFactory.getMonster(Integer.parseInt(args[1]));
                        chr.getMap().spawnMonsterOnGroudBelow(mob, chr.getPosition());
                    }
                } else {
                    chr.message("Unknown monster id: " + args[1]);
                }
            }
        } else {
            chr.message("In command: " + command);
            chr.message("Syntax: !spawn [mobId/mobName] [amount] - creates [amount] monsters with [mobId] below your feet.");
            chr.message("Type !spawn for mob names.");
        }
    }

    @Command
    @Syntax("!spawnon [name] [mobid|mobname] [amount]")
    @Description("Spawns [amount] mobs where [name] is standing. '!spawn' for a list of mobs.")
    @Alias({"summonon"})
    public void spawnon() {
        int[] id = {};
        int quantity = 1;
        boolean isName = false;
        MapleCharacter victim = world.getPlayerStorage().getCharacterByName(args[1]);
        if (victim != null) {
            if (args.length == 3) {
                if (args[2].equals("balrog")) {
                    int[] newId = {8130100, 8150000, 9400536};
                    id = newId;
                    isName = true;
                } else if (args[2].equals("mushmom")) {
                    int[] newId = {6130101, 6300005, 9400205};
                    id = newId;
                    isName = true;
                } else if (args[2].equals("nxslimes")) {
                    int[] newId = {9400202};
                    id = newId;
                    quantity = 10;
                    isName = true;
                } else if (args[2].equals("pap")) {
                    int[] newId = {8500001};
                    id = newId;
                    isName = true;
                } else if (args[2].equals("pianus")) {
                    int[] newId = {8510000};
                    id = newId;
                    isName = true;
                }
                if (isName) {
                    for (int a = 0; a < quantity; a++) {
                        for (int b : id) {
                            victim.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(b), victim.getPosition());
                        }
                    }
                } else {
                    MapleMonster mob = MapleLifeFactory.getMonster(Integer.parseInt(args[2]));
                    if (mob != null) {
                        for (int i = 0; i < quantity; i++) {
                            victim.getMap().spawnMonsterOnGroudBelow(mob, victim.getPosition());
                        }
                    } else {
                        chr.message("Unknown monster id: " + args[2]);
                    }
                }
            } else if (args.length == 4) {
                if (args[2].equals("balrog")) {
                    int[] newId = {8130100, 8150000, 9400536};
                    id = newId;
                    isName = true;
                } else if (args[2].equals("mushmom")) {
                    int[] newId = {6130101, 6300005, 9400205};
                    id = newId;
                    isName = true;
                } else if (args[2].equals("nxslimes")) {
                    int[] newId = {9400202};
                    id = newId;
                    quantity = 10;
                    isName = true;
                } else if (args[2].equals("pap")) {
                    int[] newId = {8500001};
                    id = newId;
                    isName = true;
                } else if (args[2].equals("pianus")) {
                    int[] newId = {8510000};
                    id = newId;
                    isName = true;
                }
                if (isName) {
                    for (int a = 0; a < quantity; a++) {
                        for (int b : id) {
                            victim.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(b), victim.getPosition());
                        }
                    }
                } else {
                    try {
                        quantity = Integer.parseInt(args[3]);
                        if (quantity > 100) {
                            quantity = 100;
                        }
                    } catch (Exception e) {
                        quantity = 1;
                    }
                    MapleMonster mob = MapleLifeFactory.getMonster(Integer.parseInt(args[2]));
                    if (mob != null) {
                        for (int a = 0; a < quantity; a++) {
                            mob = MapleLifeFactory.getMonster(Integer.parseInt(args[2]));
                            victim.getMap().spawnMonsterOnGroudBelow(mob, victim.getPosition());
                        }
                    } else {
                        chr.message("Unknown monster id: " + args[2]);
                    }

                }
            } else {
                chr.message("In command: " + command);
                chr.message("Syntax: !spawnon [user] [mobId/mobName] [amount] - creates [amount] monsters with [mobId] below [user]'s feet.");
                chr.message("Type !spawn for mob names.");
            }
        } else {
            chr.message(args[1] + " is offline or doesn't exist.");
        }
    }

    @Command
    @Description("Clears all items from the ground in a map.")
    public void cleardrops() {
        chr.getMap().clearDrops();
    }

    @Command
    @Syntax("!dc [name]")
    @Description("Disconnects [name] from the game.")
    @Alias({"kick"})
    public void dc() {
        MapleCharacter p = world.getPlayerStorage().getCharacterByName(args[1]);
        if (this.chr.gmLevel() > p.gmLevel()) {
            p.getClient().disconnect();
        }
    }

    @Command
    @Syntax("!exprate [amount]")
    @Description("Sets the exp rate of the server to [amount].")
    public void exprate() {
                short amount = Short.parseShort(args[1]);
                if (amount > 0) {
                    world.setExpRate(amount);
                    for (Channel cs : Server.getInstance().getChannelsFromWorld(chr.getWorld())) {
                        for (MapleCharacter mc : cs.getPlayerStorage().getAllCharacters()) {
                            mc.setCards();
                        }
                    }
                }
    }

    @Command
    @Syntax("!mesorate [amount]")
    @Description("Sets the meso rate of the server to [amount].")
    public void mesorate() {
                short amount = Short.parseShort(args[1]);
                if (amount > 0) {
                    world.setMesoRate(amount);
                    for (Channel cs : Server.getInstance().getChannelsFromWorld(chr.getWorld())) {
                        for (MapleCharacter mc : cs.getPlayerStorage().getAllCharacters()) {
                            mc.setCards();
                        }
                    }
                }
    }

    @Command
    @Syntax("!droprate [amount]")
    @Description("Sets the drop rate of the server to [amount].")
    public void droprate() {
                short amount = Short.parseShort(args[1]);
                if (amount > 0) {
                    world.setDropRate(amount);
                    for (Channel cs : Server.getInstance().getChannelsFromWorld(chr.getWorld())) {
                        for (MapleCharacter mc : cs.getPlayerStorage().getAllCharacters()) {
                            mc.setCards();
                        }
                    }
                }
    }

    @Command
    @Syntax("!fame [name] [amount]")
    @Description("Sets [name]'s fame to [amount].")
    public void fame() {
                MapleCharacter victim = channel.getPlayerStorage().getCharacterByName(args[1]);
                victim.setFame(Integer.parseInt(args[2]));
                victim.updateSingleStat(MapleStat.FAME, victim.getFame());
    }

    @Command
    @Syntax("!giftnx [name] [amount]")
    @Description("Gives [amount] NX cash to [name].")
    @Alias({"nx"})
    public void giftnx() {
                channel.getPlayerStorage().getCharacterByName(args[1]).getCashShop().gainCash(1, Integer.parseInt(args[2]));
                chr.message("Done");
    }

    @Command
    @Description("Opens the GM shop.")
    public void gmshop() {
                MapleShopFactory.getInstance().getShop(1337).sendShop(client);
    }

    @Command
    @Syntax("![item|drop] [itemid] [amount]")
    @Description("Gives you [amount] items with id [itemid]. '!drop' drops on ground instead.")
    @Alias({"drop"})
    public void item() {
        int itemId = Integer.parseInt(args[1]);
        short quantity = 1;
        try {
            quantity = Short.parseShort(args[2]);
        } catch (Exception e) {
        }
        if (args[0].equalsIgnoreCase("item")) {
            int petid = -1;
            if (ItemConstants.isPet(itemId)) {
                petid = MaplePet.createPet(itemId);
            }
            MapleInventoryManipulator.addById(client, itemId, quantity, chr.getName(), petid, Long.MAX_VALUE);
        } else {
            IItem toDrop;
            if (MapleItemInformationProvider.getInstance().getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                toDrop = MapleItemInformationProvider.getInstance().getEquipById(itemId);
            } else {
                toDrop = new Item(itemId, (byte) 0, quantity);
            }
            chr.getMap().spawnItemDrop(chr, chr, toDrop, chr.getPosition(), true, true);
        }
    }

    @Command
    @Description("Resets the map.")
    @Alias({"resetmap"})
    public void unbugmap() {
        client.getPlayer().getMap().broadcastMessage(MaplePacketCreator.enableActions());
    }

    @Command
    @Syntax("!level (name) [level]")
    @Description("Sets (name)'s level to [level].")
    @Alias({"lvl", "setlevel"})
    public void level() {
        MapleCharacter victim = chr;
        int level = Integer.parseInt(args[1]);
        if(args.length == 3) {
            victim = world.getPlayerStorage().getCharacterByName(args[1]);
            level = Integer.parseInt(args[2]);
        }
        if(victim != null) {
            level = Math.min(ServerConstants.MAX_LEVEL, level);
            if(level > 0 && level <= ServerConstants.MAX_LEVEL) {
                victim.setLevel(level);
                victim.setExp(0);
                chr.updateSingleStat(MapleStat.LEVEL, chr.getLevel());
                chr.updateSingleStat(MapleStat.EXP, 0);
            } else {
                chr.message((args.length == 3 ? args[2] : args[1]) + " is not a correct level.");
            }
        } else {
            chr.message(args[1] + " is offline or doesn't exist.");
        }
    }

    @Command
    @Syntax("!levelpro (name) [level]")
    @Description("Levels (name) until level [level].")
    @Alias({"levelupto", "lvlpro", "lvlupto"})
    public void levelpro() {
        MapleCharacter victim = chr;
        int level = Integer.parseInt(args[1]);
        if(args.length == 3) {
            victim = world.getPlayerStorage().getCharacterByName(args[1]);
            level = Integer.parseInt(args[2]);
        }
        if(victim != null) {
            int count = 0;
            while (victim.getLevel() < Math.min(ServerConstants.MAX_LEVEL, level)) {
                victim.levelUp(victim.getLevel() + 1 == level);
                count++;
            }
            victim.message("You have leveled " + count + " times!");
        } else {
            chr.message(args[1] + " is offline or doesn't exist.");
        }
    }

    @Command
    @Syntax("!levelup (name)")
    @Description("Levels up (name) once.")
    @Alias({"lvlup"})
    public void levelup() {
                MapleCharacter victim = chr;
        if (args.length == 3) {
            victim = world.getPlayerStorage().getCharacterByName(args[1]);
        }
        if (victim != null) {
            victim.levelUp(true);
        } else {
            chr.message(args[1] + " is offline or doesn't exist.");
        }
    }

    @Command
    @Syntax("!maxstat (name)")
    @Description("Maxes all of (name)'s stats.")
    @Alias({"maxstats"})
    public void maxstat() {
        final String[] s = {"setall", String.valueOf(Short.MAX_VALUE)};
        if (args.length == 1) {
            args = new String[2];
            args[1] = String.valueOf(Short.MAX_VALUE);
            this.setall();
            chr.setLevel(255);
            chr.setFame(13337);
            chr.setMaxHp(30000);
            chr.setMaxMp(30000);
            chr.updateSingleStat(MapleStat.LEVEL, 255);
            chr.updateSingleStat(MapleStat.FAME, 13337);
            chr.updateSingleStat(MapleStat.MAXHP, 30000);
            chr.updateSingleStat(MapleStat.MAXMP, 30000);
            chr.message("Your stats have been maxed.");
        } else if (args.length == 2) {
            MapleCharacter victim = world.getPlayerStorage().getCharacterByName(args[1]);
            if (victim != null) {
                chr = victim;
                args[1] = String.valueOf(Short.MAX_VALUE);
                this.setall();
                victim.setLevel(255);
                victim.setFame(13337);
                victim.setMaxHp(30000);
                victim.setMaxMp(30000);
                victim.updateSingleStat(MapleStat.LEVEL, 255);
                victim.updateSingleStat(MapleStat.FAME, 13337);
                victim.updateSingleStat(MapleStat.MAXHP, 30000);
                victim.updateSingleStat(MapleStat.MAXMP, 30000);
                victim.message("Your stats have been maxed.");
            } else {
                chr.message("Unable to find " + victim + ".");
            }
        }
    }

    @Command
    @Syntax("!maxskills (name)")
    @Description("Maxes all of (name)'s skills.")
    @Alias({"maxallskills"})
    public void maxskills() {
        if (args.length == 1) {
            for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren()) {
                try {
                    ISkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                    chr.changeSkillLevel(skill, (byte) skill.getMaxLevel(), skill.getMaxLevel(), -1);
                } catch (NumberFormatException nfe) {
                    break;
                } catch (NullPointerException npe) {
                    continue;
                }
            }
            chr.message("Your skills have been maxed.");
        } else if (args.length == 2) {
            MapleCharacter victim = world.getPlayerStorage().getCharacterByName(args[1]);
            if (victim != null) {
                for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren()) {
                    try {
                        ISkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                        if (!skill.isGMSkill() || victim.isGM()) {
                            victim.changeSkillLevel(skill, (byte) skill.getMaxLevel(), skill.getMaxLevel(), -1);
                        }
                    } catch (NumberFormatException nfe) {
                        break;
                    } catch (NullPointerException npe) {
                        continue;
                    }
                }
                victim.message("Your skills have been maxed.");
            } else {
                chr.message("Unable to find " + args[1] + ".");
            }
        }
    }

    @Command
    @Syntax("!meso [amount]")
    @Description("Gives [amount] mesos to you.")
    public void meso() {
        chr.gainMeso(Integer.parseInt(args[1]), true);
    }

    @Command
    @Syntax("!nnotice [message]")
    @Description("Sends a normal notice to the server. EX: '[NOTICE] Hello World!'")
    @Alias({"nn"})
    public void nnotice() {
        Server.getInstance().broadcastMessage(chr.getWorld(), MaplePacketCreator.serverNotice(6, "[NOTICE] " + StringUtil.joinStringFrom(args, 1, " ")));
    }

    @Command
    @Syntax("!notice [message]")
    @Description("Sends a message to the server with your name. EX: '[YOURNAME] Hello World!'")
    @Alias({"n"})
    public void notice() {
        Server.getInstance().broadcastMessage(chr.getWorld(), MaplePacketCreator.serverNotice(6, "[" + chr.getName() + "] " + StringUtil.joinStringFrom(args, 1, " ")));
    }

    @Command
    @Syntax("!say [message]")
    @Description("Sends a message to all GM's.")
    @Alias({"s"})
    public void say() {
        Server.getInstance().broadcastGMMessage(chr.getWorld(), MaplePacketCreator.serverNotice(5, "[" + chr.getName() + "] " + StringUtil.joinStringFrom(args, 1, " ")));
    }

    @Command
    @Syntax("!openportal [portalname]")
    @Description("Opens the [portalname] portal.")
    public void openportal() {
        chr.getMap().getPortal(args[1]).setPortalState(true);
    }

    @Command
    @Syntax("!closeportal [portalname]")
    @Description("Closes the [portalname] portal.")
    public void closeportal() {
        chr.getMap().getPortal(args[1]).setPortalState(false);
    }

    @Command
    @Description("Starts an event in this map on this channel.")
    public void startevent() {
        for (MapleCharacter p : this.chr.getMap().getCharacters()) {
            this.chr.getMap().startEvent(p);
        }
        client.getChannelServer().setEvent(null);
    }

    @Command
    @Syntax("!scheduleevent [eventtype]")
    @Description("Creates an event in your map.")
    public void scheduleevent() {
        if (client.getPlayer().getMap().hasEventNPC()) {
            if (args[1].equals("treasure")) {
                client.getChannelServer().setEvent(new MapleEvent(109010000, 50));
            } else if (args[1].equals("ox")) {
                client.getChannelServer().setEvent(new MapleEvent(109020001, 50));
                server.broadcastMessage(chr.getWorld(), MaplePacketCreator.serverNotice(0, "Hello Scania let's play an event in " + chr.getMap().getMapName() + " CH " + client.getChannel() + "! " + chr.getMap().getEventNPC()));
            } else if (args[1].equals("ola")) {
                client.getChannelServer().setEvent(new MapleEvent(109030101, 50)); // Wrong map but still Ola Ola
                server.broadcastMessage(chr.getWorld(), MaplePacketCreator.serverNotice(0, "Hello Scania let's play an event in " + chr.getMap().getMapName() + " CH " + client.getChannel() + "! " + chr.getMap().getEventNPC()));
            } else if (args[1].equals("fitness")) {
                client.getChannelServer().setEvent(new MapleEvent(109040000, 50));
                server.broadcastMessage(chr.getWorld(), MaplePacketCreator.serverNotice(0, "Hello Scania let's play an event in " + chr.getMap().getMapName() + " CH " + client.getChannel() + "! " + chr.getMap().getEventNPC()));
            } else if (args[1].equals("snowball")) {
                client.getChannelServer().setEvent(new MapleEvent(109060001, 50));
                server.broadcastMessage(chr.getWorld(), MaplePacketCreator.serverNotice(0, "Hello Scania let's play an event in " + chr.getMap().getMapName() + " CH " + client.getChannel() + "! " + chr.getMap().getEventNPC()));
            } else if (args[1].equals("coconut")) {
                client.getChannelServer().setEvent(new MapleEvent(109080000, 50));
                server.broadcastMessage(chr.getWorld(), MaplePacketCreator.serverNotice(0, "Hello Scania let's play an event in " + chr.getMap().getMapName() + " CH " + client.getChannel() + "! " + chr.getMap().getEventNPC()));
            } else {
                chr.message("Wrong Syntax: /scheduleevent treasure, ox, ola, fitness, snowball or coconut");
            }
        } else {
            chr.message("You can only use this command in the following maps: 60000, 104000000, 200000000, 220000000");
        }
    }

    @Command
    @Description("Spawns a Papulatus where your standing.")
    public void pap() {
        chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8500001), chr.getPosition());
    }

    @Command
    @Description("Spawns a Pianus where your standing.")
    public void pianus() {
        chr.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8510000), chr.getPosition());
    }

    @Command
    @Syntax("!servermessage [message]")
    @Description("Sets the scrolling server message to [message].")
    @Alias({"sm"})
    public void servermessage() {
        for (Channel ch : server.getChannelsFromWorld(chr.getWorld())) {
            ch.setChannelMessage(StringUtil.joinStringFrom(args, 1, " "));
        }
    }

    @Command
    @Syntax("!warpsnowball")
    @Description("Warps all players in the map to the snowball map.")
    public void warpsnowball() {
        for (MapleCharacter p : this.chr.getMap().getCharacters()) {
            p.changeMap(109060000, p.getTeam());
        }
    }

    @Command
    @Description("Warps all players to you.")
    public void warpallhere() {
        MapleMap toMap = chr.getMap();
        Byte ch = client.getChannel();
        for (MapleCharacter mch : world.getPlayerStorage().getAllCharacters()) {
            warpToMapAndChannel(mch, ch, toMap, chr.getPosition());
        }
    }

    @Command
    @Description("Warps all players in your channel to you.")
    public void warpchannelhere() {
        if (args.length == 1) {
            MapleMap toMap = chr.getMap();
            Byte ch = client.getChannel();
            for (MapleCharacter mch : world.getPlayerStorage().getAllCharacters()) {
                warpToMapAndChannel(mch, ch, toMap, chr.getPosition());
            }
        } else if (args.length == 2) {
            MapleMap toMap = chr.getMap();
            Byte ch = Byte.parseByte(args[1]);
            if (ch > 0 && ch <= Server.getInstance().getAllChannels().size()) {
                for (MapleCharacter mch : world.getPlayerStorage().getAllCharacters()) {
                    warpToMapAndChannel(mch, ch, toMap, chr.getPosition());
                }
            } else {
                chr.message("Channel doesn't exist.");
            }
        }
    }

    @Command
    @Syntax("!warpmap [mapid]")
    @Description("Warps all players in your map to another map.")
    public void warpmap() {
        int mapId = -1;
        try {
            mapId = Integer.parseInt(sub);
        } catch(NumberFormatException nfe) {}
        if(mapId >= 0) {
            MapleMap targetMap = null;
            try {
                targetMap = this.world.getChannel(client.getChannel()).getMapFactory().getMap(mapId);
            } catch(RuntimeException re) {
            }
            if(targetMap != null) {
                for (MapleCharacter p : chr.getMap().getCharacters()) {
                    if(p != null)
                        p.changeMap(targetMap, targetMap.getPortal(0));
                }
            } else {
                chr.message("Unable to find map with id '" + sub + "'");
            }
        } else {
            chr.message("Incorrect map ID: " + sub);
        }
    }

    @Command
    @Syntax("!warphere [name]")
    @Description("Warps [name] to you.")
    public void warphere() {
        channel.getPlayerStorage().getCharacterByName(args[1]).changeMap(chr.getMap(), chr.getMap().findClosestSpawnpoint(chr.getPosition()));
    }

    @Command
    @Description("Warps things.")
    @Alias({"w"})
    public void warp() {
        MapleCharacter victim = world.getPlayerStorage().getCharacterByName(args[1]);
        if (victim != null) {
            if (args.length == 2) { // !warp <targetName>
                Byte vChannel = victim.getClient().getChannel();
                warpToMapAndChannel(chr, vChannel, victim.getMap(), victim.getPosition());
            } else if (args.length == 3) { // !warp <victimName> <targetName>
                MapleCharacter target = world.getPlayerStorage().getCharacterByName(args[2]);
                if (target != null) {
                    Byte tChannel = target.getClient().getChannel();
                    warpToMapAndChannel(victim, tChannel, target.getMap(), target.getPosition());
                } else { // !warp <victimName> <mapid>
                    int mapId = -1;
                    try {
                        mapId = Integer.parseInt(args[2]);
                    } catch(NumberFormatException nfe) {
                    }
                    if(mapId >= 0) {
                        Byte vChannel = victim.getClient().getChannel();
                        MapleMap targetMap = null;
                        try {
                            targetMap = this.world.getChannel(vChannel).getMapFactory().getMap(mapId);
                        } catch(RuntimeException re) {
                        }
                        if (targetMap != null) {
                            warpToMapAndChannel(victim, vChannel, targetMap, null);
                        } else {
                            chr.message("Unable to warp " + args[1] + " to " + args[2] + ".");
                        }
                    } else {
                        chr.message("Unable to warp " + args[1] + " to " + args[2] + ".");
                    }
                }
            } else if (args.length == 4) { // !warp <victimName> <mapid> <portal>
                int mapId = -1;
                try {
                    mapId = Integer.parseInt(args[2]);
                } catch(NumberFormatException nfe) {
                }
                if(mapId >= 0) {
                    Byte vChannel = victim.getClient().getChannel();
                    MapleMap targetMap = null;
                    try {
                        targetMap = this.world.getChannel(vChannel).getMapFactory().getMap(mapId);
                    } catch(RuntimeException re) {
                    }
                    if (targetMap != null) {
                        int portal = -1;
                        try {
                            portal = Integer.parseInt(args[3]);
                        } catch(NumberFormatException nfe) {
                        }
                        if (portal < 0 || targetMap.getPortal(portal) == null) {
                            portal = 0;
                        }
                        warpToMapAndChannel(victim, vChannel, targetMap, null);
                    } else {
                        chr.message("Unable to warp " + args[1] + " to " + args[2] + ".");
                    }
                } else {
                    chr.message("Incorrect map ID: " + args[2]);
                }
            }
        } else {
            // !warp [mapid] (portal)
            int mapId = -1;
            try {
                mapId = Integer.parseInt(args[1]);
            } catch(NumberFormatException nfe) { }
            if(mapId >= 0) {
                MapleMap targetMap = null;
                try {
                    targetMap = this.world.getChannel(client.getChannel()).getMapFactory().getMap(mapId);
                } catch(RuntimeException re) {
                }
                if (targetMap != null) {
                    int portal = 0;
                    if (args.length == 3) {
                        portal = Integer.parseInt(args[2]);
                        if (portal < 0 || targetMap.getPortal(portal) == null) {
                            portal = 0;
                        }
                    }
                    chr.changeMap(targetMap, targetMap.getPortal(portal));
                } else {
                    chr.message("Unable to warp to " + args[1] + ".");
                }
            } else {
                chr.message("Unable to warp to " + args[1] + ".");
            }
        }
    }

    @Command
    @Syntax("!setall [amount]")
    @Description("Sets all your stats to [amount].")
    public void setall() {
        final int x = Short.parseShort(args[1]);
        chr.setStr(x);
        chr.setDex(x);
        chr.setInt(x);
        chr.setLuk(x);
        chr.updateSingleStat(MapleStat.STR, x);
        chr.updateSingleStat(MapleStat.DEX, x);
        chr.updateSingleStat(MapleStat.INT, x);
        chr.updateSingleStat(MapleStat.LUK, x);
    }

    @Command
    @Syntax("!sp [amount]")
    @Description("Sets your sp to [amount].")
    public void sp() {
        chr.setRemainingSp(Integer.parseInt(args[1]));
        chr.updateSingleStat(MapleStat.AVAILABLESP, chr.getRemainingSp());
    }

    @Command
    @Syntax("!unban [name]")
    @Description("Unbans [name].")
    public void unban() {
        try {
            PreparedStatement p = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = -1 WHERE id = " + MapleCharacter.getIdByName(args[1]));
            p.executeUpdate();
            p.close();
        } catch (Exception e) {
            chr.message("Failed to unban " + args[1]);
            return;
        }
        chr.message("Unbanned " + args[1]);
    }

    @Command
    @Description("Does a item vac of the whole map.")
    public void vac() {
        double range = Double.POSITIVE_INFINITY;
        int count = ItemPickupHandler.vacItems(client, chr, chr.getPosition(), 
                Integer.MAX_VALUE, range, false).size();
        chr.message("You have vacuumed " + count + " items.");
    }

    @Command
    @Syntax("!effect [effectname]")
    @Description("Shows an [effectname] effect.")
    public void effect() {
        String effect = args[1];
        client.announce(MaplePacketCreator.showEffect(effect));
    }

    @Command
    @Syntax("!addskillexp (name) [type] [amount]")
    @Description("Adds [amount] exp for the [type] power skill for (name).")
    public void addskillexp() {
        MapleCharacter victim = null;
        int amount = 0;
        PowerSkillType sType = null;
        if (args.length == 3) { // !addskillexp <type> <amount>
            try {
                victim = chr;
                sType = PowerSkillType.valueOf(args[1].toUpperCase());
                amount = Integer.parseInt(args[2]);
            } catch (Exception ex) {
            }
            if (sType != null) {
                if (amount > 0) {
                    victim.addPowerSkillExp(sType, amount);
                }
            } else {
                chr.message("Power skill '" + args[1] + "' does not exist in command: '" + command + "'");
            }
        } else if (args.length == 4) { // !addskillexp [targetName] <type> <amount>
            try {
                victim = channel.getPlayerStorage().getCharacterByName(args[1]);
                sType = PowerSkillType.valueOf(args[2]);
                amount = Integer.parseInt(args[3]);
            } catch (Exception ex) {
            }
            if (victim != null) {
                if (sType != null) {
                    if (amount > 0) {
                        victim.addPowerSkillExp(sType, amount);
                        chr.message(amount + " exp has been added to the "
                                + PowerSkill.getName(sType) + " power skill for '" + victim.getName() + "'.");
                    }
                } else {
                    chr.message("Power skill '" + args[1] + "' does not exist in command: '" + command + "'");
                }
            } else {
                chr.message("Target '" + args[1] + "' was not found in the command: '" + command + "'");
            }
        }
    }

    @Command
    @Syntax("!setskillexp (name) [type] [amount]")
    @Description("Sets the exp of the [type] power skill to [amount] for (name).")
    public void setskillexp() {
        MapleCharacter victim = null;
        int amount = 0;
        PowerSkillType sType = null;
        if (args.length == 3) { // !setskillexp <type> <amount>
            try {
                sType = PowerSkillType.valueOf(args[1].toUpperCase());
                amount = Integer.parseInt(args[2]);
            } catch (Exception ex) {
            }
            if (sType != null) {
                if (amount >= 0) {
                    chr.getPowerSkill(sType).setExp(amount);
                    chr.message("Your exp for the '" + PowerSkill.getName(sType)
                            + "' power skill has been set to " + amount + ".");
                }
            } else {
                chr.message("Power skill '" + args[1] + "' does not exist in command: '" + command + "'");
            }
        } else if (args.length == 4) { // !setskillexp [targetName] <type> <amount>
            try {
                victim = channel.getPlayerStorage().getCharacterByName(args[1]);
                sType = PowerSkillType.valueOf(args[2]);
                amount = Integer.parseInt(args[3]);
            } catch (Exception ex) {
            }
            if (victim != null) {
                if (sType != null) {
                    if (amount >= 0) {
                        victim.getPowerSkill(sType).setExp(amount);
                        victim.message("Your exp for the '" + PowerSkill.getName(sType)
                                + "' power skill has been set to " + amount + ".");
                        chr.message(victim.getName() + "'s exp for the '" + PowerSkill.getName(sType)
                                + "' power skill has been set to " + amount + ".");
                    }
                } else {
                    chr.message("Power skill '" + args[1] + "' does not exist in command: '" + command + "'");
                }
            } else {
                chr.message("Target '" + args[1] + "' was not found in the command: '" + command + "'");
            }
        }
    }

    @Command
    @Syntax("!levelupskill (name) [type]")
    @Description("Levels up the [type] power skill for (name).")
    public void levelupskill() {
        MapleCharacter victim = null;
        PowerSkillType sType = null;
        if (args.length == 2) { // !levelupskill <type>
            try {
                sType = PowerSkillType.valueOf(args[1].toUpperCase());
            } catch (Exception ex) {
            }
            if (sType != null) {
                chr.levelupPowerSkill(sType);
            } else {
                chr.message("Power skill '" + args[1] + "' does not exist in command: '" + command + "'");
            }
        } else if (args.length == 3) { // !levelupskill [targetName] <type>
            try {
                victim = channel.getPlayerStorage().getCharacterByName(args[1]);
                sType = PowerSkillType.valueOf(args[2]);
            } catch (Exception ex) {
            }
            if (victim != null) {
                if (sType != null) {
                    victim.levelupPowerSkill(sType);
                } else {
                    chr.message("Power skill '" + args[1] + "' does not exist in command: '" + command + "'");
                }
            } else {
                chr.message("Target '" + args[1] + "' was not found in the command: '" + command + "'");
            }
        }
    }

    @Command
    @Syntax("!setskilllevel (name) [type] [amount]")
    @Description("Sets the [type] power skill level to [amount] for (name).")
    public void setskilllevel() {
        MapleCharacter victim = null;
        int amount = 0;
        PowerSkillType sType = null;
        if (args.length == 3) { // !setskilllevel <type> <amount>
            try {
                sType = PowerSkillType.valueOf(args[1].toUpperCase());
                amount = Integer.parseInt(args[2]);
            } catch (Exception ex) {
            }
            if (sType != null) {
                if (amount >= 0) {
                    chr.getPowerSkill(sType).setLevel(amount);
                    chr.getPowerSkill(sType).setExp(ExpTable.getSkillExpNeededForLevel(amount));
                    chr.message("Your level for the '" + PowerSkill.getName(sType)
                            + "' power skill has been set to " + amount + ".");
                }
            } else {
                chr.message("Power skill '" + args[1] + "' does not exist in command: '" + command + "'");
            }
        } else if (args.length == 4) { // !setskilllevel [targetName] <type> <amount>
            try {
                victim = channel.getPlayerStorage().getCharacterByName(args[1]);
                sType = PowerSkillType.valueOf(args[2]);
                amount = Integer.parseInt(args[3]);
            } catch (Exception ex) {
            }
            if (victim != null) {
                if (sType != null) {
                    if (amount >= 0) {
                        victim.getPowerSkill(sType).setLevel(amount);
                        victim.getPowerSkill(sType).setExp(ExpTable.getSkillExpNeededForLevel(amount));
                        victim.message("Your level for the '" + PowerSkill.getName(sType)
                                + "' power skill has been set to " + amount + ".");
                        chr.message(victim.getName() + "'s level for the '" + PowerSkill.getName(sType)
                                + "' power skill has been set to " + amount + ".");
                    }
                } else {
                    chr.message("Power skill '" + args[1] + "' does not exist in command: '" + command + "'");
                }
            } else {
                chr.message("Target '" + args[1] + "' was not found in the command: '" + command + "'");
            }
        }
    }
}
