/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation version 3 as published by
the Free Software Foundation. You may not use, modify or distribute
this program under any other version of the GNU Affero General Public
License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package scripting.npc;

import client.CharInfo;
import client.Equip;
import client.IItem;
import client.ISkill;
import client.ItemFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import constants.ExpTable;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MapleJob;
import client.MaplePet;
import client.MapleSkinColor;
import client.MapleStat;
import client.SkillFactory;
import client.powerskills.PowerSkillType;
import constants.ServerConstants;
import tools.Randomizer;
import java.io.File;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import net.server.Channel;
import tools.DatabaseConnection;
import net.server.MapleParty;
import net.server.MaplePartyCharacter;
import net.server.Server;
import net.server.World;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import scripting.AbstractPlayerInteraction;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.events.gm.MapleEvent;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.partyquest.Pyramid;
import server.partyquest.Pyramid.PyramidMode;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.Pair;

/**
 *
 * @author Matze
 */
public class NPCConversationManager extends AbstractPlayerInteraction {

    private int npc;
    private String getText;

    public NPCConversationManager(MapleClient c, int npc) {
        super(c);
        this.npc = npc;
    }

    public int getNpc() {
        return npc;
    }

    public void dispose() {
        NPCScriptManager.getInstance().dispose(this);
    }

    public void sendNext(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", (byte) 0));
    }

    public void sendPrev(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", (byte) 0));
    }

    public void sendNextPrev(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", (byte) 0));
    }

    public void sendOk(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", (byte) 0));
    }

    public void sendYesNo(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 1, text, "", (byte) 0));
    }

    public void sendAcceptDecline(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0x0C, text, "", (byte) 0));
    }

    public void sendSimple(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 4, text, "", (byte) 0));
    }

    public void sendNext(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", speaker));
    }

    public void sendPrev(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", speaker));
    }

    public void sendNextPrev(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", speaker));
    }

    public void sendOk(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", speaker));
    }

    public void sendYesNo(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 1, text, "", speaker));
    }

    public void sendAcceptDecline(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0x0C, text, "", speaker));
    }

    public void sendSimple(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 4, text, "", speaker));
    }

    public void sendStyle(String text, int styles[]) {
        getClient().announce(MaplePacketCreator.getNPCTalkStyle(npc, text, styles));
    }

    public void sendGetNumber(String text, int def, int min, int max) {
        getClient().announce(MaplePacketCreator.getNPCTalkNum(npc, text, def, min, max));
    }

    public void sendGetText(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalkText(npc, text, ""));
    }

    /*
     * 0 = ariant colliseum
     * 1 = Dojo
     * 2 = Carnival 1
     * 3 = Carnival 2
     * 4 = Ghost Ship PQ?
     * 5 = Pyramid PQ
     * 6 = Kerning Subway
     */
    public void sendDimensionalMirror(String text) {
        getClient().announce(MaplePacketCreator.getDimensionalMirror(text));
    }

    public void setGetText(String text) {
        this.getText = text;
    }

    public String getText() {
        return this.getText;
    }

    public int getTextNumber() {
        return Integer.parseInt(this.getText);
    }

    public int getJobId() {
        return getPlayer().getJob().getId();
    }

    public String getJobName(int id) {
        return MapleJob.getById(id).getName();
    }
    
    public String getJobsMatching(int jobClass, int jobBase, int jobSpec, int jobUnique) {
        String ret = "";
        for(MapleJob j : MapleJob.values()) {
            int jc = (j.getId() / 1000); // Class Type
            int jb = ((j.getId() % 1000) / 100); // Base Job Type
            int js = ((j.getId() % 100) / 10);
            int ju = j.getId() % 10;
            // -1 = get all except unchosen
            if((jobClass == jc || (jobClass == -1 && jc != 0)) 
                    && (jobBase == jb || (jobBase == -1 && jb != 0)) 
                    && (jobSpec == js || (jobSpec == -1 && js != 0)) 
                    && (jobUnique == ju || (jobUnique == -1 && ju != 0))) {
                ret += "#L" + j.getId() + "#" + j.getName() + "#l\r\n";
            }
        }
        return ret;
    }

    public void startQuest(short id) {
        try {
            MapleQuest.getInstance(id).forceStart(getPlayer(), npc);
        } catch (NullPointerException ex) {
        }
    }

    public void completeQuest(short id) {
        try {
            MapleQuest.getInstance(id).forceComplete(getPlayer(), npc);
        } catch (NullPointerException ex) {
        }
    }

    public int getMeso() {
        return getPlayer().getMeso();
    }

    public void gainMeso(int gain) {
        getPlayer().gainMeso(gain, true, false, true);
    }

    public void gainExp(int gain) {
        getPlayer().gainExp(gain, true, true);
    }

    public int getLevel() {
        return getPlayer().getLevel();
    }

    public void showEffect(String effect) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(effect, 3));
    }

    public void playSound(String sound) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(sound, 4));
    }

    public void setHair(int hair) {
        getPlayer().setHair(hair);
        getPlayer().updateSingleStat(MapleStat.HAIR, hair);
        getPlayer().equipChanged();
    }

    public void setFace(int face) {
        getPlayer().setFace(face);
        getPlayer().updateSingleStat(MapleStat.FACE, face);
        getPlayer().equipChanged();
    }

    public void setSkin(int color) {
        getPlayer().setSkinColor(MapleSkinColor.getById(color));
        getPlayer().updateSingleStat(MapleStat.SKIN, color);
        getPlayer().equipChanged();
    }

    public int itemQuantity(int itemid) {
        return getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(itemid)).countById(itemid);
    }

    public void displayGuildRanks() {
        MapleGuild.displayGuildRanks(getClient(), npc);
    }

    @Override
    public MapleParty getParty() {
        return getPlayer().getParty();
    }

    @Override
    public void resetMap(int mapid) {
        getClient().getChannelServer().getMapFactory().getMap(mapid).resetReactors();
    }

    public void gainCloseness(int closeness) {
        for (MaplePet pet : getPlayer().getPets()) {
            if (pet.getCloseness() > 30000) {
                pet.setCloseness(30000);
                return;
            }
            pet.gainCloseness(closeness);
            while (pet.getCloseness() > ExpTable.getClosenessNeededForLevel(pet.getLevel())) {
                pet.setLevel((byte) (pet.getLevel() + 1));
                byte index = getPlayer().getPetIndex(pet);
                getClient().announce(MaplePacketCreator.showOwnPetLevelUp(index));
                getPlayer().getMap().broadcastMessage(getPlayer(), MaplePacketCreator.showPetLevelUp(getPlayer(), index));
            }
            IItem petz = getPlayer().getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
            getPlayer().getClient().announce(MaplePacketCreator.updateSlot(petz));
        }
    }

    public String getName() {
        return getPlayer().getName();
    }

    public int getGender() {
        return getPlayer().getGender();
    }

    public void changeJobById(int a) {
        getPlayer().changeJob(MapleJob.getById(a));
    }

    public void addRandomItem(int id) {
        MapleItemInformationProvider i = MapleItemInformationProvider.getInstance();
        MapleInventoryManipulator.addFromDrop(getClient(), i.randomizeStats((Equip) i.getEquipById(id)), true);
    }

    public MapleStatEffect getItemEffect(int itemId) {
        return MapleItemInformationProvider.getInstance().getItemEffect(itemId);
    }

    public void resetStats() {
        getPlayer().resetStats();
    }

    public void maxMastery() {
        for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren()) {
            try {
                ISkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));                
                getPlayer().changeSkillLevel(skill, (byte) 0, skill.getMaxLevel(), -1);
            } catch (NumberFormatException nfe) {
                break;
            } catch (NullPointerException npe) {
                continue;
            }
        }
    }

    public void processGachapon(int[] id, boolean remote) {
        int[] gacMap = {100000000, 101000000, 102000000, 103000000, 105040300, 800000000, 809000101, 809000201, 600000000, 120000000};
        int itemid = id[Randomizer.nextInt(id.length)];
        addRandomItem(itemid);
        if (!remote) {
            gainItem(5220000, (short) -1);
        }
        sendNext("You have obtained a #b#t" + itemid + "##k.");
        getClient().getChannelServer().broadcastPacket(MaplePacketCreator.gachaponMessage(getPlayer().getInventory(MapleInventoryType.getByType((byte) (itemid / 1000000))).findById(itemid), c.getChannelServer().getMapFactory().getMap(gacMap[(getNpc() != 9100117 && getNpc() != 9100109) ? (getNpc() - 9100100) : getNpc() == 9100109 ? 8 : 9]).getMapName(), getPlayer()));
    }

    public void disbandAlliance(MapleClient c, int allianceId) {
        PreparedStatement ps = null;
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM `alliance` WHERE id = ?");
            ps.setInt(1, allianceId);
            ps.executeUpdate();
            ps.close();
            Server.getInstance().allianceMessage(c.getPlayer().getGuild().getAllianceId(), MaplePacketCreator.disbandAlliance(allianceId), -1, -1);
            Server.getInstance().disbandAlliance(allianceId);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        }
    }

    public boolean canBeUsedAllianceName(String name) {
        if (name.contains(" ") || name.length() > 12) {
            return false;
        }
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name FROM alliance WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ps.close();
                rs.close();
                return false;
            }
            ps.close();
            rs.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static MapleAlliance createAlliance(MapleCharacter chr1, MapleCharacter chr2, String name) {
        int id = 0;
        int guild1 = chr1.getGuildId();
        int guild2 = chr2.getGuildId();
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO `alliance` (`name`, `guild1`, `guild2`) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setInt(2, guild1);
            ps.setInt(3, guild2);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            id = rs.getInt(1);
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        MapleAlliance alliance = new MapleAlliance(name, id, guild1, guild2);
        try {
            Server.getInstance().setGuildAllianceId(guild1, id);
            Server.getInstance().setGuildAllianceId(guild2, id);
            chr1.setAllianceRank(1);
            chr1.saveGuildStatus();
            chr2.setAllianceRank(2);
            chr2.saveGuildStatus();
            Server.getInstance().addAlliance(id, alliance);
            Server.getInstance().allianceMessage(id, MaplePacketCreator.makeNewAlliance(alliance, chr1.getClient()), -1, -1);
        } catch (Exception e) {
            return null;
        }
        return alliance;
    }

    public List<MapleCharacter> getPartyMembers() {
        if (getPlayer().getParty() == null) {
            return null;
        }
        List<MapleCharacter> chars = new LinkedList<MapleCharacter>();
        for (Channel channel : Server.getInstance().getChannelsFromWorld(getPlayer().getWorld())) {
            for (MapleCharacter chr : channel.getPartyMembers(getPlayer().getParty())) {
                if (chr != null) {
                    chars.add(chr);
                }
            }
        }
        return chars;
    }

    public void warpParty(int id) {
        for (MapleCharacter mc : getPartyMembers()) {
            if (id == 925020100) {
                mc.setDojoParty(true);
            }
            mc.changeMap(getWarpMap(id));
        }
    }

    public boolean hasMerchant() {
        return getPlayer().hasMerchant();
    }

    public boolean hasMerchantItems() {
        try {
            if (!ItemFactory.MERCHANT.loadItems(getPlayer().getId(), false).isEmpty()) {
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
        if (getPlayer().getMerchantMeso() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void showFredrick() {
        c.announce(MaplePacketCreator.getFredrick(getPlayer()));
    }

    public int partyMembersInMap() {
        int inMap = 0;
        for (MapleCharacter char2 : getPlayer().getMap().getCharacters()) {
            if (char2.getParty() == getPlayer().getParty()) {
                inMap++;
            }
        }
        return inMap;
    }

    public MapleEvent getEvent() {
        return c.getChannelServer().getEvent();
    }

    public void divideTeams() {
        if (getEvent() != null) {
            getPlayer().setTeam(getEvent().getLimit() % 2); //muhaha :D
        }
    }

    public boolean createPyramid(String mode, boolean party) {
        PyramidMode mod = PyramidMode.valueOf(mode);

        MapleParty partyz = getPlayer().getParty();
        MapleMapFactory mf = c.getChannelServer().getMapFactory();

        MapleMap map = null;
        int mapid = 926010100;
        if (party) mapid += 10000;
        mapid += (mod.getMode() * 1000);

        for (byte b = 0; b < 5; b++) {//They cannot warp to the next map before the timer ends (:
            map = mf.getMap(mapid + b);
            if (map.getCharacters().size() > 0) {
                map = null; continue;
            } else break;
        }

        if (map == null) return false;
        
        if (!party) partyz = new MapleParty(-1, new MaplePartyCharacter(getPlayer()));
        Pyramid py = new Pyramid(partyz, mod, map.getId());
        getPlayer().setPartyQuest(py);
        py.warp(mapid);
        dispose();
        return true;
    }

    public Channel getChannelServer() {
        return getClient().getChannelServer();
    }

    public World getWorldServer() {
        return getClient().getWorldServer();
    }

    public int getMesoRate() {
        return getWorldServer().getMesoRate();
    }

    public int getDropRate() {
        return getWorldServer().getDropRate();
    }

    public int getBossDropRate() {
        return getWorldServer().getBossDropRate();
    }

    public int getExpRate() {
        return getWorldServer().getExpRate();
    }

    public int getMaxStat() {
        return ServerConstants.MAX_STAT;
    }

    public int getLevelCap() {
        return ServerConstants.MAX_LEVEL;
    }

    public int getRebirthLevel(boolean koc) {
        return koc ? ServerConstants.KOC_REBIRTH_LEVEL : ServerConstants.REBIRTH_LEVEL;
    }

    public int getStr() {
        return getPlayer().getStr();
    }

    public void addStr(int amount) {
        getPlayer().setStr(getPlayer().getStr() + amount);
        getPlayer().updateSingleStat(MapleStat.STR, getPlayer().getStr());
    }

    public void subtractStr(int amount) {
        getPlayer().setStr(getPlayer().getStr() - amount);
        getPlayer().updateSingleStat(MapleStat.STR, getPlayer().getStr());
    }

    public int getDex() {
        return getPlayer().getDex();
    }

    public void addDex(int amount) {
        getPlayer().setDex(getPlayer().getDex() + amount);
        getPlayer().updateSingleStat(MapleStat.DEX, getPlayer().getDex());
    }

    public void subtractDex(int amount) {
        getPlayer().setDex(getPlayer().getDex() - amount);
        getPlayer().updateSingleStat(MapleStat.DEX, getPlayer().getDex());
    }

    public int getInt() {
        return getPlayer().getInt();
    }

    public void addInt(int amount) {
        getPlayer().setInt(getPlayer().getInt() + amount);
        getPlayer().updateSingleStat(MapleStat.INT, getPlayer().getInt());
    }

    public void subtractInt(int amount) {
        getPlayer().setInt(getPlayer().getInt() - amount);
        getPlayer().updateSingleStat(MapleStat.INT, getPlayer().getInt());
    }

    public int getLuk() {
        return getPlayer().getLuk();
    }

    public void addLuk(int amount) {
        getPlayer().setLuk(getPlayer().getLuk() + amount);
        getPlayer().updateSingleStat(MapleStat.LUK, getPlayer().getLuk());
    }

    public void subtractLuk(int amount) {
        getPlayer().setLuk(getPlayer().getLuk() - amount);
        getPlayer().updateSingleStat(MapleStat.LUK, getPlayer().getLuk());
    }

    public int getAp() {
        return getPlayer().getRemainingAp();
    }

    public void addAp(int amount) {
        getPlayer().setRemainingAp(getPlayer().getRemainingAp() + amount);
        getPlayer().updateSingleStat(MapleStat.AVAILABLEAP, getPlayer().getRemainingAp());
    }

    public void subtractAp(int amount) {
        getPlayer().setRemainingAp(getPlayer().getRemainingAp() - amount);
        getPlayer().updateSingleStat(MapleStat.AVAILABLEAP, getPlayer().getRemainingAp());
    }

    public int getSp() {
        return getPlayer().getRemainingSp();
    }

    public void addSp(int amount) {
        getPlayer().setRemainingSp(getPlayer().getRemainingSp() + amount);
        getPlayer().updateSingleStat(MapleStat.AVAILABLESP, getPlayer().getRemainingSp());
    }

    public void subtractSp(int amount) {
        getPlayer().setRemainingSp(getPlayer().getRemainingSp() - amount);
        getPlayer().updateSingleStat(MapleStat.AVAILABLESP, getPlayer().getRemainingSp());
    }

    public int getStoredAp() {
        return getPlayer().getStoredAp();
    }

    public void addStoredAp(int amount) {
        getPlayer().setStoredAp(getPlayer().getStoredAp() + amount);
    }

    public void subtractStoredAp(int amount) {
        getPlayer().setStoredAp(getPlayer().getStoredAp() - amount);
    }

    public String getTransferedInfo(){
        return getPlayer().getTransferedInfo();
    }

    public String getTransferedInfo(String id){
        return getPlayer().getTransferedInfo(id);
    }

    public int getTransferedInfoInt(String id){
        return Integer.parseInt(getPlayer().getTransferedInfo(id));
    }

    public double getTransferedInfoDouble(String id){
        return Double.parseDouble(getPlayer().getTransferedInfo(id));
    }

    public long getTransferedInfoLong(String id){
        return Long.parseLong(getPlayer().getTransferedInfo(id));
    }

    public boolean getTransferedInfoBool(String id){
        return Boolean.parseBoolean(getPlayer().getTransferedInfo(id));
    }

    public void addTransferedInfo(String id, String value){
        getPlayer().addTransferedInfo(id, value);
    }

    public void addTransferedInfo(String id, int value){
        getPlayer().addTransferedInfo(id, String.valueOf(value));
    }

    public void addTransferedInfo(String id, double value){
        getPlayer().addTransferedInfo(id, String.valueOf(value));
    }

    public void addTransferedInfo(String id, long value){
        getPlayer().addTransferedInfo(id, String.valueOf(value));
    }

    public void addTransferedInfo(String id, boolean value){
        getPlayer().addTransferedInfo(id, String.valueOf(value));
    }

    public void removeTransferedInfo(String id){
        getPlayer().removeTransferedInfo(id);
    }

    public String EquipList() {
        StringBuilder str = new StringBuilder();
        MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
        List<String> stra = new LinkedList<String>();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (IItem item : equip.list()) {
            int id = item.getItemId();
            MapleInventoryType type = ii.getInventoryType(id);
            if (id < 1902000 || id > 1902999)//mounts
                if (id < 1912000 || id > 1912999)//saddles
                    if (!type.equals(MapleInventoryType.CASH))
                        stra.add("#L" + id + "##v" + id + "##t" + id + "##l\r\n");
        }
        for (String strb : stra)
            str.append(strb);
        if(!str.toString().contains("#L"))
            return "There are no Equips in your inventory.";
        return str.toString();
    }

    public void giveCustomStatItem(Equip equip, int str, int dex, int _int, int luk, int mAtk, int wAtk, int mDef, int wDef, int hp, int mp, int acc, int avo, int hands, int jump, int speed, int mSlots, String owner) {
        MapleItemInformationProvider i = MapleItemInformationProvider.getInstance();
        MapleInventoryManipulator.addFromDrop(getClient(), i.giveCustomStatItem(equip, str, dex, _int, luk, mAtk, wAtk, mDef, wDef, hp, mp, acc, avo, hands, jump, speed, mSlots, owner), true);
        getPlayer().equipChanged();
    }

    public void giveLevelStatItem(Equip equip, int level, String owner) {
        MapleItemInformationProvider i = MapleItemInformationProvider.getInstance();
        MapleInventoryManipulator.addFromDrop(getClient(), i.giveLevelStatItem(equip, level, owner), true);
    }

    public int getItemStatValue(Equip equip, int type) {
        int ret = 0;
        if (equip == null)
            return ret;
        switch(type) {
            case 0: ret = equip.getStr();break;
            case 1: ret = equip.getDex();break;
            case 2: ret = equip.getInt();break;
            case 3: ret = equip.getLuk();break;
            case 4: ret = equip.getMatk();break;
            case 5: ret = equip.getWatk();break;
            case 6: ret = equip.getMdef();break;
            case 7: ret = equip.getWdef();break;
            case 8: ret = equip.getHp();break;
            case 9: ret = equip.getMp();break;
            case 10: ret = equip.getAcc();break;
            case 11: ret = equip.getAvoid();break;
            case 12: ret = equip.getHands();break;
            case 13: ret = equip.getJump();break;
            case 14: ret = equip.getSpeed();break;
            case 15: ret = equip.getUpgradeSlots();break;
        }
        return ret;
    }

    public Equip getEquipBySlot(byte slot) {
        MapleInventory iequip = getPlayer().getInventory(MapleInventoryType.EQUIP);
        Equip equip = (Equip) iequip.getItem(slot);
        return equip;
    }

    public Equip getEquipById(int id){
        return getEquipById(id, false);
    }

    public Equip getEquipById(int id, boolean fromCharacter) {
        Equip ret = null;
        if(!fromCharacter){
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            IItem item = ii.getEquipById(id);
            MapleInventoryType type = ii.getInventoryType(id);
            if (type.equals(MapleInventoryType.EQUIP)) {
                ret = (Equip) item;
            }
        } else {
            MapleInventory iequip = getPlayer().getInventory(MapleInventoryType.EQUIP);
            ret = (Equip) iequip.findById(id);
        }
        return ret;
    }

    public Equip getEquipWithSameStats(Equip equip){
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            Equip ret = (Equip) ii.getEquipById(equip.getItemId());
            ret.setAcc(equip.getAcc());
            ret.setAvoid(equip.getAvoid());
            ret.setDex(equip.getDex());
            ret.setHands(equip.getHands());
            ret.setHp(equip.getHp());
            ret.setInt(equip.getInt());
            ret.setJump(equip.getJump());
            ret.setLevel(equip.getLevel());
            //ret.setLocked(equip.getLocked());
            ret.setLuk(equip.getLuk());
            ret.setMatk(equip.getMatk());
            ret.setMdef(equip.getMdef());
            ret.setMp(equip.getMp());
            ret.setOwner(equip.getOwner());
            ret.setProtect(equip.getProtect());
            ret.setSpeed(equip.getSpeed());
            ret.setStr(equip.getStr());
            ret.setUpgradeSlots(equip.getUpgradeSlots());
            ret.setWatk(equip.getWatk());
            ret.setWdef(equip.getWdef());
            return ret;
    }

    public String getAllEquipsByJob(int job){
        String ret = "";
        MapleItemInformationProvider i = MapleItemInformationProvider.getInstance();
        List<Pair<Integer, String>> items = i.getAllItems();
        for(Pair<Integer, String> item : items){
            if(i.getReqJob(item.getLeft()) == job){
                int id = item.getLeft();
                if(!i.isCash(id))
                    ret += "#L"+id+"##v"+id+"##t"+id+"# ("+i.getPrice(id)+" mesos)#l\r\n";
            }
        }
        return ret;
    }
    
    public String getEquipCategory(int itemId){
        String cat = "none";
        if (itemId >= 1010000 && itemId < 1040000 || itemId >= 1122000 && itemId < 1123000)
            cat = "Accessory";
        else if (itemId >= 1000000 && itemId < 1010000)
            cat = "Cap";
        else if (itemId >= 1102000 && itemId < 1103000)
            cat = "Cape";
        else if (itemId >= 1040000 && itemId < 1050000)
            cat = "Coat";
        else if (itemId >= 20000 && itemId < 22000)
            cat = "Face";
        else if (itemId >= 1080000 && itemId < 1090000)
            cat = "Glove";
        else if (itemId >= 1050000 && itemId < 1060000)
            cat = "Longcoat";
        else if (itemId >= 1060000 && itemId < 1070000)
            cat = "Pants";
        else if (itemId >= 1802000 && itemId < 1810000)
            cat = "PetEquip";
        else if (itemId >= 1112000 && itemId < 1120000)
            cat = "Ring";
        else if (itemId >= 1092000 && itemId < 1100000)
            cat = "Shield";
        else if (itemId >= 1070000 && itemId < 1080000)
            cat = "Shoes";
        else if (itemId >= 1900000 && itemId < 2000000)
            cat = "Taming";
        else if (itemId >= 1300000 && itemId < 1800000)
            cat = "Weapon";
        return cat;
    }

    public String getAllEquipsByCat(String cat, int limit, int page){
        return getAllEquipsByCat(-1, cat, limit, page);
    }

    //job:
    //0 = beginner
    //1 = warrior
    //2 = magician
    //3 = bowman
    //4 = thief
    //5 = pirate
    public String getAllEquipsByCat(int job, String cat, int limit, int page){
        String ret = "";
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int found = 0;
        int i = 0;
        int cpage = 1;
        if(page == 0)
            page = 1;
        for(Pair<Integer, String> item : ii.getAllEquips()){
            int id = item.getLeft();
            if(ii.getReqJob(id) == job || job == -1){
                if(!ii.isCash(id) && (getEquipCategory(id).equalsIgnoreCase(cat) || cat.equals(""))){
                    if(i < limit){
                        ret += "#L"+id+"##v"+id+"##e#t"+id+"##n ("+ii.getPrice(id)*getMesoRate()+" mesos)#l\r\n";
                        i++;
                    } else {
                        if(cpage != page){
                            ret = "";
                            i = 0;
                            cpage++;
                        }
                    }
                    found++;
                }
            }
        }
        if(page > 1)
            ret += "\r\n\r\n#L998#<Prev Page#l";
        if(found - (page * limit) > 0){
            if(ret.contains("#L998"))
                ret += " | ";
            ret += "#L999#Next Page>#l";
        }
        if(!ret.contains("#L"))
            ret = "There are no equips for this job under #e" + cat + "#n.\r\n";
        return ret;
    }

    //job:
    //0 = beginner
    //1 = warrior
    //2 = magician
    //3 = bowman
    //4 = thief
    //5 = pirate
    public String getAllCashByCat(String cat, int limit, int page){
        String ret = "";
        MapleItemInformationProvider prov = MapleItemInformationProvider.getInstance();
        int found = 0;
        int i = 0;
        int cpage = 1;
        if(page == 0)
            page = 1;
        for(Pair<Integer, String> item : prov.getAllEquips()){
            int id = item.getLeft();
            if(prov.isCash(id) && (getEquipCategory(id).equalsIgnoreCase(cat) || cat.equals(""))){
                if(i < limit){
                    ret += "#L"+id+"##v"+id+"##t"+id+"##l\r\n";
                    i++;
                } else {
                    if(cpage != page){
                        ret = "";
                        i = 0;
                        cpage++;
                    }
                }
                found++;
            }
        }
        if(page > 1)
            ret += "\r\n\r\n#L998#<Prev Page#l";
        if(found - (page * limit) > 0){
            if(ret.contains("#L998"))
                ret += " | ";
            ret += "#L999#Next Page>#l";
        }
        if(!ret.contains("#L"))
            ret = "There are no cash equips under #e" + cat + "#n.\r\n";
        return ret;
    }

    public String getPlayerStats(String name) {
        try {
            CharInfo stats = new CharInfo();
            if (stats.loadStats(name)) {
                boolean viewAll = getPlayer().isGM() || getPlayer().getName().equalsIgnoreCase(name);
                String output = "#e#b" + stats.getCName() + "'s Stats:#k#n\r\n";
                if (!stats.isBanned() || viewAll) {
                    if (stats.isOnline())
                        output += "#g#e" + stats.getCName() + " is online!#n#k\r\n";
                    else
                        output += "#r#e" + stats.getCName() + " is offline.#n#k\r\n";
                    if (!stats.canTalk())
                        output += "#r" + stats.getCName() + " is muted and cannot talk.\r\n";
                    if (viewAll)
                        output += "#r#eAccount ID:#n#k " + stats.getAccountId() + "\r\n";
                    output += "#r#eCharacter ID:#n#k " + stats.getCharId() + "\r\n";
                    if (!stats.isOnline())
                        output += "#r#eLast Online:#n#k " + stats.getLastLogin() + "\r\n";
                    output += "#r#eCreate Date:#n#k " + stats.getCreateDate() + "\r\n";
                    output += "#r#eLevel:#n#k " + stats.getLevel() + "\r\n";
                    output += "#r#eTotal Level:#n#k " + stats.getTotalLevel() + "\r\n";
                    output += "#r#eRebirths:#n#k " + stats.getRebirths() + "\r\n\r\n";
                    stats.setSkillFocus(PowerSkillType.ENCHANTMENT);
                    if(stats.hasSkill())
                        output += "#r#eEnchantment:#n#k " + stats.getSkillLevel() + " (" + stats.getSkillExp() + " / " + stats.getSkillNextExp() + ")\r\n";
                    stats.setSkillFocus(PowerSkillType.SKINNING);
                    if(stats.hasSkill())
                        output += "#r#eSkinning:#n#k " + stats.getSkillLevel() + " (" + stats.getSkillExp() + " / " + stats.getSkillNextExp() + ")\r\n";
                    stats.setSkillFocus(PowerSkillType.MAGNETO);
                    if(stats.hasSkill())
                        output += "#r#eMagneto:#n#k " + stats.getSkillLevel() + " (" + stats.getSkillExp() + " / " + stats.getSkillNextExp() + ")\r\n";
                    
                    if(stats.isOnline()){
                        output += "\r\n#r#eExp Rate:#n#k " + stats.getTotalExpRate() + " ( (" + getWorldServer().getExpRate() + (stats.getExpRate() > 0 ? " + " + stats.getExpRate() : "") + ") x " + stats.getExpCards() + ")\r\n";
                        output += "#r#eMeso Rate:#n#k " + stats.getTotalMesoRate() + " ( (" + getWorldServer().getMesoRate() + (stats.getMesoRate() > 0 ? " + " + stats.getMesoRate() : "") + ") x " + stats.getMesoCards() + ")\r\n";
                        output += "#r#eDrop Rate:#n#k " + stats.getTotalDropRate() + " ( (" + getWorldServer().getDropRate() + (stats.getDropRate() > 0 ? " + " + stats.getDropRate() : "") + ") x " + stats.getDropCards() + ")\r\n";
                    }
                    if (viewAll) {
                        output += "\r\n#r#eMap:#n#k " + stats.getMapName() + "\r\n";
                        output += "#r#eMesos:#n#k " + stats.getMeso() + "\r\n";
                    }
                    output += "#r#eWealth:#n#k " + stats.getWealth() + "\r\n";
                    output += "#r#eJob:#n#k " + stats.getJobName() + "\r\n";
                    output += "#r#eFame:#n#k " + stats.getFame() + "\r\n";
                    if (viewAll) {
                        output += "#r#eAp Per Level:#n#k " + stats.getApPerLevel() + "\r\n";
                        output += "#r#eStored Ap:#n#k " + stats.getStoredAp() + "\r\n";
                    }
                    if (!stats.getPartnerName().equals("None"))
                        output += "#r#ePartner:#n#k " + stats.getPartnerName() + "\r\n";
                    output += "#r#eGender:#n#k " + stats.getGender() + "\r\n";
                    if (viewAll && stats.getGmTitle() != null)
                        output += "#r#eGM Level:#n#k " + stats.getGmTitle() + "\r\n";
                    output += "#r#eGuild:#n#k " + stats.getGuildName() + "\r\n";
                    if (!stats.getGuildName().equals("None"))
                        output += "#r#eGuild Rank:#n#k " + stats.getGuildRank() + "\r\n";
                    output += "#r#eTotal Buddies:#n#k " + stats.getBuddies() + "\r\n";
                    output +=  "#r#eNx Cash#n#k " + stats.getNxCash() + "\r\n";;
                    if (viewAll && stats.getVotePoints() > 0)
                        output +=  "#r#eVote Points#n#k " + stats.getVotePoints() + "\r\n";;
                    if (stats.getDonation() > 0)
                        output +=  stats.getCName() + " is a donator and has donatated #g$" + stats.getDonation() + "\r\n";
                    else
                        output += "#r" + stats.getCName() + " is not a donator.#k";
                } else {
                    output += "#rYou cannot view this users info because he/she is banned.#k";
                }
                return output;
            } else {
                return "Error loading " + name + " from database. Make sure the user exists.";
            }
        } catch (Exception e) {
            System.out.println("CharInfo: " + e + " | " + e.getMessage());
            e.printStackTrace();
            return "Error loading " + name + " from database. Make sure the user exists.";
        }
    }

    public String getTop5Stats() {
        String out = "#bTop 5 Players:#k\r\n  Name | Level | Rebirths | Wealth\r\n";
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM characters WHERE gm < 10 ORDER BY totalLevel DESC, level DESC, exp DESC, name ASC LIMIT 5");
            ResultSet rs = ps.executeQuery();
            int a = 1;
            while (rs.next()) {
                out += a + ") #b" + rs.getString("name") + "#k | " + String.valueOf(rs.getInt("level")) + " | " + String.valueOf(rs.getInt("rebirths")) + " | " + String.valueOf(rs.getLong("wealth")) + "\r\n";
                a++;
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            System.out.println("CharInfo: " + e + " | " + e.getMessage());
            e.printStackTrace();
            out =  "Error loading top 5...";
        }
        if (!out.contains("#b"))
            out = "Error loading top 5...";
        return out;
    }
}
