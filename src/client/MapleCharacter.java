/* 
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation version 3 as published by
the Free Software Foundation. You may not use, modify or distribute
this program unader any cother version of the GNU Affero General Public
License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client;

import client.autoban.AutobanManager;
import client.groups.MapleGroup;
import client.powerskills.PowerSkill;
import client.powerskills.PowerSkillType;
import constants.ExpTable;
import constants.ItemConstants;
import constants.ServerConstants;
import constants.skills.*;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map.Entry;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import net.MaplePacket;
import net.server.*;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import scripting.event.EventInstanceManager;
import server.*;
import server.events.MapleEvents;
import server.events.RescueGaga;
import server.events.gm.MapleFitness;
import server.events.gm.MapleOla;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.*;
import server.partyquest.MonsterCarnival;
import server.partyquest.MonsterCarnivalParty;
import server.partyquest.PartyQuest;
import server.quest.MapleQuest;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;

public class MapleCharacter extends AbstractAnimatedMapleMapObject {

    private byte worldid;
    private int accountid, id;
    private int rank, rankMove, jobRank, jobRankMove;
    private int level, str, dex, luk, int_, hp, maxhp, mp, maxmp;
    private int hpMpApUsed;
    private int hair;
    private int face;
    private int remainingAp, remainingSp;
    private int fame;
    private int initialSpawnPoint;
    private int mapid;
    private int gender;
    private int currentPage, currentType = 0, currentTab = 1;
    private int chair;
    private int itemEffect;
    private int guildid, guildrank, allianceRank;
    private int messengerposition = 4;
    private int slots = 0;
    private int energybar;
    private int nextMap;
    private int nextMapPortal;
    // BossStory
    private int origAccountId;
    private int returnMap;
    private Timestamp createDate;
    private long lastUse = 0;
    private MapleGroup gmLevel;
    private int rebirths;
    private boolean vip;
    private int expRate = 1, mesoRate = 1, dropRate = 1;
    private int expRateCards = 1, mesoRateCards = 1, dropRateCards = 1;
    private boolean currencyConverted;
    private long nextCurrencyCheck;
    private String transferedInfo = "";
    private int totalLevel, autoAp, apPerLevel, storedAp;
    private boolean needsHelp;
    private String previousNames;
    private long wealth;
    private int wealthRanking, rebirthRanking;
    private boolean canTalk;
    private List<Integer> savedSkillIds = new ArrayList<Integer>();
    private final int[] tutorialMaps = {
        10000, 20000, 30000, 30001, 40000, 50000, // Adventurer
        130030000, 130030001, 130030002, 130030003, 130030004, 130030005, 130030006, // KOC
        914000000, 914000100, 914000200, 914000300, 914000400, 914000500 // Legend
    };
    // Power Skills
    private List<PowerSkill> powerSkills;
    // End BossStory
    private int ci = 0;
    private MapleFamily family;
    private int familyId;
    private int bookCover;
    private int markedMonster = 0;
    private int battleshipHp = 0;
    private int mesosTraded = 0;
    private int possibleReports = 10;
    private int dojoPoints, vanquisherStage, dojoStage, dojoEnergy,
            vanquisherKills;
    private int warpToId;
    private int omokwins, omokties, omoklosses, matchcardwins, matchcardties,
            matchcardlosses;
    private int married;
    private long dojoFinish, lastfametime, lastUsedCashItem, lastHealed;
    private transient int localmaxhp, localmaxmp, localstr, localdex, localluk,
            localint_, magic, watk;
    private boolean hidden, canDoor = true, Berserk, hasMerchant;
    private int linkedLevel = 0;
    private String linkedName = null;
    private boolean finishedDojoTutorial, dojoParty;
    private String name;
    private String chalktext;
    private String search = null;
    private AtomicInteger exp = new AtomicInteger();
    private AtomicInteger gachaexp = new AtomicInteger();
    private AtomicInteger meso = new AtomicInteger();
    private int merchantmeso;
    private BuddyList buddylist;
    private EventInstanceManager eventInstance = null;
    private HiredMerchant hiredMerchant = null;
    private MapleClient client;
    private MapleGuildCharacter mgc = null;
    private MaplePartyCharacter mpc = null;
    private MapleInventory[] inventory;
    private MapleJob job = MapleJob.Aventurer;
    private MapleMap map, dojoMap;// Make a Dojo pq instance
    private MapleMessenger messenger = null;
    private MapleMiniGame miniGame;
    private MapleMount maplemount;
    private MapleParty party;
    private MaplePet[] pets = new MaplePet[3];
    private MaplePlayerShop playerShop = null;
    private MapleShop shop = null;
    private MapleSkinColor skinColor = MapleSkinColor.NORMAL;
    private MapleStorage storage = null;
    private MapleTrade trade = null;
    private SavedLocation savedLocations[];
    private SkillMacro[] skillMacros = new SkillMacro[5];
    private List<Integer> lastmonthfameids;
    private Map<MapleQuest, MapleQuestStatus> quests;
    private Set<MapleMonster> controlled = new LinkedHashSet<MapleMonster>();
    private Map<Integer, String> entered = new LinkedHashMap<Integer, String>();
    private Set<MapleMapObject> visibleMapObjects = new LinkedHashSet<MapleMapObject>();
    private Map<ISkill, SkillEntry> skills = new LinkedHashMap<ISkill, SkillEntry>();
    private EnumMap<MapleBuffStat, MapleBuffStatValueHolder> effects = new EnumMap<MapleBuffStat, MapleBuffStatValueHolder>(
            MapleBuffStat.class);
    private Map<Integer, MapleKeyBinding> keymap = new LinkedHashMap<Integer, MapleKeyBinding>();
    private Map<Integer, MapleSummon> summons = new LinkedHashMap<Integer, MapleSummon>();
    private Map<Integer, MapleCoolDownValueHolder> coolDowns = new LinkedHashMap<Integer, MapleCoolDownValueHolder>(
            50);
    private EnumMap<MapleDisease, DiseaseValueHolder> diseases = new EnumMap<MapleDisease, DiseaseValueHolder>(
            MapleDisease.class);
    private List<MapleDoor> doors = new ArrayList<MapleDoor>();
    private ScheduledFuture<?> dragonBloodSchedule;
    private ScheduledFuture<?> mapTimeLimitTask = null;
    private ScheduledFuture<?> fullnessSchedule, fullnessSchedule_1,
            fullnessSchedule_2;
    private ScheduledFuture<?> hpDecreaseTask;
    private ScheduledFuture<?> beholderHealingSchedule, beholderBuffSchedule,
            BerserkSchedule;
    private ScheduledFuture<?> expiretask;
    private ScheduledFuture<?> recoveryTask;
    private byte recovery = 0;
    private List<ScheduledFuture<?>> timers = new ArrayList<ScheduledFuture<?>>();
    private NumberFormat nf = new DecimalFormat("#,###,###,###");
    private ArrayList<Integer> excluded = new ArrayList<Integer>();
    private MonsterBook monsterbook;
    private List<MapleRing> crushRings = new ArrayList<MapleRing>();
    private List<MapleRing> friendshipRings = new ArrayList<MapleRing>();
    private MapleRing marriageRing;
    private static String[] ariantroomleader = new String[3];
    private static int[] ariantroomslot = new int[3];
    private CashShop cashshop;
    private long portaldelay = 0, lastattack = 0;
    private short combocounter = 0;
    private List<String> blockedPortals = new ArrayList<String>();
    private ArrayList<String> area_data = new ArrayList<String>();
    private AutobanManager autoban;
    private boolean isbanned = false;
    private ScheduledFuture<?> pendantOfSpirit = null; // 1122017
    private byte pendantExp = 0, lastmobcount = 0;
    private int[] trockmaps = new int[5];
    private int[] viptrockmaps = new int[10];
    private Map<String, MapleEvents> events = new LinkedHashMap<String, MapleEvents>();
    private PartyQuest partyQuest = null;

    private MapleCharacter() {
        setStance(0);
        inventory = new MapleInventory[MapleInventoryType.values().length];
        savedLocations = new SavedLocation[SavedLocationType.values().length];
        powerSkills = new ArrayList<PowerSkill>();

        for (MapleInventoryType type : MapleInventoryType.values()) {
            byte b = 24;
            if (type == MapleInventoryType.CASH) {
                b = 96;
            }
            inventory[type.ordinal()] = new MapleInventory(type, (byte) b);
        }
        for (int i = 0; i < SavedLocationType.values().length; i++) {
            savedLocations[i] = null;
        }
        quests = new LinkedHashMap<MapleQuest, MapleQuestStatus>();
        setPosition(new Point(0, 0));
    }

    public static MapleCharacter getDefault(MapleClient c) {
        MapleCharacter ret = new MapleCharacter();
        ret.client = c;
        ret.gmLevel = MapleGroup.PLAYER;
        ret.rebirths = 0;
        ret.hp = 50;
        ret.maxhp = 50;
        ret.mp = 5;
        ret.maxmp = 5;
        ret.str = 12;
        ret.dex = 5;
        ret.int_ = 4;
        ret.luk = 4;
        ret.map = null;
        ret.job = MapleJob.Aventurer;
        ret.level = 1;
        ret.apPerLevel = 5;
        ret.accountid = c.getAccID();
        ret.origAccountId = c.getAccID();
        ret.buddylist = new BuddyList(20);
        ret.canTalk = true;
        ret.maplemount = null;
        ret.createDate = new Timestamp(System.currentTimeMillis());
        ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(24);
        ret.getInventory(MapleInventoryType.USE).setSlotLimit(24);
        ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(24);
        ret.getInventory(MapleInventoryType.ETC).setSlotLimit(24);
        int[] key = {18, 65, 2, 23, 3, 4, 5, 6, 16, 17, 19, 25, 26, 27, 31,
            34, 35, 37, 38, 40, 43, 44, 45, 46, 50, 56, 59, 60, 61, 62, 63,
            64, 57, 48, 29, 7, 24, 33, 41, 39};
        int[] type = {4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
            4, 4, 5, 5, 4, 4, 5, 6, 6, 6, 6, 6, 6, 5, 4, 5, 4, 4, 4, 4, 4};
        int[] action = {0, 106, 10, 1, 12, 13, 18, 24, 8, 5, 4, 19, 14, 15, 2,
            17, 11, 3, 20, 16, 9, 50, 51, 6, 7, 53, 100, 101, 102, 103,
            104, 105, 54, 22, 52, 21, 25, 26, 23, 27};
        for (int i = 0; i < key.length; i++) {
            ret.keymap.put(key[i], new MapleKeyBinding(type[i], action[i]));
        }
        // to fix the map 0 lol
        for (int i = 0; i < 5; i++) {
            ret.trockmaps[i] = 999999999;
        }
        for (int i = 0; i < 10; i++) {
            ret.viptrockmaps[i] = 999999999;
        }

        if (ret.isGM()) {
            ret.job = MapleJob.Super_GM;
            ret.level = 200;
            // int[] gmskills = {9001000, 9001001, 9001000, 9101000, 9101001,
            // 9101002, 9101003, 9101004, 9101005, 9101006, 9101007, 9101008};
        }
        return ret;
    }

    public void addCooldown(int skillId, long startTime, long length,
            ScheduledFuture<?> timer) {
        if (this.coolDowns.containsKey(Integer.valueOf(skillId))) {
            this.coolDowns.remove(skillId);
        }
        this.coolDowns.put(Integer.valueOf(skillId), new MapleCoolDownValueHolder(
                skillId, startTime, length, timer));
    }

    public void addCrushRing(MapleRing r) {
        crushRings.add(r);
    }

    public MapleRing getRingById(int id) {
        for (MapleRing ring : getCrushRings()) {
            if (ring.getRingId() == id) {
                return ring;
            }
        }
        for (MapleRing ring : getFriendshipRings()) {
            if (ring.getRingId() == id) {
                return ring;
            }
        }
        if (getMarriageRing().getRingId() == id) {
            return getMarriageRing();
        }

        return null;
    }

    public int addDojoPointsByMap() {
        int pts = 0;
        if (dojoPoints < 17000) {
            pts = 1 + ((getMap().getId() - 1) / 100 % 100) / 6;
            if (!dojoParty) {
                pts++;
            }
            this.dojoPoints += pts;
        }
        return pts;
    }

    public void addDoor(MapleDoor door) {
        doors.add(door);
    }

    public void addExcluded(int x) {
        excluded.add(x);
    }

    public void addFame(int famechange) {
        this.fame += famechange;
    }

    public void addFriendshipRing(MapleRing r) {
        friendshipRings.add(r);
    }

    public void recovery(int delta) {
        recovery++;
        addHP(delta);
        if (recovery == 6) {
            recoveryTask.cancel(false);
            recovery = 0;
        }
    }

    public void addHP(int delta) {
        setHp(hp + delta);
        updateSingleStat(MapleStat.HP, hp);
    }

    public void addMesosTraded(int gain) {
        this.mesosTraded += gain;
    }

    public void addMP(int delta) {
        setMp(mp + delta);
        updateSingleStat(MapleStat.MP, mp);
    }

    public void addMPHP(int hpDiff, int mpDiff) {
        setHp(hp + hpDiff);
        setMp(mp + mpDiff);
        updateSingleStat(MapleStat.HP, getHp());
        updateSingleStat(MapleStat.MP, getMp());
    }

    public void addPet(MaplePet pet) {
        for (int i = 0; i < 3; i++) {
            if (pets[i] == null) {
                pets[i] = pet;
                return;
            }
        }
    }

    public void addStat(int type, int up) {
        if (type == 1) {
            this.str += up;
            updateSingleStat(MapleStat.STR, str);
        } else if (type == 2) {
            this.dex += up;
            updateSingleStat(MapleStat.DEX, dex);
        } else if (type == 3) {
            this.int_ += up;
            updateSingleStat(MapleStat.INT, int_);
        } else if (type == 4) {
            this.luk += up;
            updateSingleStat(MapleStat.LUK, luk);
        }
    }

    public int addHP(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        MapleJob jobtype = player.getJob();
        int MaxHP = player.getMaxHp();
        if (player.getHpMpApUsed() > 9999 || MaxHP >= 30000) {
            return MaxHP;
        }
        if (jobtype.isA(MapleJob.Aventurer)) {
            MaxHP += 8;
        } else if (jobtype.isA(MapleJob.Warrior)
                || jobtype.isA(MapleJob.Dawn_Warrior_1)) {
            if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(10000000) : SkillFactory.getSkill(1000001)) > 0) {
                MaxHP += 20;
            } else {
                MaxHP += 8;
            }
        } else if (jobtype.isA(MapleJob.Magician)
                || jobtype.isA(MapleJob.Blaze_Wizard_1)) {
            MaxHP += 6;
        } else if (jobtype.isA(MapleJob.Bowman)
                || jobtype.isA(MapleJob.Wind_Archer_1)) {
            MaxHP += 8;
        } else if (jobtype.isA(MapleJob.Thief)
                || jobtype.isA(MapleJob.Night_Walker_1)) {
            MaxHP += 8;
        } else if (jobtype.isA(MapleJob.Pirate)
                || jobtype.isA(MapleJob.Thunder_Breaker_1)) {
            if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(15100000) : SkillFactory.getSkill(5100000)) > 0) {
                MaxHP += 18;
            } else {
                MaxHP += 8;
            }
        }
        return MaxHP;
    }

    public int addMP(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        int MaxMP = player.getMaxMp();
        if (player.getHpMpApUsed() > 9999 || player.getMaxMp() >= 30000) {
            return MaxMP;
        }
        if (player.getJob().isA(MapleJob.Aventurer)
                || player.getJob().isA(MapleJob.Noblesse)
                || player.getJob().isA(MapleJob.Legend)) {
            MaxMP += 6;
        } else if (player.getJob().isA(MapleJob.Warrior)
                || player.getJob().isA(MapleJob.Dawn_Warrior_1)
                || player.getJob().isA(MapleJob.Aran_1)) {
            MaxMP += 2;
        } else if (player.getJob().isA(MapleJob.Magician)
                || player.getJob().isA(MapleJob.Blaze_Wizard_1)) {
            if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(12000000) : SkillFactory.getSkill(2000001)) > 0) {
                MaxMP += 18;
            } else {
                MaxMP += 14;
            }

        } else if (player.getJob().isA(MapleJob.Bowman)
                || player.getJob().isA(MapleJob.Thief)) {
            MaxMP += 10;
        } else if (player.getJob().isA(MapleJob.Pirate)) {
            MaxMP += 14;
        }

        return MaxMP;
    }

    public void addSummon(int id, MapleSummon summon) {
        summons.put(id, summon);
    }

    public void addVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.add(mo);
    }

    public void ban(String reason, boolean dc) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = 1, banReason = ? WHERE id = ?");
            ps.setString(1, reason);
            ps.setInt(2, accountid);
            ps.executeUpdate();
            ps.close();
            this.isbanned = true;
        } catch (Exception e) {
        }

    }

    public static boolean ban(String id, String reason, boolean accountId) {
        PreparedStatement ps = null;
        try {
            Connection con = DatabaseConnection.getConnection();
            if (id.matches("/[0-9]{1,3}\\..*")) {
                ps = con.prepareStatement("INSERT INTO ipBans VALUES (DEFAULT, ?)");
                ps.setString(1, id);
                ps.executeUpdate();
                ps.close();
                return true;
            }
            if (accountId) {
                ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
            } else {
                ps = con.prepareStatement("SELECT accountId FROM characters WHERE name = ?");
            }

            boolean ret = false;
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PreparedStatement psb = DatabaseConnection.getConnection().prepareStatement(
                        "UPDATE accounts SET banned = 1, banReason = ? WHERE id = ?");
                psb.setString(1, reason);
                psb.setInt(2, rs.getInt(1));
                psb.executeUpdate();
                psb.close();
                ret = true;
            }
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException ex) {
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
            } catch (SQLException e) {
            }
        }
        return false;
    }

    public int calculateMaxBaseDamage(int watk) {
        int maxbasedamage;
        if (watk == 0) {
            maxbasedamage = 1;
        } else {
            IItem weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
            if (weapon_item != null) {
                MapleWeaponType weapon = MapleItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
                int mainstat;
                int secondarystat;
                if (weapon == MapleWeaponType.BOW
                        || weapon == MapleWeaponType.CROSSBOW) {
                    mainstat = localdex;
                    secondarystat = localstr;
                } else if ((getJob().isA(MapleJob.Thief) || getJob().isA(
                        MapleJob.Night_Walker_1))
                        && (weapon == MapleWeaponType.CLAW || weapon == MapleWeaponType.DAGGER)) {
                    mainstat = localluk;
                    secondarystat = localdex + localstr;
                } else {
                    mainstat = localstr;
                    secondarystat = localdex;
                }
                maxbasedamage = (int) (((weapon.getMaxDamageMultiplier()
                        * mainstat + secondarystat) / 100.0) * watk) + 10;
            } else {
                maxbasedamage = 0;
            }
        }
        return maxbasedamage;
    }

    public void cancelAllBuffs() {
        for (MapleBuffStatValueHolder mbsvh : new ArrayList<MapleBuffStatValueHolder>(
                effects.values())) {
            cancelEffect(mbsvh.effect, false, mbsvh.startTime);
        }
    }

    public void cancelBuffStats(MapleBuffStat stat) {
        List<MapleBuffStat> buffStatList = Arrays.asList(stat);
        deregisterBuffStats(buffStatList);
        cancelPlayerBuffs(buffStatList);
    }

    public void setCombo(short count) {
        if (combocounter > 30000) {
            combocounter = 30000;
            return;
        }
        combocounter = count;

    }

    public void setLastAttack(long time) {
        lastattack = time;
    }

    public short getCombo() {
        return combocounter;
    }

    public long getLastAttack() {
        return lastattack;
    }

    public int getLastMobCount() { // Used for skills that have mobCount at 1.
        // (a/b)
        return lastmobcount;
    }

    public void setLastMobCount(byte count) {
        lastmobcount = count;
    }

    public void newClient(MapleClient c) {
        c.setAccountName(this.client.getAccountName());// No null's for
        // accountName
        this.client = c;
        MaplePortal portal = map.findClosestSpawnpoint(getPosition());
        if (portal == null) {
            portal = map.getPortal(0);
        }
        this.setPosition(portal.getPosition());
        this.initialSpawnPoint = portal.getId();
        this.map = c.getChannelServer().getMapFactory().getMap(getMapId());
    }

    public void cancelBuffEffects() {
        for (MapleBuffStatValueHolder mbsvh : effects.values()) {
            mbsvh.schedule.cancel(false);
        }
        this.effects.clear();
    }

    public void setTotalLevel(int set) {
        totalLevel = set;
    }

    public int getTotalLevel() {
        return totalLevel;
    }

    public static class CancelCooldownAction implements Runnable {

        private int skillId;
        private WeakReference<MapleCharacter> target;

        public CancelCooldownAction(MapleCharacter target, int skillId) {
            this.target = new WeakReference<MapleCharacter>(target);
            this.skillId = skillId;
        }

        @Override
        public void run() {
            MapleCharacter realTarget = target.get();
            if (realTarget != null) {
                realTarget.removeCooldown(skillId);
                realTarget.client.announce(MaplePacketCreator.skillCooldown(
                        skillId, 0));
            }
        }
    }

    public void cancelEffect(int itemId) {
        cancelEffect(
                MapleItemInformationProvider.getInstance().getItemEffect(itemId), false, -1);
    }

    public void cancelEffect(MapleStatEffect effect, boolean overwrite,
            long startTime) {
        List<MapleBuffStat> buffstats;
        if (!overwrite) {
            buffstats = getBuffStats(effect, startTime);
        } else {
            List<Pair<MapleBuffStat, Integer>> statups = effect.getStatups();
            buffstats = new ArrayList<MapleBuffStat>(statups.size());
            for (Pair<MapleBuffStat, Integer> statup : statups) {
                buffstats.add(statup.getLeft());
            }
        }
        deregisterBuffStats(buffstats);
        if (effect.isMagicDoor()) {
            if (!getDoors().isEmpty()) {
                MapleDoor door = getDoors().iterator().next();
                for (MapleCharacter chr : door.getTarget().getCharacters()) {
                    door.sendDestroyData(chr.client);
                }
                for (MapleCharacter chr : door.getTown().getCharacters()) {
                    door.sendDestroyData(chr.client);
                }
                for (MapleDoor destroyDoor : getDoors()) {
                    door.getTarget().removeMapObject(destroyDoor);
                    door.getTown().removeMapObject(destroyDoor);
                }
                clearDoors();
                silentPartyUpdate();
            }
        }
        if (effect.getSourceId() == Spearman.HYPER_BODY
                || effect.getSourceId() == GM.HYPER_BODY
                || effect.getSourceId() == SuperGM.HYPER_BODY) {
            List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(
                    4);
            statup.add(new Pair<MapleStat, Integer>(MapleStat.HP, Math.min(hp,
                    maxhp)));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.MP, Math.min(mp,
                    maxmp)));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, maxhp));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, maxmp));
            client.announce(MaplePacketCreator.updatePlayerStats(statup));
        }
        if (effect.isMonsterRiding()) {
            if (effect.getSourceId() != Corsair.BATTLE_SHIP) {
                this.getMount().cancelSchedule();
                this.getMount().setActive(false);
            }
        }
        if (!overwrite) {
            cancelPlayerBuffs(buffstats);
        }
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat) {
        cancelEffect(effects.get(stat).effect, false, -1);
    }

    public void toggleHide(boolean login) {
        if (isGM()) {
            if (isHidden()) {
                this.hidden = false;
                announce(MaplePacketCreator.getGMEffect(0x10, (byte) 0));
                getMap().broadcastNONGMMessage(this, MaplePacketCreator.spawnPlayerMapobject(this, false), false);
                updatePartyMemberHP();
            } else {
                this.hidden = true;
                announce(MaplePacketCreator.getGMEffect(0x10, (byte) 1));
                if (!login) {
                    getMap().broadcastNONGMMessage(this, MaplePacketCreator.removePlayerFromMap(getId()), false);
                }
            }
            announce(MaplePacketCreator.enableActions());
        }
    }

    private void cancelFullnessSchedule(int petSlot) {
        switch (petSlot) {
            case 0:
                if (fullnessSchedule != null) {
                    fullnessSchedule.cancel(false);
                }
                break;
            case 1:
                if (fullnessSchedule_1 != null) {
                    fullnessSchedule_1.cancel(false);
                }
                break;
            case 2:
                if (fullnessSchedule_2 != null) {
                    fullnessSchedule_2.cancel(false);
                }
                break;
        }
    }

    public void cancelMagicDoor() {
        for (MapleBuffStatValueHolder mbsvh : new ArrayList<MapleBuffStatValueHolder>(
                effects.values())) {
            if (mbsvh.effect.isMagicDoor()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void cancelMapTimeLimitTask() {
        if (mapTimeLimitTask != null) {
            mapTimeLimitTask.cancel(false);
        }
    }

    private void cancelPlayerBuffs(List<MapleBuffStat> buffstats) {
        if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
            recalcLocalStats();
            enforceMaxHpMp();
            client.announce(MaplePacketCreator.cancelBuff(buffstats));
            if (buffstats.size() > 0) {
                getMap().broadcastMessage(
                        this,
                        MaplePacketCreator.cancelForeignBuff(getId(), buffstats), false);
            }
        }
    }

    public static boolean canCreateChar(String name) {
        if (name.length() < 4 || name.length() > 12) {
            return false;
        }

        if (isInUse(name)) {
            return false;
        }

        return getIdByName(name) < 0
                && !name.toLowerCase().contains("gm")
                && Pattern.compile("[a-zA-Z0-9_-]{3,12}").matcher(name).matches();
    }

    public boolean canDoor() {
        return canDoor;
    }

    public FameStatus canGiveFame(MapleCharacter from) {
        if (gmLevel.atleast(MapleGroup.INTERN)) {
            return FameStatus.OK;
        } else if (lastfametime >= System.currentTimeMillis() - 3600000 * 24) {
            return FameStatus.NOT_TODAY;
        } else if (lastmonthfameids.contains(Integer.valueOf(from.getId()))) {
            return FameStatus.NOT_THIS_MONTH;
        } else {
            return FameStatus.OK;
        }
    }

    public void changeCI(int type) {
        this.ci = type;
    }

    public void changeJob(MapleJob newJob) {
        this.job = newJob;
        this.remainingSp++;
        if (newJob.getId() % 10 == 2) {
            this.remainingSp += 2;
        }
        if (newJob.getId() % 10 > 1) {
            this.remainingAp += 5;
        }
        int job_ = job.getId() % 1000; // lame temp "fix"
        if (job_ == 100) {
            maxhp += Randomizer.rand(200, 250);
        } else if (job_ == 200) {
            maxmp += Randomizer.rand(100, 150);
        } else if (job_ % 100 == 0) {
            maxhp += Randomizer.rand(100, 150);
            maxhp += Randomizer.rand(25, 50);
        } else if (job_ > 0 && job_ < 200) {
            maxhp += Randomizer.rand(300, 350);
        } else if (job_ < 300) {
            maxmp += Randomizer.rand(450, 500);
        } // handle KoC here (undone)
        else if (job_ > 0 && job_ != 1000) {
            maxhp += Randomizer.rand(300, 350);
            maxmp += Randomizer.rand(150, 200);
        }
        if (maxhp >= 30000) {
            maxhp = 30000;
        }
        if (maxmp >= 30000) {
            maxmp = 30000;
        }
        if (!isGM()) {
            for (byte i = 1; i < 5; i++) {
                gainSlots(i, 4, true);
            }
        }
        List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(
                5);
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, Integer.valueOf(maxhp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, Integer.valueOf(maxmp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP,
                remainingAp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLESP,
                remainingSp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.JOB, Integer.valueOf(job.getId())));
        recalcLocalStats();
        client.announce(MaplePacketCreator.updatePlayerStats(statup));
        silentPartyUpdate();
        if (this.guildid > 0) {
            getGuild().broadcast(
                    MaplePacketCreator.jobMessage(0, job.getId(), name),
                    this.getId());
        }
        guildUpdate();
        getMap().broadcastMessage(this,
                MaplePacketCreator.showForeignEffect(getId(), 8), false);
    }

    public void changeKeybinding(int key, MapleKeyBinding keybinding) {
        if (keybinding.getType() != 0) {
            keymap.put(Integer.valueOf(key), keybinding);
        } else {
            keymap.remove(Integer.valueOf(key));
        }
    }

    public void changeMap(int map) {
        changeMap(map, 0);
    }

    public void changeMap(int map, int portal) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);
        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, String portal) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);
        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, MaplePortal portal) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);
        changeMap(warpMap, portal);
    }

    public void changeMap(MapleMap to) {
        changeMap(to, to.getPortal(0));
    }

    public void changeMap(final MapleMap to, final MaplePortal pto) {
        if (to.getId() == 100000200 || to.getId() == 211000100
                || to.getId() == 220000300) {
            changeMapInternal(to, pto.getPosition(),
                    MaplePacketCreator.getWarpToMap(to, pto.getId() - 2, this));
        } else {
            changeMapInternal(to, pto.getPosition(),
                    MaplePacketCreator.getWarpToMap(to, pto.getId(), this));
        }
    }

    public void changeMap(final MapleMap to, final Point pos) {
        changeMapInternal(to, pos,
                MaplePacketCreator.getWarpToMap(to, 0x80, this));
    }

    public void changeMapBanish(int mapid, String portal, String msg) {
        message(5, msg);
        MapleMap map_ = client.getChannelServer().getMapFactory().getMap(mapid);
        changeMap(map_, map_.getPortal(portal));
    }

    private void changeMapInternal(final MapleMap to, final Point pos,
            MaplePacket warpPacket) {
        warpPacket.setOnSend(new Runnable() {

            @Override
            public void run() {
                map.removePlayer(MapleCharacter.this);
                if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
                    map = to;
                    setPosition(pos);
                    map.addPlayer(MapleCharacter.this);
                    if (party != null) {
                        mpc.setMapId(to.getId());
                        silentPartyUpdate();
                        client.announce(MaplePacketCreator.updateParty(
                                client.getChannel(), party,
                                PartyOperation.SILENT_UPDATE, null));
                        updatePartyMemberHP();
                    }
                    if (getMap().getHPDec() > 0) {
                        hpDecreaseTask = TimerManager.getInstance().schedule(
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        doHurtHp();
                                    }
                                }, 10000);
                    }
                }
            }
        });
        client.announce(warpPacket);
    }

    public void changePage(int page) {
        this.currentPage = page;
    }

    public void changeSkillLevel(ISkill skill, byte newLevel,
            int newMasterlevel, long expiration) {
        if (newLevel > -1) {
            skills.put(skill, new SkillEntry(newLevel, newMasterlevel,
                    expiration));
            this.client.announce(MaplePacketCreator.updateSkill(skill.getId(),
                    newLevel, newMasterlevel, expiration));
        } else {
            skills.remove(skill);
            this.client.announce(MaplePacketCreator.updateSkill(skill.getId(),
                    newLevel, newMasterlevel, -1)); // Shouldn't use expiration
            // anymore :)
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM skills WHERE skillId = ? AND characterId = ?");
                ps.setInt(1, skill.getId());
                ps.setInt(2, id);
                ps.execute();
                ps.close();
            } catch (SQLException ex) {
                System.out.print("Error deleting skill: " + ex);
            }
        }
    }

    public void changeTab(int tab) {
        this.currentTab = tab;
    }

    public void changeType(int type) {
        this.currentType = type;
    }

    public void checkBerserk() {
        if (BerserkSchedule != null) {
            BerserkSchedule.cancel(false);
        }
        final MapleCharacter chr = this;
        if (job.equals(MapleJob.Dark_Knight)) {
            ISkill BerserkX = SkillFactory.getSkill(DarkKnight.BERSERK);
            final int skilllevel = getSkillLevel(BerserkX);
            if (skilllevel > 0) {
                Berserk = chr.getHp() * 100 / chr.getMaxHp() < BerserkX.getEffect(skilllevel).getX();
                BerserkSchedule = TimerManager.getInstance().register(
                        new Runnable() {

                            @Override
                            public void run() {
                                client.announce(MaplePacketCreator.showOwnBerserk(skilllevel, Berserk));
                                getMap().broadcastMessage(
                                        MapleCharacter.this,
                                        MaplePacketCreator.showBerserk(getId(),
                                        skilllevel, Berserk), false);
                            }
                        }, 5000, 3000);
            }
        }
    }

    public void checkMessenger() {
        if (messenger != null && messengerposition < 4
                && messengerposition > -1) {
            World world = Server.getInstance().getWorld(worldid);
            world.silentJoinMessenger(messenger.getId(),
                    new MapleMessengerCharacter(this, messengerposition),
                    messengerposition);
            world.updateMessenger(getMessenger().getId(), name,
                    client.getChannel());
        }
    }

    public void checkMonsterAggro(MapleMonster monster) {
        if (!monster.isControllerHasAggro()) {
            if (monster.getController() == this) {
                monster.setControllerHasAggro(true);
            } else {
                monster.switchController(this, true);
            }
        }
    }

    public void clearDoors() {
        doors.clear();
    }

    public void clearSavedLocation(SavedLocationType type) {
        savedLocations[type.ordinal()] = null;
    }

    public void controlMonster(MapleMonster monster, boolean aggro) {
        monster.setController(this);
        controlled.add(monster);
        client.announce(MaplePacketCreator.controlMonster(monster, false, aggro));
    }

    public int countItem(int itemid) {
        return inventory[MapleItemInformationProvider.getInstance().getInventoryType(itemid).ordinal()].countById(itemid);
    }

    public void decreaseBattleshipHp(int decrease) {
        this.battleshipHp -= decrease;
        if (battleshipHp <= 0) {
            this.battleshipHp = 0;
            ISkill battleship = SkillFactory.getSkill(Corsair.BATTLE_SHIP);
            int cooldown = battleship.getEffect(getSkillLevel(battleship)).getCooldown();
            announce(MaplePacketCreator.skillCooldown(Corsair.BATTLE_SHIP,
                    cooldown));
            addCooldown(
                    Corsair.BATTLE_SHIP,
                    System.currentTimeMillis(),
                    cooldown,
                    TimerManager.getInstance().schedule(
                    new CancelCooldownAction(this,
                    Corsair.BATTLE_SHIP),
                    cooldown * 1000));
            removeCooldown(5221999);
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        } else {
            announce(MaplePacketCreator.skillCooldown(5221999,
                    battleshipHp / 10)); // :D
            addCooldown(5221999, 0, battleshipHp, null);
        }
    }

    public void decreaseReports() {
        this.possibleReports--;
    }

    public void deleteGuild(int guildId) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildId = 0, guildRank = 5 WHERE guildId = ?");
            ps.setInt(1, guildId);
            ps.execute();
            ps.close();
            ps = con.prepareStatement("DELETE FROM guilds WHERE guildId = ?");
            ps.setInt(1, id);
            ps.execute();
            ps.close();
        } catch (SQLException ex) {
            System.out.print("Error deleting guild: " + ex);
        }
    }

    private void deleteWhereCharacterId(Connection con, String sql)
            throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    private void deregisterBuffStats(List<MapleBuffStat> stats) {
        synchronized (stats) {
            List<MapleBuffStatValueHolder> effectsToCancel = new ArrayList<MapleBuffStatValueHolder>(
                    stats.size());
            for (MapleBuffStat stat : stats) {
                MapleBuffStatValueHolder mbsvh = effects.get(stat);
                if (mbsvh != null) {
                    effects.remove(stat);
                    boolean addMbsvh = true;
                    for (MapleBuffStatValueHolder contained : effectsToCancel) {
                        if (mbsvh.startTime == contained.startTime
                                && contained.effect == mbsvh.effect) {
                            addMbsvh = false;
                        }
                    }
                    if (addMbsvh) {
                        effectsToCancel.add(mbsvh);
                    }
                    if (stat == MapleBuffStat.SUMMON
                            || stat == MapleBuffStat.PUPPET) {
                        int summonId = mbsvh.effect.getSourceId();
                        MapleSummon summon = summons.get(summonId);
                        if (summon != null) {
                            getMap().broadcastMessage(
                                    MaplePacketCreator.removeSummon(summon,
                                    true), summon.getPosition());
                            getMap().removeMapObject(summon);
                            removeVisibleMapObject(summon);
                            summons.remove(summonId);
                        }
                        if (summon.getSkill() == DarkKnight.BEHOLDER) {
                            if (beholderHealingSchedule != null) {
                                beholderHealingSchedule.cancel(false);
                                beholderHealingSchedule = null;
                            }
                            if (beholderBuffSchedule != null) {
                                beholderBuffSchedule.cancel(false);
                                beholderBuffSchedule = null;
                            }
                        }
                    } else if (stat == MapleBuffStat.DRAGONBLOOD) {
                        dragonBloodSchedule.cancel(false);
                        dragonBloodSchedule = null;
                    }
                }
            }
            for (MapleBuffStatValueHolder cancelEffectCancelTasks : effectsToCancel) {
                if (getBuffStats(cancelEffectCancelTasks.effect, cancelEffectCancelTasks.startTime).isEmpty()) {
                    if (cancelEffectCancelTasks.schedule != null) {
                        cancelEffectCancelTasks.schedule.cancel(false);
                    }
                }
            }
        }
    }

    public void disableDoor() {
        canDoor = false;
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                canDoor = true;
            }
        }, 5000);
    }

    public void disbandGuild() {
        if (guildid < 1 || guildrank != 1) {
            return;
        }
        try {
            Server.getInstance().disbandGuild(guildid);
        } catch (Exception e) {
        }
    }

    public void dispel() {
        for (MapleBuffStatValueHolder mbsvh : new ArrayList<MapleBuffStatValueHolder>(
                effects.values())) {
            if (mbsvh.effect.isSkill()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public final List<PlayerDiseaseValueHolder> getAllDiseases() {
        final List<PlayerDiseaseValueHolder> ret = new ArrayList<PlayerDiseaseValueHolder>(
                5);

        DiseaseValueHolder vh;
        for (Entry<MapleDisease, DiseaseValueHolder> disease : diseases.entrySet()) {
            vh = disease.getValue();
            ret.add(new PlayerDiseaseValueHolder(disease.getKey(),
                    vh.startTime, vh.length));
        }
        return ret;
    }

    public final boolean hasDisease(final MapleDisease dis) {
        for (final MapleDisease disease : diseases.keySet()) {
            if (disease == dis) {
                return true;
            }
        }
        return false;
    }

    public void giveDebuff(final MapleDisease disease, MobSkill skill) {
        final List<Pair<MapleDisease, Integer>> debuff = Collections.singletonList(new Pair<MapleDisease, Integer>(disease, Integer.valueOf(skill.getX())));

        if (!hasDisease(disease) && diseases.size() < 2) {
            if (!(disease == MapleDisease.SEDUCE || disease == MapleDisease.STUN)) {
                if (isActiveBuffedValue(2321005)) {
                    return;
                }
            }
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    dispelDebuff(disease);
                }
            }, skill.getDuration());

            diseases.put(
                    disease,
                    new DiseaseValueHolder(System.currentTimeMillis(), skill.getDuration()));
            client.getSession().write(
                    MaplePacketCreator.giveDebuff(debuff, skill));
            map.broadcastMessage(this,
                    MaplePacketCreator.giveForeignDebuff(id, debuff, skill),
                    false);
        }
    }

    public void dispelDebuff(MapleDisease debuff) {
        if (hasDisease(debuff)) {
            long mask = debuff.getValue();
            announce(MaplePacketCreator.cancelDebuff(mask));
            map.broadcastMessage(this,
                    MaplePacketCreator.cancelForeignDebuff(id, mask), false);

            diseases.remove(debuff);
        }
    }

    public void dispelDebuffs() {
        dispelDebuff(MapleDisease.CURSE);
        dispelDebuff(MapleDisease.DARKNESS);
        dispelDebuff(MapleDisease.POISON);
        dispelDebuff(MapleDisease.SEAL);
        dispelDebuff(MapleDisease.WEAKEN);
    }

    public void cancelAllDebuffs() {
        diseases.clear();
    }

    public void dispelSkill(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(
                effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (skillid == 0) {
                if (mbsvh.effect.isSkill()
                        && (mbsvh.effect.getSourceId() % 10000000 == 1004 || dispelSkills(mbsvh.effect.getSourceId()))) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            } else if (mbsvh.effect.isSkill()
                    && mbsvh.effect.getSourceId() == skillid) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    private boolean dispelSkills(int skillid) {
        switch (skillid) {
            case DarkKnight.BEHOLDER:
            case FPArchMage.ELQUINES:
            case ILArchMage.IFRIT:
            case Priest.SUMMON_DRAGON:
            case Bishop.BAHAMUT:
            case Ranger.PUPPET:
            case Ranger.SILVER_HAWK:
            case Sniper.PUPPET:
            case Sniper.GOLDEN_EAGLE:
            case Hermit.SHADOW_PARTNER:
                return true;
            default:
                return false;
        }
    }

    public void doHurtHp() {
        if (this.getInventory(MapleInventoryType.EQUIPPED).findById(
                getMap().getHPDecProtect()) != null) {
            return;
        }
        addHP(-getMap().getHPDec());
        hpDecreaseTask = TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                doHurtHp();
            }
        }, 10000);
    }

    public void message(String m) {
        message(5, m);
    }

    public void yellowMessage(String m) {
        announce(MaplePacketCreator.sendYellowTip(m));
    }

    public void message(int type, String message) {
        client.announce(MaplePacketCreator.serverNotice(type, message));
    }

    public String emblemCost() {
        return nf.format(MapleGuild.CHANGE_EMBLEM_COST);
    }

    public List<ScheduledFuture<?>> getTimers() {
        return timers;
    }

    private void enforceMaxHpMp() {
        List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>(
                2);
        if (getMp() > getCurrentMaxMp()) {
            setMp(getMp());
            stats.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(getMp())));
        }
        if (getHp() > getCurrentMaxHp()) {
            setHp(getHp());
            stats.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(getHp())));
        }
        if (stats.size() > 0) {
            client.announce(MaplePacketCreator.updatePlayerStats(stats));
        }
    }

    public void enteredScript(String script, int mapid) {
        if (!entered.containsKey(mapid)) {
            entered.put(mapid, script);
        }
    }

    public void equipChanged() {
        getMap().broadcastMessage(this,
                MaplePacketCreator.updateCharLook(this), false);
        recalcLocalStats();
        enforceMaxHpMp();
        // saveToDB(true);
        if (getMessenger() != null) {
            Server.getInstance().getWorld(worldid).updateMessenger(getMessenger(), getName(), getWorld(),
                    client.getChannel());
        }
    }

    public void cancelExpirationTask() {
        if (expiretask != null) {
            expiretask.cancel(false);
            expiretask = null;
        }
    }

    public void expirationTask() {
        if (expiretask != null) {
            expiretask.cancel(false);
            expiretask = null;
        }
        expiretask = TimerManager.getInstance().register(new Runnable() {

            @Override
            public void run() {
                long expiration, currenttime = System.currentTimeMillis();
                Set<ISkill> keys = getSkills().keySet();
                for (Iterator<ISkill> i = keys.iterator(); i.hasNext();) {
                    ISkill key = i.next();
                    SkillEntry skill = getSkills().get(key);
                    if (skill.expiration != -1
                            && skill.expiration < currenttime) {
                        changeSkillLevel(key, (byte) -1, 0, -1);
                    }
                }

                List<IItem> toberemove = new ArrayList<IItem>();
                for (MapleInventory inv : inventory) {
                    for (IItem item : inv.list()) {
                        expiration = item.getExpiration();
                        if (expiration != -1
                                && (expiration < currenttime)
                                && ((item.getFlag() & ItemConstants.LOCK) == ItemConstants.LOCK)) {
                            byte aids = item.getFlag();
                            aids &= ~(ItemConstants.LOCK);
                            item.setFlag(aids); // Probably need a check, else
                            // people can make expiring
                            // items into permanent items...
                            item.setExpiration(-1);
                            forceUpdateItem(inv.getType(), item); // TEST :3
                        } else if (expiration != -1 && expiration < currenttime) {
                            client.announce(MaplePacketCreator.itemExpired(item.getItemId()));
                            toberemove.add(item);
                        }
                    }
                    for (IItem item : toberemove) {
                        MapleInventoryManipulator.removeFromSlot(client,
                                inv.getType(), item.getPosition(),
                                item.getQuantity(), true);
                    }
                    toberemove.clear();
                }
                // announce(MaplePacketCreator.enableActions());
                // saveToDB(true);
            }
        }, 60000);
    }

    public enum FameStatus {

        OK, NOT_TODAY, NOT_THIS_MONTH
    }

    public void forceUpdateItem(MapleInventoryType type, IItem item) {
        client.announce(MaplePacketCreator.clearInventoryItem(type,
                item.getPosition(), false));
        client.announce(MaplePacketCreator.addInventorySlot(type, item, false));
    }

    public void gainGachaExp() {
        int expgain = 0;
        int currentgexp = gachaexp.get();
        if ((currentgexp + exp.get()) >= ExpTable.getExpNeededForLevel(level)) {
            expgain += ExpTable.getExpNeededForLevel(level) - exp.get();
            int nextneed = ExpTable.getExpNeededForLevel(level + 1);
            if ((currentgexp - expgain) >= nextneed) {
                expgain += nextneed;
            }
            this.gachaexp.set(currentgexp - expgain);
        } else {
            expgain = this.gachaexp.getAndSet(0);
        }
        gainExp(expgain, false, false);
        updateSingleStat(MapleStat.GACHAEXP, this.gachaexp.get());
    }

    public void gainGachaExp(int gain) {
        updateSingleStat(MapleStat.GACHAEXP, gachaexp.addAndGet(gain));
    }

    public void gainExp(int gain, boolean show, boolean inChat) {
        gainExp(gain, show, inChat, true);
    }

    public void gainExp(int gain, boolean show, boolean inChat, boolean white) {
        int equip = (gain / 10) * pendantExp;
        long total = (gain + equip) * getTotalExpRate();
        
        int expNeeded = ExpTable.getExpNeededForLevel(level) - this.exp.get(); // To next level
        while(total >= expNeeded && level < getMaxLevel()) {
            total -= expNeeded;
            updateSingleStat(MapleStat.EXP, exp.addAndGet(expNeeded));
            if(show)
                client.announce(MaplePacketCreator.getShowExpGain(expNeeded, equip, inChat, white));
            levelUp(expNeeded > total || level + 1 > getMaxLevel());
            expNeeded = ExpTable.getExpNeededForLevel(level) - this.exp.get(); // To next level
        }
        updateSingleStat(MapleStat.EXP, exp.addAndGet((int) total));
        if(show)
            client.announce(MaplePacketCreator.getShowExpGain((int) total, equip, inChat, white));
//        if ((long) this.exp.get() + (long) total > (long) Integer.MAX_VALUE) {
//            int gainFirst = ExpTable.getExpNeededForLevel(level)
//                    - this.exp.get();
//            total -= gainFirst + 1;
//            this.gainExp(gainFirst + 1, false, inChat, white);
//        }
//        updateSingleStat(MapleStat.EXP, this.exp.addAndGet((int)total));
//        if (show && gain != 0) {
//            client.announce(MaplePacketCreator.getShowExpGain(gain, equip,
//                    inChat, white));
//        }
//        while (level < ServerConstants.MAX_LEVEL && exp.get() >= ExpTable.getExpNeededForLevel(level)) {
//            levelUp(exp.get() >= ExpTable.getExpNeededForLevel(level + 1));
//        }
    }

    public void gainFame(int delta) {
        this.addFame(delta);
        this.updateSingleStat(MapleStat.FAME, this.fame);
    }

    public void gainMeso(int gain, boolean show) {
        gainMeso(gain, show, false, false);
    }

    public void gainMeso(int gain, boolean show, boolean enableActions,
            boolean inChat) {
        if (meso.get() + gain < 0) {
            client.announce(MaplePacketCreator.enableActions());
            return;
        }
        updateSingleStat(MapleStat.MESO, meso.addAndGet(gain), enableActions);
        if (show) {
            client.announce(MaplePacketCreator.getShowMesoGain(gain, inChat));
        }
    }

    public void genericGuildMessage(int code) {
        this.client.announce(MaplePacketCreator.genericGuildMessage((byte) code));
    }

    public int getAccountID() {
        return accountid;
    }

    public List<PlayerBuffValueHolder> getAllBuffs() {
        List<PlayerBuffValueHolder> ret = new ArrayList<PlayerBuffValueHolder>();
        for (MapleBuffStatValueHolder mbsvh : effects.values()) {
            ret.add(new PlayerBuffValueHolder(mbsvh.startTime, mbsvh.effect));
        }
        return ret;
    }

    public List<PlayerCoolDownValueHolder> getAllCooldowns() {
        List<PlayerCoolDownValueHolder> ret = new ArrayList<PlayerCoolDownValueHolder>();
        for (MapleCoolDownValueHolder mcdvh : coolDowns.values()) {
            ret.add(new PlayerCoolDownValueHolder(mcdvh.skillId,
                    mcdvh.startTime, mcdvh.length));
        }
        return ret;
    }

    public int getAllianceRank() {
        return this.allianceRank;
    }

    public int getAllowWarpToId() {
        return warpToId;
    }

    public static String getAriantRoomLeaderName(int room) {
        return ariantroomleader[room];
    }

    public static int getAriantSlotsRoom(int room) {
        return ariantroomslot[room];
    }

    public int getBattleshipHp() {
        return battleshipHp;
    }

    public BuddyList getBuddylist() {
        return buddylist;
    }

    @SuppressWarnings("CallToThreadDumpStack")
    public static Map<String, String> getCharacterFromDatabase(String name) {
        Map<String, String> character = new LinkedHashMap<String, String>();

        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                    "SELECT `id`, `accountId`, `name` FROM `characters` WHERE `name` = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }

            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                character.put(rs.getMetaData().getColumnLabel(i),
                        rs.getString(i));
            }

            rs.close();
            ps.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return character;
    }

    public static boolean isInUse(String name) {
        return getCharacterFromDatabase(name) != null;
    }

    public Long getBuffedStarttime(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return Long.valueOf(mbsvh.startTime);
    }

    public Integer getBuffedValue(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return Integer.valueOf(mbsvh.value);
    }

    public int getBuffSource(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null) {
            return -1;
        }
        return mbsvh.effect.getSourceId();
    }

    private List<MapleBuffStat> getBuffStats(MapleStatEffect effect,
            long startTime) {
        List<MapleBuffStat> stats = new ArrayList<MapleBuffStat>();
        for (Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : effects.entrySet()) {
            if (stateffect.getValue().effect.sameSource(effect)
                    && (startTime == -1 || startTime == stateffect.getValue().startTime)) {
                stats.add(stateffect.getKey());
            }
        }
        return stats;
    }

    public int getChair() {
        return chair;
    }

    public String getChalkboard() {
        return this.chalktext;
    }

    public MapleClient getClient() {
        return client;
    }

    public final List<MapleQuestStatus> getCompletedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
                ret.add(q);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    public Collection<MapleMonster> getControlledMonsters() {
        return Collections.unmodifiableCollection(controlled);
    }

    public List<MapleRing> getCrushRings() {
        Collections.sort(crushRings);
        return crushRings;
    }

    public int getCurrentCI() {
        return ci;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getCurrentMaxHp() {
        return localmaxhp;
    }

    public int getCurrentMaxMp() {
        return localmaxmp;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public int getCurrentType() {
        return currentType;
    }

    public int getDex() {
        return dex;
    }

    public int getDojoEnergy() {
        return dojoEnergy;
    }

    public boolean getDojoParty() {
        return dojoParty;
    }

    public int getDojoPoints() {
        return dojoPoints;
    }

    public int getDojoStage() {
        return dojoStage;
    }

    public List<MapleDoor> getDoors() {
        return new ArrayList<MapleDoor>(doors);
    }

    public int getTotalExpRate() {
        return (Server.getInstance().getWorld(worldid).getExpRate() + expRate) * expRateCards;
    }
    
    public int getExpRate() {
        return expRate;
    }
    
    public int getExpCards() {
        return expRateCards;
    }

    public int getTotalDropRate() {
        return (Server.getInstance().getWorld(worldid).getDropRate() + dropRate) * dropRateCards;
    }
    
    public int getDropRate() {
        return dropRate;
    }
    
    public int getDropCards() {
        return dropRateCards;
    }

    public int getTotalMesoRate() {
        return (Server.getInstance().getWorld(worldid).getMesoRate() + mesoRate) * mesoRateCards;
    }

    public int getMesoRate() {
        return mesoRate;
    }

    public int getMesoCards() {
        return mesoRateCards;
    }

    public int getEnergyBar() {
        return energybar;
    }

    public EventInstanceManager getEventInstance() {
        return eventInstance;
    }

    public ArrayList<Integer> getExcluded() {
        return excluded;
    }

    public int getExp() {
        return exp.get();
    }

    public int getGachaExp() {
        return gachaexp.get();
    }

    public int getFace() {
        return face;
    }

    public int getFame() {
        return fame;
    }

    public MapleFamily getFamily() {
        return family;
    }

    public void setFamily(MapleFamily f) {
        this.family = f;
    }

    public int getFamilyId() {
        return familyId;
    }

    public boolean getFinishedDojoTutorial() {
        return finishedDojoTutorial;
    }

    public List<MapleRing> getFriendshipRings() {
        Collections.sort(friendshipRings);
        return friendshipRings;
    }

    public int getGender() {
        return gender;
    }

    public boolean isMale() {
        return getGender() == 0;
    }

    public MapleGuild getGuild() {
        try {
            return Server.getInstance().getGuild(getGuildId(), null);
        } catch (Exception ex) {
            return null;
        }
    }

    public int getGuildId() {
        return guildid;
    }

    public int getGuildRank() {
        return guildrank;
    }

    public int getHair() {
        return hair;
    }

    public HiredMerchant getHiredMerchant() {
        return hiredMerchant;
    }

    public int getHp() {
        return hp;
    }

    public int getHpMpApUsed() {
        return hpMpApUsed;
    }

    public int getId() {
        return id;
    }

    public static int getIdByName(String name) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                    "SELECT id FROM characters WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            int id = rs.getInt("id");
            rs.close();
            ps.close();
            return id;
        } catch (Exception e) {
        }
        return -1;
    }

    public static String getNameById(int id) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                    "SELECT name FROM characters WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }
            String name = rs.getString("name");
            rs.close();
            ps.close();
            return name;
        } catch (Exception e) {
        }
        return null;
    }

    public int getInitialSpawnpoint() {
        return initialSpawnPoint;
    }

    public int getInt() {
        return int_;
    }

    public MapleInventory getInventory(MapleInventoryType type) {
        return inventory[type.ordinal()];
    }

    public int getItemEffect() {
        return itemEffect;
    }

    public int getItemQuantity(int itemid, boolean checkEquipped) {
        int possesed = inventory[MapleItemInformationProvider.getInstance().getInventoryType(itemid).ordinal()].countById(itemid);
        if (checkEquipped) {
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        return possesed;
    }

    public MapleJob getJob() {
        return job;
    }

    public int getJobRank() {
        return jobRank;
    }

    public int getJobRankMove() {
        return jobRankMove;
    }

    public int getJobType() {
        return job.getId() / 1000;
    }

    public Map<Integer, MapleKeyBinding> getKeymap() {
        return keymap;
    }

    public long getLastHealed() {
        return lastHealed;
    }

    public long getLastUsedCashItem() {
        return lastUsedCashItem;
    }

    public int getLevel() {
        return level;
    }

    public int getLuk() {
        return luk;
    }

    public int getFh() {
        if (getMap().getFootholds().findBelow(this.getPosition()) == null) {
            return 0;
        } else {
            return getMap().getFootholds().findBelow(this.getPosition()).getId();
        }
    }

    public MapleMap getMap() {
        return map;
    }

    public int getMapId() {
        if (map != null) {
            return map.getId();
        }
        return mapid;
    }
    
    public int getReturnMap() {
        return returnMap;
    }

    public int getMarkedMonster() {
        return markedMonster;
    }

    public MapleRing getMarriageRing() {
        return marriageRing;
    }

    public int getMarried() {
        return married;
    }

    public int getMasterLevel(ISkill skill) {
        if (skills.get(skill) == null) {
            return 0;
        }
        return skills.get(skill).masterlevel;
    }

    public int getMaxHp() {
        return maxhp;
    }

    public int getMaxLevel() {
        return ServerConstants.MAX_LEVEL;//isCygnus() ? 120 : 200;
    }

    public int getMaxMp() {
        return maxmp;
    }

    public int getMeso() {
        return meso.get();
    }

    public int getMerchantMeso() {
        return merchantmeso;
    }

    public int getMesosTraded() {
        return mesosTraded;
    }

    public int getMessengerPosition() {
        return messengerposition;
    }

    public MapleGuildCharacter getMGC() {
        return mgc;
    }

    public MaplePartyCharacter getMPC() {
        // if (mpc == null) mpc = new MaplePartyCharacter(this);
        return mpc;
    }

    public void setMPC(MaplePartyCharacter mpc) {
        this.mpc = mpc;
    }

    public MapleMiniGame getMiniGame() {
        return miniGame;
    }

    public int getMiniGamePoints(String type, boolean omok) {
        if (omok) {
            if (type.equals("wins")) {
                return omokwins;
            } else if (type.equals("losses")) {
                return omoklosses;
            } else {
                return omokties;
            }
        } else {
            if (type.equals("wins")) {
                return matchcardwins;
            } else if (type.equals("losses")) {
                return matchcardlosses;
            } else {
                return matchcardties;
            }
        }
    }

    public MonsterBook getMonsterBook() {
        return monsterbook;
    }

    public int getMonsterBookCover() {
        return bookCover;
    }

    public MapleMount getMount() {
        return maplemount;
    }

    public int getMp() {
        return mp;
    }

    public MapleMessenger getMessenger() {
        return messenger;
    }

    public String getName() {
        return name;
    }

    public int getNextEmptyPetIndex() {
        for (int i = 0; i < 3; i++) {
            if (pets[i] == null) {
                return i;
            }
        }
        return 3;
    }

    public int getNoPets() {
        int ret = 0;
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                ret++;
            }
        }
        return ret;
    }

    public int getNumControlledMonsters() {
        return controlled.size();
    }

    public MapleParty getParty() {
        return party;
    }

    public int getPartyId() {
        return (party != null ? party.getId() : -1);
    }

    public MaplePlayerShop getPlayerShop() {
        return playerShop;
    }

    public MaplePet[] getPets() {
        return pets;
    }

    public MaplePet getPet(int index) {
        return pets[index];
    }

    public byte getPetIndex(int petId) {
        for (byte i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == petId) {
                    return i;
                }
            }
        }
        return -1;
    }

    public byte getPetIndex(MaplePet pet) {
        for (byte i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == pet.getUniqueId()) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getPossibleReports() {
        return possibleReports;
    }

    public final byte getQuestStatus(final int quest) {
        for (final MapleQuestStatus q : quests.values()) {
            if (q.getQuest().getId() == quest) {
                return (byte) q.getStatus().getId();
            }
        }
        return 0;
    }

    public MapleQuestStatus getQuest(MapleQuest quest) {
        if (!quests.containsKey(quest)) {
            return new MapleQuestStatus(quest,
                    MapleQuestStatus.Status.NOT_STARTED);
        }
        return quests.get(quest);
    }

    public boolean needQuestItem(int questid, int itemid) {
        if (questid <= 0) {
            return true; // For non quest items :3
        }
        MapleQuest quest = MapleQuest.getInstance(questid);
        return getInventory(ItemConstants.getInventoryType(itemid)).countById(
                itemid) < quest.getItemAmountNeeded(itemid);
    }

    public int getRank() {
        return rank;
    }

    public int getRankMove() {
        return rankMove;
    }

    public int getRemainingAp() {
        return remainingAp;
    }

    public int getRemainingSp() {
        return remainingSp;
    }

    public int getSavedLocation(String type) {
        SavedLocation sl = savedLocations[SavedLocationType.fromString(type).ordinal()];
        if (sl == null) {
            return 102000000;
        }
        int m = sl.getMapId();
        if (!SavedLocationType.fromString(type).equals(
                SavedLocationType.WORLDTOUR)) {
            clearSavedLocation(SavedLocationType.fromString(type));
        }
        return m;
    }

    public String getSearch() {
        return search;
    }

    public MapleShop getShop() {
        return shop;
    }

    public Map<ISkill, SkillEntry> getSkills() {
        return Collections.unmodifiableMap(skills);
    }

    public int getSkillLevel(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null) {
            return 0;
        }
        return ret.skillevel;
    }

    public byte getSkillLevel(ISkill skill) {
        if (skills.get(skill) == null) {
            return 0;
        }
        return skills.get(skill).skillevel;
    }

    public long getSkillExpiration(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null) {
            return -1;
        }
        return ret.expiration;
    }

    public long getSkillExpiration(ISkill skill) {
        if (skills.get(skill) == null) {
            return -1;
        }
        return skills.get(skill).expiration;
    }

    public MapleSkinColor getSkinColor() {
        return skinColor;
    }

    public int getSlot() {
        return slots;
    }

    public final List<MapleQuestStatus> getStartedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
                ret.add(q);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    public final int getStartedQuestsSize() {
        int i = 0;
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
                if (q.getQuest().getInfoNumber() > 0) {
                    i++;
                }
                i++;
            }
        }
        return i;
    }

    public MapleStatEffect getStatForBuff(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.effect;
    }

    public MapleStorage getStorage() {
        return storage;
    }

    public int getStr() {
        return str;
    }

    public Map<Integer, MapleSummon> getSummons() {
        return summons;
    }

    public int getTotalLuk() {
        return localluk;
    }

    public int getTotalMagic() {
        return magic;
    }

    public int getTotalWatk() {
        return watk;
    }

    public MapleTrade getTrade() {
        return trade;
    }

    public int getVanquisherKills() {
        return vanquisherKills;
    }

    public int getVanquisherStage() {
        return vanquisherStage;
    }

    public Collection<MapleMapObject> getVisibleMapObjects() {
        return Collections.unmodifiableCollection(visibleMapObjects);
    }

    public byte getWorld() {
        return worldid;
    }

    public void giveCoolDowns(final int skillid, long starttime, long length) {
        if (skillid == 5221999) {
            this.battleshipHp = (int) length;
            addCooldown(skillid, 0, length, null);
        } else {
            int time = (int) ((length + starttime) - System.currentTimeMillis());
            addCooldown(
                    skillid,
                    System.currentTimeMillis(),
                    time,
                    TimerManager.getInstance().schedule(
                    new CancelCooldownAction(this, skillid), time));
        }
    }

    public int gmLevel() {
        return gmLevel.getId();
    }

    public String guildCost() {
        return nf.format(MapleGuild.CREATE_GUILD_COST);
    }

    private void guildUpdate() {
        if (this.guildid < 1) {
            return;
        }
        mgc.setLevel(level);
        mgc.setJobId(job.getId());
        try {
            Server.getInstance().memberLevelJobUpdate(this.mgc);
            int allianceId = getGuild().getAllianceId();
            if (allianceId > 0) {
                Server.getInstance().allianceMessage(allianceId,
                        MaplePacketCreator.updateAllianceJobLevel(this),
                        getId(), -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleEnergyChargeGain() { // to get here energychargelevel has
        // to be > 0
        ISkill energycharge = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.ENERGY_CHARGE) : SkillFactory.getSkill(Marauder.ENERGY_CHARGE);
        MapleStatEffect ceffect = null;
        ceffect = energycharge.getEffect(getSkillLevel(energycharge));
        TimerManager tMan = TimerManager.getInstance();
        if (energybar < 10000) {
            energybar += 102;
            if (energybar > 10000) {
                energybar = 10000;
            }
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(
                    MapleBuffStat.ENERGY_CHARGE, energybar));
            setBuffedValue(MapleBuffStat.ENERGY_CHARGE, energybar);
            client.announce(MaplePacketCreator.giveBuff(energybar, 0, stat));
            client.announce(MaplePacketCreator.showOwnBuffEffect(
                    energycharge.getId(), 2));
            getMap().broadcastMessage(
                    this,
                    MaplePacketCreator.showBuffeffect(id, energycharge.getId(),
                    2));
            getMap().broadcastMessage(this,
                    MaplePacketCreator.giveForeignBuff(energybar, stat));
        }
        if (energybar >= 10000 && energybar < 11000) {
            energybar = 15000;
            final MapleCharacter chr = this;
            tMan.schedule(new Runnable() {

                @Override
                public void run() {
                    energybar = 0;
                    List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(
                            MapleBuffStat.ENERGY_CHARGE, energybar));
                    setBuffedValue(MapleBuffStat.ENERGY_CHARGE, energybar);
                    client.announce(MaplePacketCreator.giveBuff(energybar, 0,
                            stat));
                    getMap().broadcastMessage(chr,
                            MaplePacketCreator.giveForeignBuff(energybar, stat));
                }
            }, ceffect.getDuration());
        }
    }

    public void handleOrbconsume() {
        int skillid = isCygnus() ? DawnWarrior.COMBO : Crusader.COMBO;
        ISkill combo = SkillFactory.getSkill(skillid);
        List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(
                MapleBuffStat.COMBO, 1));
        setBuffedValue(MapleBuffStat.COMBO, 1);
        client.announce(MaplePacketCreator.giveBuff(
                skillid,
                combo.getEffect(getSkillLevel(combo)).getDuration()
                + (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis())), stat));
        getMap().broadcastMessage(this,
                MaplePacketCreator.giveForeignBuff(getId(), stat), false);
    }

    public boolean hasEntered(String script) {
        for (int mapId : entered.keySet()) {
            if (entered.get(mapId).equals(script)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEntered(String script, int mapId) {
        if (entered.containsKey(mapId)) {
            if (entered.get(mapId).equals(script)) {
                return true;
            }
        }
        return false;
    }

    public void hasGivenFame(MapleCharacter to) {
        lastfametime = System.currentTimeMillis();
        lastmonthfameids.add(Integer.valueOf(to.getId()));
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                    "INSERT INTO fameLog (characterId, characterIdTo) VALUES (?, ?)");
            ps.setInt(1, getId());
            ps.setInt(2, to.getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
        }
    }

    public boolean hasMerchant() {
        return hasMerchant;
    }

    public boolean haveItem(int itemid) {
        return getItemQuantity(itemid, false) > 0;
    }

    public void increaseGuildCapacity() { // hopefully nothing is null
        if (getMeso() < getGuild().getIncreaseGuildCost(
                getGuild().getCapacity())) {
            message(1, "You don't have enough mesos.");
            return;
        }
        Server.getInstance().increaseGuildCapacity(guildid);
        gainMeso(-getGuild().getIncreaseGuildCost(getGuild().getCapacity()),
                true, false, false);
    }

    public boolean isActiveBuffedValue(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(
                effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                return true;
            }
        }
        return false;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public boolean isBuffFrom(MapleBuffStat stat, ISkill skill) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null) {
            return false;
        }
        return mbsvh.effect.isSkill()
                && mbsvh.effect.getSourceId() == skill.getId();
    }

    public boolean isCygnus() {
        return getJobType() == 1;
    }

    public boolean isAran() {
        return getJob().getId() >= 2000 && getJob().getId() <= 2112;
    }

    public boolean isBeginnerJob() {
        return (getJob().getId() == 0 || getJob().getId() == 1000 || getJob().getId() == 2000) && getLevel() < 11;
    }

    public boolean isGM() {
        return gmLevel() >= MapleGroup.INTERN.getId();
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isMapObjectVisible(MapleMapObject mo) {
        return visibleMapObjects.contains(mo);
    }

    public boolean isPartyLeader() {
        return party.getLeader() == party.getMemberById(getId());
    }

    public void leaveMap() {
        controlled.clear();
        visibleMapObjects.clear();
        if (chair != 0) {
            chair = 0;
        }
        if (hpDecreaseTask != null) {
            hpDecreaseTask.cancel(false);
        }
    }

//    public void levelUp() {
//        levelUp(false);
//    }
    
    public void levelUp(boolean update) {
        ISkill improvingMaxHP = null;
        ISkill improvingMaxMP = null;
        int improvingMaxHPLevel = 0;
        int improvingMaxMPLevel = 0;

        if (isBeginnerJob()) {
            remainingAp = 0;
            if (getLevel() < 6) {
                str += 5;
            } else {
                str += 4;
                dex += 1;
            }
        } else {
//            remainingAp += 5;
//            if (isCygnus() && level < 70) {
//                remainingAp++;
//            }
            //autoap
            int maxStat = ServerConstants.MAX_STAT;
            int newRemainingAp = remainingAp, newStr = 0, newDex = 0, newInt = 0, newLuk = 0;
            if (isAutoAp()) {
                if (getAutoAp() == 1) {
                    newStr = getStr();
                    if (newStr + apPerLevel <= maxStat) {
                        newStr += apPerLevel;
                    } else {
                        newRemainingAp += apPerLevel - (maxStat - newStr);
                        newStr = maxStat;
                        removeAutoAp();
                        message("Your str has been maxed. Auto-ap disabled.");
                    }
                    this.str = newStr;
                    this.updateSingleStat(MapleStat.STR, newStr);
                } else if (getAutoAp() == 2) {
                    newDex = getDex();
                    if (newDex + apPerLevel <= maxStat) {
                        newDex += apPerLevel;
                    } else {
                        newRemainingAp += apPerLevel - (maxStat - newDex);
                        newDex = maxStat;
                        removeAutoAp();
                        message("Your dex has been maxed. Auto-ap disabled.");
                    }
                    this.dex = newDex;
                    this.updateSingleStat(MapleStat.DEX, newDex);
                } else if (getAutoAp() == 3) {
                    newInt = getInt();
                    if (newInt + apPerLevel <= maxStat) {
                        newInt += apPerLevel;
                    } else {
                        newRemainingAp += apPerLevel - (maxStat - newInt);
                        newInt = maxStat;
                        removeAutoAp();
                        message("Your int has been maxed. Auto-ap disabled.");
                    }
                    this.int_ = newInt;
                    this.updateSingleStat(MapleStat.INT, newInt);
                } else if (getAutoAp() == 4) {
                    newLuk = getLuk();
                    if (newLuk + apPerLevel <= maxStat) {
                        newLuk += apPerLevel;
                    } else {
                        newRemainingAp += apPerLevel - (maxStat - newLuk);
                        newLuk = maxStat;
                        removeAutoAp();
                        message("Your luk has been maxed. Auto-ap disabled.");
                    }
                    this.luk = newLuk;
                    this.updateSingleStat(MapleStat.LUK, newLuk);
                } else if (getAutoAp() == 5) {
                    storedAp += apPerLevel;
                } else {
                    newRemainingAp += apPerLevel;
                }
            } else {
                newRemainingAp += apPerLevel;
            }
            if (newRemainingAp > maxStat) {
                int apToLeave = 1000;
                this.storedAp += newRemainingAp - apToLeave;
                message("You have maxed how much unused ap you can hold at a time. " + (newRemainingAp - apToLeave) + " ap has been stored for a total of " + this.storedAp + " ap in storage.");
                message("To manage your stored ap talk to Cody in Henesys.");
                message("To rid of this message type '@autoap store' or use the ap on a stat before it maxes.");
                newRemainingAp = apToLeave;
            }
            //autoap end
            remainingAp = newRemainingAp;
        }
        if (job == MapleJob.Aventurer || job == MapleJob.Noblesse
                || job == MapleJob.Legend) {
            maxhp += Randomizer.rand(12, 16);
            maxmp += Randomizer.rand(10, 12);
        } else if (job.isA(MapleJob.Warrior) || job.isA(MapleJob.Dawn_Warrior_1)) {
            improvingMaxHP = isCygnus() ? SkillFactory.getSkill(DawnWarrior.MAX_HP_INCREASE) : SkillFactory.getSkill(Swordsman.IMPROVED_MAX_HP_INCREASE);
            if (job.isA(MapleJob.Crusader)) {
                improvingMaxMP = SkillFactory.getSkill(1210000);
            } else if (job.isA(MapleJob.Dawn_Warrior_2)) {
                improvingMaxMP = SkillFactory.getSkill(11110000);
            }
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += Randomizer.rand(24, 28);
            maxmp += Randomizer.rand(4, 6);
        } else if (job.isA(MapleJob.Magician) || job.isA(MapleJob.Blaze_Wizard_1)) {
            improvingMaxMP = isCygnus() ? SkillFactory.getSkill(BlazeWizard.INCREASING_MAX_MP) : SkillFactory.getSkill(Magician.IMPROVED_MAX_MP_INCREASE);
            improvingMaxMPLevel = getSkillLevel(improvingMaxMP);
            maxhp += Randomizer.rand(10, 14);
            maxmp += Randomizer.rand(22, 24);
        } else if (job.isA(MapleJob.Bowman) || job.isA(MapleJob.Thief)
                || (job.getId() > 1299 && job.getId() < 1500)) {
            maxhp += Randomizer.rand(20, 24);
            maxmp += Randomizer.rand(14, 16);
        } else if (job.isA(MapleJob.GM)) {
            maxhp = 30000;
            maxmp = 30000;
        } else if (job.isA(MapleJob.Pirate)
                || job.isA(MapleJob.Thunder_Breaker_1)) {
            improvingMaxHP = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.IMPROVE_MAX_HP) : SkillFactory.getSkill(5100000);
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += Randomizer.rand(22, 28);
            maxmp += Randomizer.rand(18, 23);
        } else if (job.isA(MapleJob.Aran_1)) {
            maxhp += Randomizer.rand(44, 48);
            int aids = Randomizer.rand(4, 8);
            maxmp += aids + Math.floor(aids * 0.1);
        }
        if (improvingMaxHPLevel > 0
                && (job.isA(MapleJob.Warrior) || job.isA(MapleJob.Pirate) || job.isA(MapleJob.Dawn_Warrior_1))) {
            maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
        }
        if (improvingMaxMPLevel > 0
                && (job.isA(MapleJob.Magician) || job.isA(MapleJob.Crusader) || job.isA(MapleJob.Blaze_Wizard_1))) {
            maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getX();
        }
        maxmp += localint_ / 10;
        // Take Exp
        exp.addAndGet(-ExpTable.getExpNeededForLevel(level));
        if (exp.get() < 0) {
            exp.set(0);
        }
        level++;
        if (level >= getMaxLevel()) {
            exp.set(0);
        }
        maxhp = Math.min(30000, maxhp);
        maxmp = Math.min(30000, maxmp);
        if (level == 200) {
            exp.set(0);
        }
        hp = maxhp;
        mp = maxmp;
        recalcLocalStats();
        if (job.getId() % 1000 > 0) {
            remainingSp += 3;
        }

        if (update) {
            List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(
                    10);
            statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP,
                    remainingAp));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.HP, localmaxhp));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.MP, localmaxmp));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.EXP, exp.get()));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.LEVEL, level));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, maxhp));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, maxmp));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.STR, str));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.DEX, dex));
            if (job.getId() % 1000 > 0) {
                statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLESP,
                        remainingSp));
            }
            client.announce(MaplePacketCreator.updatePlayerStats(statup));
            getMap().broadcastMessage(this,
                    MaplePacketCreator.showForeignEffect(getId(), 0), false);
            if (this.guildid > 0) {
                getGuild().broadcast(
                        MaplePacketCreator.levelUpMessage(2, level, name),
                        this.getId());
            }
        }
        recalcLocalStats();
        setMPC(new MaplePartyCharacter(this));
        silentPartyUpdate();
        guildUpdate();
        if (update) {
            saveToDB(true);
        }
    }

    public static MapleCharacter loadCharFromDB(int charid, MapleClient client,
            boolean channelserver) throws SQLException {
        try {
            MapleCharacter ret = new MapleCharacter();
            ret.client = client;
            ret.id = charid;
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
            ps.setInt(1, charid);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                throw new RuntimeException("Loading char failed (not found)");
            }
            ret.name = rs.getString("name");
            if (rs.getString("previousNames") != null) {
                ret.previousNames = rs.getString("previousNames");
                //check if new name
                boolean newName = true;
                if (ret.previousNames.length() > 1) {
                    for (String prevName : ret.previousNames.split(",")) {
                        if (prevName.equals(ret.name)) {
                            newName = false;
                        }
                    }
                } else if (ret.previousNames.length() == 1 && !ret.previousNames.equals(ret.name)) {
                    ret.previousNames += "," + ret.name;
                    newName = false;
                }
                if (newName && ret.previousNames.length() > 0) {
                    ret.previousNames += "," + ret.name;
                } else if (newName) {
                    ret.previousNames = ret.name;
                }
                //end check
            } else {
                ret.previousNames = ret.name;
            }
            ret.canTalk = rs.getInt("canTalk") == 1;
            ret.level = rs.getInt("level");
            ret.totalLevel = rs.getInt("totalLevel");
            ret.rebirths = rs.getInt("rebirths");
            ret.rebirthRanking = rs.getInt("rebirthRank");
            ret.fame = rs.getInt("fame");
            ret.str = rs.getInt("str");
            ret.dex = rs.getInt("dex");
            ret.int_ = rs.getInt("int");
            ret.luk = rs.getInt("luk");
            ret.exp.set(rs.getInt("exp"));
            ret.expRate = rs.getInt("expRate");
            ret.mesoRate = rs.getInt("mesoRate");
            ret.dropRate = rs.getInt("dropRate");
            ret.gachaexp.set(rs.getInt("gachaExp"));
            ret.hp = rs.getInt("hp");
            ret.maxhp = rs.getInt("maxHp");
            ret.mp = rs.getInt("mp");
            ret.maxmp = rs.getInt("maxMp");
            ret.hpMpApUsed = rs.getInt("hpMpUsed");
            ret.hasMerchant = rs.getInt("hasMerchant") == 1;
            ret.remainingSp = rs.getInt("sp");
            ret.remainingAp = rs.getInt("ap");
            ret.apPerLevel = rs.getInt("apPerLevel");
            ret.storedAp = rs.getInt("storedAp");
            ret.autoAp = rs.getInt("autoAp");
            ret.meso.set(rs.getInt("meso"));
            ret.wealth = rs.getLong("wealth");
            ret.wealthRanking = rs.getInt("wealthRank");
            ret.merchantmeso = rs.getInt("merchantMesos");
            ret.gmLevel = MapleGroup.getById(rs.getInt("gm"));
            ret.skinColor = MapleSkinColor.getById(rs.getInt("skinColor"));
            ret.gender = rs.getInt("gender");
            ret.job = MapleJob.getById(rs.getInt("job"));
            ret.finishedDojoTutorial = rs.getInt("finishedDojoTutorial") == 1;
            ret.vanquisherKills = rs.getInt("vanquisherKills");
            ret.omokwins = rs.getInt("omokWins");
            ret.omoklosses = rs.getInt("omokLosses");
            ret.omokties = rs.getInt("omokTies");
            ret.matchcardwins = rs.getInt("matchcardWins");
            ret.matchcardlosses = rs.getInt("matchcardLosses");
            ret.matchcardties = rs.getInt("matchcardTies");
            ret.hair = rs.getInt("hair");
            ret.face = rs.getInt("face");
            ret.accountid = rs.getInt("accountId");
            ret.origAccountId = rs.getInt("origAccId");
            if (ret.origAccountId == 0) {
                ret.origAccountId = ret.accountid;
            }
            ret.createDate = rs.getTimestamp("createDate");
            ret.mapid = rs.getInt("map");
            ret.returnMap = rs.getInt("returnMap");
            ret.initialSpawnPoint = rs.getInt("spawnPoint");
            ret.worldid = rs.getByte("world");
            ret.rank = rs.getInt("rank");
            ret.rankMove = rs.getInt("rankMove");
            ret.jobRank = rs.getInt("jobRank");
            ret.jobRankMove = rs.getInt("jobRankMove");
            int mountexp = rs.getInt("mountExp");
            int mountlevel = rs.getInt("mountLevel");
            int mounttiredness = rs.getInt("mountTiredness");
            ret.guildid = rs.getInt("guildId");
            ret.guildrank = rs.getInt("guildRank");
            ret.allianceRank = rs.getInt("allianceRank");
            ret.familyId = rs.getInt("familyId");
            ret.bookCover = rs.getInt("monsterBookCover");
            ret.monsterbook = new MonsterBook();
            ret.monsterbook.loadCards(charid);
            ret.vanquisherStage = rs.getInt("vanquisherStage");
            ret.dojoPoints = rs.getInt("dojoPoints");
            ret.dojoStage = rs.getInt("lastDojoStage");
            if (ret.guildid > 0) {
                ret.mgc = new MapleGuildCharacter(ret);
            }
            int buddyCapacity = rs.getInt("buddyCapacity");
            ret.buddylist = new BuddyList(buddyCapacity);
            ret.nextMap = -1;
            ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(
                    rs.getByte("equipSlots"));
            ret.getInventory(MapleInventoryType.USE).setSlotLimit(
                    rs.getByte("useSlots"));
            ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(
                    rs.getByte("setupSlots"));
            ret.getInventory(MapleInventoryType.ETC).setSlotLimit(
                    rs.getByte("etcSlots"));
            for (Pair<IItem, MapleInventoryType> item : ItemFactory.INVENTORY.loadItems(ret.id, !channelserver)) {
                ret.getInventory(item.getRight()).addFromDB(item.getLeft());
                IItem itemz = item.getLeft();
                if (itemz.getPetId() > -1) {
                    MaplePet pet = itemz.getPet();
                    if (pet != null && pet.isSummoned()) {
                        ret.addPet(pet);
                    }
                    continue;
                }
                if (item.getRight().equals(MapleInventoryType.EQUIP)
                        || item.getRight().equals(MapleInventoryType.EQUIPPED)) {
                    IEquip equip = (IEquip) item.getLeft();
                    if (equip.getRingId() > -1) {
                        MapleRing ring = MapleRing.loadFromDb(equip.getRingId());
                        if (item.getRight().equals(MapleInventoryType.EQUIPPED)) {
                            ring.equip();
                        }
                        if (ring.getItemId() > 1112012) {
                            ret.addFriendshipRing(ring);
                        } else {
                            ret.addCrushRing(ring);
                        }
                    }
                }
            }
            if (channelserver) {
                MapleMapFactory mapFactory = client.getChannelServer().getMapFactory();
                ret.map = mapFactory.getMap(ret.mapid);
                if (ret.map == null) {
                    ret.map = mapFactory.getMap(100000000);
                }
                MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
                if (portal == null) {
                    portal = ret.map.getPortal(0);
                    ret.initialSpawnPoint = 0;
                }
                ret.setPosition(portal.getPosition());
                int partyid = rs.getInt("party");
                MapleParty party = Server.getInstance().getWorld(ret.worldid).getParty(partyid);
                if (party != null) {
                    ret.mpc = party.getMemberById(ret.id);
                    if (ret.mpc != null) {
                        ret.party = party;
                    }
                }
                int messengerid = rs.getInt("messengerId");
                int position = rs.getInt("messengerPosition");
                if (messengerid > 0 && position < 4 && position > -1) {
                    MapleMessenger messenger = Server.getInstance().getWorld(ret.worldid).getMessenger(messengerid);
                    if (messenger != null) {
                        ret.messenger = messenger;
                        ret.messengerposition = position;
                    }
                }
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT type FROM powerSkills WHERE characterId = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();

            while (rs.next()) {
                PowerSkillType[] values = PowerSkillType.values();
                int typeIndex = rs.getInt("type");
                if(typeIndex < 0 || typeIndex >= values.length) {
                    System.out.println("An invalid power skill type of " + typeIndex 
                            + "was found for " + ret.name + " (" + ret.id + ").");
                    continue;
                }
                PowerSkillType type = values[typeIndex];
                PowerSkill skill = PowerSkill.getNewPowerSkill(type);
                skill.load(ret);
                ret.powerSkills.add(skill);
            }

            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT mapId,vip FROM trockLocations WHERE characterId = ? LIMIT 15");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            byte v = 0;
            byte r = 0;
            while (rs.next()) {
                if (rs.getInt("vip") == 1) {
                    ret.viptrockmaps[v] = rs.getInt("mapId");
                    v++;
                } else {
                    ret.trockmaps[r] = rs.getInt("mapId");
                    r++;
                }
            }
            while (v < 10) {
                ret.viptrockmaps[v] = 999999999;
                v++;
            }
            while (r < 5) {
                ret.trockmaps[r] = 999999999;
                r++;
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT name FROM accounts WHERE id = ?",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, ret.accountid);
            rs = ps.executeQuery();
            if (rs.next()) {
                ret.getClient().setAccountName(rs.getString("name"));
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT `name`,`info` FROM eventStats WHERE characterId = ?");
            ps.setInt(1, ret.id);
            rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                if (rs.getString("name").equals("rescueGaga")) {
                    ret.events.put(name, new RescueGaga(rs.getInt("info")));
                }
                // ret.events = new MapleEvents(new
                // RescueGaga(rs.getInt("rescuegaga")), new
                // ArtifactHunt(rs.getInt("artifacthunt")));
            }
            rs.close();
            ps.close();
            ret.cashshop = new CashShop(ret.accountid, ret.id, ret.getJobType());
            ret.autoban = new AutobanManager(ret);
            ret.marriageRing = null; // for now
            ps = con.prepareStatement("SELECT name, level FROM characters WHERE accountId = ? AND id != ? ORDER BY level DESC limit 1");
            ps.setInt(1, ret.accountid);
            ps.setInt(2, charid);
            rs = ps.executeQuery();
            if (rs.next()) {
                ret.linkedName = rs.getString("name");
                ret.linkedLevel = rs.getInt("level");
            }
            rs.close();
            ps.close();
            if (channelserver) {
                ps = con.prepareStatement("SELECT * FROM questStatus WHERE characterId = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                PreparedStatement pse = con.prepareStatement("SELECT * FROM questProgress WHERE questStatusId = ?");
                PreparedStatement psf = con.prepareStatement("SELECT mapId FROM medalMaps WHERE queststatusId = ?");
                while (rs.next()) {
                    MapleQuest q = MapleQuest.getInstance(rs.getShort("quest"));
                    MapleQuestStatus status = new MapleQuestStatus(
                            q,
                            MapleQuestStatus.Status.getById(rs.getInt("status")));
                    long cTime = rs.getLong("time");
                    if (cTime > -1) {
                        status.setCompletionTime(cTime * 1000);
                    }
                    status.setForfeited(rs.getInt("forfeited"));
                    ret.quests.put(q, status);
                    pse.setInt(1, rs.getInt("questStatusId"));
                    ResultSet rsProgress = pse.executeQuery();
                    while (rsProgress.next()) {
                        status.setProgress(rsProgress.getInt("progressId"),
                                rsProgress.getString("progress"));
                    }
                    rsProgress.close();
                    psf.setInt(1, rs.getInt("questStatusId"));
                    ResultSet medalmaps = psf.executeQuery();
                    while (medalmaps.next()) {
                        status.addMedalMap(medalmaps.getInt("mapId"));
                    }
                    medalmaps.close();
                }
                rs.close();
                ps.close();
                pse.close();
                psf.close();
                ps = con.prepareStatement("SELECT skillId,skillLevel,masterLevel,expiration FROM skills WHERE characterId = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.skills.put(
                            SkillFactory.getSkill(rs.getInt("skillId")),
                            new SkillEntry(rs.getByte("skillLevel"), rs.getInt("masterLevel"), rs.getLong("expiration")));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT skillId,startTime,length FROM cooldowns WHERE charId = ?");
                ps.setInt(1, ret.getId());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final int skillid = rs.getInt("skillId");
                    final long length = rs.getLong("length"), startTime = rs.getLong("startTime");
                    if (skillid != 5221999
                            && (length + startTime < System.currentTimeMillis())) {
                        continue;
                    }
                    ret.giveCoolDowns(skillid, startTime, length);
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("DELETE FROM cooldowns WHERE charId = ?");
                ps.setInt(1, ret.getId());
                ps.executeUpdate();
                ps.close();
                ps = con.prepareStatement("SELECT * FROM skillMacros WHERE characterId = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int position = rs.getInt("position");
                    SkillMacro macro = new SkillMacro(rs.getInt("skill1"),
                            rs.getInt("skill2"), rs.getInt("skill3"),
                            rs.getString("name"), rs.getInt("shout"), position);
                    ret.skillMacros[position] = macro;
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT `key`,`type`,`action` FROM keyMap WHERE characterId = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int key = rs.getInt("key");
                    int type = rs.getInt("type");
                    int action = rs.getInt("action");
                    ret.keymap.put(Integer.valueOf(key), new MapleKeyBinding(
                            type, action));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT `locationType`,`map`,`portal` FROM savedLocations WHERE characterId = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.savedLocations[SavedLocationType.valueOf(
                            rs.getString("locationType")).ordinal()] = new SavedLocation(
                            rs.getInt("map"), rs.getInt("portal"));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT `characterIdTo`,`when` FROM fameLog WHERE characterId = ? AND DATEDIFF(NOW(),`when`) < 30");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                ret.lastfametime = 0;
                ret.lastmonthfameids = new ArrayList<Integer>(31);
                while (rs.next()) {
                    ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                    ret.lastmonthfameids.add(Integer.valueOf(rs.getInt("characterIdTo")));
                }
                rs.close();
                ps.close();
                ret.buddylist.loadFromDb(charid);
                ret.storage = MapleStorage.loadOrCreateFromDB(ret.accountid,
                        ret.worldid);
                ret.recalcLocalStats();
                // ret.resetBattleshipHp();
                ret.silentEnforceMaxHpMp();
            }
            int mountid = ret.getJobType() * 10000000 + 1004;
            if (ret.getInventory(MapleInventoryType.EQUIPPED).getItem(
                    (byte) -18) != null) {
                ret.maplemount = new MapleMount(ret, ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18).getItemId(), mountid);
            } else {
                ret.maplemount = new MapleMount(ret, 0, mountid);
            }
            ret.maplemount.setExp(mountexp);
            ret.maplemount.setLevel(mountlevel);
            ret.maplemount.setTiredness(mounttiredness);
            ret.maplemount.setActive(false);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String makeMapleReadable(String in) {
        String i = in.replace('I', 'i');
        i = i.replace('l', 'L');
        i = i.replace("rn", "Rn");
        i = i.replace("vv", "Vv");
        i = i.replace("VV", "Vv");
        return i;
    }

    private static class MapleBuffStatValueHolder {

        public MapleStatEffect effect;
        public long startTime;
        public int value;
        public ScheduledFuture<?> schedule;

        public MapleBuffStatValueHolder(MapleStatEffect effect, long startTime,
                ScheduledFuture<?> schedule, int value) {
            super();
            this.effect = effect;
            this.startTime = startTime;
            this.schedule = schedule;
            this.value = value;
        }
    }

    public static class MapleCoolDownValueHolder {

        public int skillId;
        public long startTime, length;
        public ScheduledFuture<?> timer;

        public MapleCoolDownValueHolder(int skillId, long startTime,
                long length, ScheduledFuture<?> timer) {
            super();
            this.skillId = skillId;
            this.startTime = startTime;
            this.length = length;
            this.timer = timer;
        }
    }

    public void mobKilled(int id) {
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() == MapleQuestStatus.Status.COMPLETED
                    || q.getQuest().canComplete(this, null)) {
                continue;
            }
            String progress = q.getProgress(id);
            if (!progress.isEmpty()
                    && Integer.parseInt(progress) >= q.getQuest().getMobAmountNeeded(id)) {
                continue;
            }
            if (q.progress(id)) {
                client.announce(MaplePacketCreator.updateQuest(q.getQuest().getId(), q.getQuestData()));
            }
        }
    }

    public void mount(int id, int skillid) {
        maplemount = new MapleMount(this, id, skillid);
    }

    private void playerDead() {
        cancelAllBuffs();
        dispelDebuffs();
        if (getEventInstance() != null) {
            getEventInstance().playerKilled(this);
        }
        int[] charmID = {5130000, 4031283, 4140903};
        int possesed = 0;
        int i;
        for (i = 0; i < charmID.length; i++) {
            int quantity = getItemQuantity(charmID[i], false);
            if (possesed == 0 && quantity > 0) {
                possesed = quantity;
                break;
            }
        }
        if (possesed > 0) {
            message("You have used a safety charm, so your EXP points have not been decreased.");
            MapleInventoryManipulator.removeById(client,
                    MapleItemInformationProvider.getInstance().getInventoryType(charmID[i]), charmID[i], 1, true,
                    false);
        } else if (mapid > 925020000 && mapid < 925030000) {
            this.dojoStage = 0;
        } else if (mapid > 980000100 && mapid < 980000700) {
            getMap().broadcastMessage(this, MaplePacketCreator.CPQDied(this));
        } else if (getJob() != MapleJob.Aventurer && getJob() != MapleJob.Noblesse && getJob() != MapleJob.Legend) { // Hmm...
            int XPdummy = ExpTable.getExpNeededForLevel(getLevel());
            if (getMap().isTown()) {
                XPdummy /= 100;
            }
            if (XPdummy == ExpTable.getExpNeededForLevel(getLevel())) {
                if (getLuk() <= 100 && getLuk() > 8) {
                    XPdummy *= (200 - getLuk()) / 2000;
                } else if (getLuk() < 8) {
                    XPdummy /= 10;
                } else {
                    XPdummy /= 20;
                }
            }
            if (getExp() > XPdummy) {
                gainExp(-XPdummy, false, false);
            } else {
                gainExp(-getExp(), false, false);
            }
        }
        if (getBuffedValue(MapleBuffStat.MORPH) != null) {
            cancelEffectFromBuffStat(MapleBuffStat.MORPH);
        }

        if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        }

        if (getChair() == -1) {
            setChair(0);
            client.announce(MaplePacketCreator.cancelChair(-1));
            getMap().broadcastMessage(this,
                    MaplePacketCreator.showChair(getId(), 0), false);
        }
        client.announce(MaplePacketCreator.enableActions());
    }

    private void prepareDragonBlood(final MapleStatEffect bloodEffect) {
        if (dragonBloodSchedule != null) {
            dragonBloodSchedule.cancel(false);
        }
        dragonBloodSchedule = TimerManager.getInstance().register(
                new Runnable() {

                    @Override
                    public void run() {
                        addHP(-bloodEffect.getX());
                        client.announce(MaplePacketCreator.showOwnBuffEffect(
                                bloodEffect.getSourceId(), 5));
                        getMap().broadcastMessage(
                                MapleCharacter.this,
                                MaplePacketCreator.showBuffeffect(getId(),
                                bloodEffect.getSourceId(), 5), false);
                        checkBerserk();
                    }
                }, 4000, 4000);
    }

    private void recalcLocalStats() {
        int oldmaxhp = localmaxhp;
        localmaxhp = getMaxHp();
        localmaxmp = getMaxMp();
        localdex = getDex();
        localint_ = getInt();
        localstr = getStr();
        localluk = getLuk();
        int speed = 100, jump = 100;
        magic = localint_;
        watk = 0;
        for (IItem item : getInventory(MapleInventoryType.EQUIPPED)) {
            IEquip equip = (IEquip) item;
            localmaxhp += equip.getHp();
            localmaxmp += equip.getMp();
            localdex += equip.getDex();
            localint_ += equip.getInt();
            localstr += equip.getStr();
            localluk += equip.getLuk();
            magic += equip.getMatk() + equip.getInt();
            watk += equip.getWatk();
            speed += equip.getSpeed();
            jump += equip.getJump();
        }
        magic = Math.min(magic, 2000);
        Integer hbhp = getBuffedValue(MapleBuffStat.HYPERBODYHP);
        if (hbhp != null) {
            localmaxhp += (hbhp.doubleValue() / 100) * localmaxhp;
        }
        Integer hbmp = getBuffedValue(MapleBuffStat.HYPERBODYMP);
        if (hbmp != null) {
            localmaxmp += (hbmp.doubleValue() / 100) * localmaxmp;
        }
        localmaxhp = Math.min(30000, localmaxhp);
        localmaxmp = Math.min(30000, localmaxmp);
        Integer watkbuff = getBuffedValue(MapleBuffStat.WATK);
        if (watkbuff != null) {
            watk += watkbuff.intValue();
        }
        if (job.isA(MapleJob.Bowman)) {
            ISkill expert = null;
            if (job.isA(MapleJob.Marksman)) {
                expert = SkillFactory.getSkill(3220004);
            } else if (job.isA(MapleJob.Bow_Master)) {
                expert = SkillFactory.getSkill(3120005);
            }
            if (expert != null) {
                int boostLevel = getSkillLevel(expert);
                if (boostLevel > 0) {
                    watk += expert.getEffect(boostLevel).getX();
                }
            }
        }
        Integer matkbuff = getBuffedValue(MapleBuffStat.MATK);
        if (matkbuff != null) {
            magic += matkbuff.intValue();
        }
        Integer speedbuff = getBuffedValue(MapleBuffStat.SPEED);
        if (speedbuff != null) {
            speed += speedbuff.intValue();
        }
        Integer jumpbuff = getBuffedValue(MapleBuffStat.JUMP);
        if (jumpbuff != null) {
            jump += jumpbuff.intValue();
        }
        if (speed > 140) {
            speed = 140;
        }
        if (jump > 123) {
            jump = 123;
        }
        if (oldmaxhp != 0 && oldmaxhp != localmaxhp) {
            updatePartyMemberHP();
        }
    }

    public void receivePartyMemberHP() {
        if (party != null) {
            byte channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapId() == getMapId()
                        && partychar.getChannel() == channel) {
                    MapleCharacter other = Server.getInstance().getWorld(worldid).getChannel(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        client.announce(MaplePacketCreator.updatePartyMemberHP(
                                other.getId(), other.getHp(),
                                other.getCurrentMaxHp()));
                    }
                }
            }
        }
    }

    public void registerEffect(MapleStatEffect effect, long starttime,
            ScheduledFuture<?> schedule) {
        if (effect.isDragonBlood()) {
            prepareDragonBlood(effect);
        } else if (effect.isBerserk()) {
            checkBerserk();
        } else if (effect.isBeholder()) {
            final int beholder = DarkKnight.BEHOLDER;
            if (beholderHealingSchedule != null) {
                beholderHealingSchedule.cancel(false);
            }
            if (beholderBuffSchedule != null) {
                beholderBuffSchedule.cancel(false);
            }
            ISkill bHealing = SkillFactory.getSkill(DarkKnight.AURA_OF_BEHOLDER);
            int bHealingLvl = getSkillLevel(bHealing);
            if (bHealingLvl > 0) {
                final MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
                int healInterval = healEffect.getX() * 1000;
                beholderHealingSchedule = TimerManager.getInstance().register(
                        new Runnable() {

                            @Override
                            public void run() {
                                addHP(healEffect.getHp());
                                client.announce(MaplePacketCreator.showOwnBuffEffect(beholder, 2));
                                getMap().broadcastMessage(
                                        MapleCharacter.this,
                                        MaplePacketCreator.summonSkill(getId(),
                                        beholder, 5), true);
                                getMap().broadcastMessage(
                                        MapleCharacter.this,
                                        MaplePacketCreator.showOwnBuffEffect(
                                        beholder, 2), false);
                            }
                        }, healInterval, healInterval);
            }
            ISkill bBuff = SkillFactory.getSkill(DarkKnight.HEX_OF_BEHOLDER);
            if (getSkillLevel(bBuff) > 0) {
                final MapleStatEffect buffEffect = bBuff.getEffect(getSkillLevel(bBuff));
                int buffInterval = buffEffect.getX() * 1000;
                beholderBuffSchedule = TimerManager.getInstance().register(
                        new Runnable() {

                            @Override
                            public void run() {
                                buffEffect.applyTo(MapleCharacter.this);
                                client.announce(MaplePacketCreator.showOwnBuffEffect(beholder, 2));
                                getMap().broadcastMessage(
                                        MapleCharacter.this,
                                        MaplePacketCreator.summonSkill(getId(),
                                        beholder,
                                        (int) (Math.random() * 3) + 6),
                                        true);
                                getMap().broadcastMessage(
                                        MapleCharacter.this,
                                        MaplePacketCreator.showBuffeffect(
                                        getId(), beholder, 2), false);
                            }
                        }, buffInterval, buffInterval);
            }
        } else if (effect.isRecovery()) {
            final byte heal = (byte) effect.getX();
            recoveryTask = TimerManager.getInstance().register(new Runnable() {

                @Override
                public void run() {
                    recovery(heal);
                    client.announce(MaplePacketCreator.showOwnRecovery(heal));
                    getMap().broadcastMessage(MapleCharacter.this,
                            MaplePacketCreator.showRecovery(id, heal), false);
                }
            }, 5000, 5000);
        }
        for (Pair<MapleBuffStat, Integer> statup : effect.getStatups()) {
            effects.put(statup.getLeft(), new MapleBuffStatValueHolder(effect,
                    starttime, schedule, statup.getRight().intValue()));
        }
        recalcLocalStats();
    }

    public void removeAllCooldownsExcept(int id) {
        for (MapleCoolDownValueHolder mcvh : coolDowns.values()) {
            if (mcvh.skillId != id) {
                coolDowns.remove(mcvh.skillId);
            }
        }
    }

    public static void removeAriantRoom(int room) {
        ariantroomleader[room] = "";
        ariantroomslot[room] = 0;
    }

    public void removeCooldown(int skillId) {
        if (this.coolDowns.containsKey(skillId)) {
            this.coolDowns.remove(skillId);
        }
    }

    public void removePet(MaplePet pet, boolean shift_left) {
        int slot = -1;
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == pet.getUniqueId()) {
                    pets[i] = null;
                    slot = i;
                    break;
                }
            }
        }
        if (shift_left) {
            if (slot > -1) {
                for (int i = slot; i < 3; i++) {
                    if (i != 2) {
                        pets[i] = pets[i + 1];
                    } else {
                        pets[i] = null;
                    }
                }
            }
        }
    }

    public void removeVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.remove(mo);
    }

    public void resetStats() {
        List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(
                5);
        int tap = 0, tsp = 1;
        int tstr = 4, tdex = 4, tint = 4, tluk = 4;
        int levelap = (isCygnus() ? 6 : 5);
        switch (job.getId()) {
            case 100:
            case 1100:
            case 2100:// ?
                tstr = 35;
                tap = ((getLevel() - 10) * levelap) + 14;
                tsp += ((getLevel() - 10) * 3);
                break;
            case 200:
            case 1200:
                tint = 20;
                tap = ((getLevel() - 8) * levelap) + 29;
                tsp += ((getLevel() - 8) * 3);
                break;
            case 300:
            case 1300:
            case 400:
            case 1400:
                tdex = 25;
                tap = ((getLevel() - 10) * levelap) + 24;
                tsp += ((getLevel() - 10) * 3);
                break;
            case 500:
            case 1500:
                tdex = 20;
                tap = ((getLevel() - 10) * levelap) + 29;
                tsp += ((getLevel() - 10) * 3);
                break;
        }
        this.remainingAp = tap;
        this.remainingSp = tsp;
        this.dex = tdex;
        this.int_ = tint;
        this.str = tstr;
        this.luk = tluk;
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, tap));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLESP, tsp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.STR, tstr));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.DEX, tdex));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.INT, tint));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.LUK, tluk));
        announce(MaplePacketCreator.updatePlayerStats(statup));
    }

    public void resetBattleshipHp() {
        this.battleshipHp = 4000
                * getSkillLevel(SkillFactory.getSkill(Corsair.BATTLE_SHIP))
                + ((getLevel() - 120) * 2000);
    }

    public void resetEnteredScript() {
        if (entered.containsKey(map.getId())) {
            entered.remove(map.getId());
        }
    }

    public void resetEnteredScript(int mapId) {
        if (entered.containsKey(mapId)) {
            entered.remove(mapId);
        }
    }

    public void resetEnteredScript(String script) {
        for (int mapId : entered.keySet()) {
            if (entered.get(mapId).equals(script)) {
                entered.remove(mapId);
            }
        }
    }

    public void resetMGC() {
        this.mgc = null;
    }

    public void saveCooldowns() {
        if (getAllCooldowns().size() > 0) {
            try {
                Connection con = DatabaseConnection.getConnection();
                deleteWhereCharacterId(con,
                        "DELETE FROM cooldowns WHERE charId = ?");
                PreparedStatement ps = con.prepareStatement("INSERT INTO cooldowns (charId, skillID, startTime, length) VALUES (?, ?, ?, ?)");
                ps.setInt(1, getId());
                for (PlayerCoolDownValueHolder cooling : getAllCooldowns()) {
                    ps.setInt(2, cooling.skillId);
                    ps.setLong(3, cooling.startTime);
                    ps.setLong(4, cooling.length);
                    ps.addBatch();
                }
                ps.executeBatch();
                ps.close();
            } catch (SQLException se) {
            }
        }
    }

    public void saveGuildStatus() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                    "UPDATE characters SET guildId = ?, guildRank = ?, allianceRank = ? WHERE id = ?");
            ps.setInt(1, guildid);
            ps.setInt(2, guildrank);
            ps.setInt(3, allianceRank);
            ps.setInt(4, id);
            ps.execute();
            ps.close();
        } catch (SQLException se) {
        }
    }

    public void saveLocation(String type) {
        MaplePortal closest = map.findClosestPortal(getPosition());
        savedLocations[SavedLocationType.fromString(type).ordinal()] = new SavedLocation(
                getMapId(), closest != null ? closest.getId() : 0);
    }

    private class tempValue {

        public String name;
        public Object value;

        tempValue(String _name, Object _value) {
            name = _name;
            value = _value;
        }
    }

    public void saveToDB(boolean update) {
        Connection con = DatabaseConnection.getConnection();
        try {
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            con.setAutoCommit(false);
            HashMap<String, Object> values = new LinkedHashMap<String, Object>();
            values.put("canTalk", canTalk ? 1 : 0);
            if (!isGM() && level > 199) {
                values.put("level", ServerConstants.MAX_LEVEL);
            } else {
                values.put("level", level);
            }
            values.put("rebirths", rebirths);
            values.put("rebirthRank", rebirthRanking);
            values.put("fame", fame);
            values.put("str", str);
            values.put("dex", dex);
            values.put("luk", luk);
            values.put("`int`", int_);
            values.put("exp", Math.abs(exp.get()));
            values.put("expRate", expRate);
            values.put("mesoRate", mesoRate);
            values.put("dropRate", dropRate);
            values.put("lastExpCards", expRateCards);
            values.put("lastMesoCards", mesoRateCards);
            values.put("lastDropCards", dropRateCards);
            values.put("gachaExp", Math.abs(gachaexp.get()));
            values.put("hp", hp);
            values.put("mp", mp);
            values.put("maxHp", maxhp);
            values.put("maxMp", maxmp);
            values.put("sp", remainingSp);
            values.put("ap", remainingAp);
            values.put("gm", gmLevel.getId());
            values.put("skinColor", skinColor.getId());
            values.put("gender", gender);
            values.put("job", job.getId());
            values.put("hair", hair);
            values.put("face", face);
            if (map == null) {
                if (getJob() == MapleJob.Aventurer) {
                    values.put("map", 0);
                } else if (getJob() == MapleJob.Noblesse) {
                    //values.put("map", 130030000);
                    values.put("map", 0);
                } else if (getJob() == MapleJob.Legend) {
                    //values.put("map", 914000000);
                    values.put("map", 0);
                } else if (getJob() == MapleJob.GM
                        || getJob() == MapleJob.Super_GM) {
                    values.put("map", 180000000);
                }
            } else {
                if (map.getForcedReturnId() != 999999999 && !isGM()) {
                    values.put("map", map.getForcedReturnId());
                } else {
                    values.put("map", map.getId());
                }
            }
            values.put("returnMap", returnMap);
            values.put("meso", meso.get());
            if (update) {
                recalcWealth();
            }
            values.put("wealth", wealth);
            values.put("wealthRank", wealthRanking);
            values.put("hpMpUsed", hpMpApUsed);
            if (map == null || map.getId() == 610020000
                    || map.getId() == 610020001) {
                values.put("spawnPoint", 0);
            } else {
                MaplePortal closest = map.findClosestSpawnpoint(getPosition());
                if (closest != null) {
                    values.put("spawnPoint", closest.getId());
                } else {
                    values.put("spawnPoint", 0);
                }
            }
            if (party != null) {
                values.put("party", party.getId());
            } else {
                values.put("party", -1);
            }
            values.put("buddyCapacity", buddylist.getCapacity());
            if (messenger != null) {
                values.put("messengerId", messenger.getId());
                values.put("messengerPosition", messengerposition);
            } else {
                values.put("messengerId", 0);
                values.put("messengerPosition", 4);
            }
            if (maplemount != null) {
                values.put("mountLevel", maplemount.getLevel());
                values.put("mountExp", maplemount.getExp());
                values.put("mountTiredness", maplemount.getTiredness());
            } else {
                values.put("mountLevel", 1);
                values.put("mountExp", 0);
                values.put("mountTiredness", 0);
            }

            values.put("equipSlots", getSlots(1));
            values.put("useSlots", getSlots(2));
            values.put("setupSlots", getSlots(3));
            values.put("etcSlots", getSlots(4));

            if (update) {
                monsterbook.saveCards(getId());
            }

            values.put("monsterBookCover", bookCover);
            values.put("vanquisherStage", vanquisherStage);
            values.put("dojoPoints", dojoPoints);
            values.put("lastDojoStage", dojoStage);
            values.put("finishedDojoTutorial", finishedDojoTutorial ? 1 : 0);
            values.put("vanquisherKills", vanquisherKills);
            values.put("matchcardWins", matchcardwins);
            values.put("matchcardLosses", matchcardlosses);
            values.put("matchcardTies", matchcardties);
            values.put("omokWins", omokwins);
            values.put("omokLosses", omoklosses);
            values.put("omokTies", omokties);
            values.put("origAccId", origAccountId);
            if (update) {
                values.put("id", id);
            } else {
                values.put("accountId", accountid);
                values.put("name", name);
                values.put("world", worldid);
            }

            PreparedStatement ps;
            if (update) {
                StringBuilder sql = new StringBuilder();
                sql.append("UPDATE characters SET ");
                for (int i = 0; i < values.size(); i++) {
                    if (!values.keySet().toArray()[i].equals("id")) {
                        sql.append(values.keySet().toArray()[i]).append(" = ?, ");
                    }
                }
                sql = sql.delete(sql.length() - 2, sql.length() - 1); // Removes the ', ' at the end
                sql.append(" WHERE id = ?");
                ps = con.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            } else {
                StringBuilder set = new StringBuilder();
                StringBuilder vals = new StringBuilder();
                for (int i = 0; i < values.size(); i++) {
                    set.append(values.keySet().toArray()[i]).append(", ");
                    vals.append("?, ");
                }
                set = set.delete(set.length() - 2, set.length() - 1); // Removes the ', ' at the end
                vals = vals.delete(vals.length() - 2, vals.length() - 1); // Removes the ', ' at the end
                String sql = "INSERT INTO characters (" + set + ") VALUES (" + vals + ")";
                ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            }

            Object[] keys = values.keySet().toArray();
            for (int i = 1; i <= values.size(); i++) {
                String key = (String) keys[i - 1];
                Object val = values.get(key);
                if (val instanceof Integer) {
                    ps.setInt(i, (Integer) val);
                } else if (val instanceof String) {
                    ps.setString(i, (String) val);
                } else if (val instanceof Long) {
                    ps.setLong(i, (Long) val);
                } else if (val instanceof Double) {
                    ps.setDouble(i, (Double) val);
                } else if (val instanceof Byte) {
                    ps.setByte(i, (Byte) val);
                } else if (val  == null) {
                    System.out.println("Null value when setting '"
                            + key + "' in character save statement.");
                } else {
                    System.out.println("Unkown value type of '"
                            + val.getClass().getName() + "' when setting '"
                            + key + "' in character save statement.");
                }
            }

            values.clear();

            try {
                int updateRows = ps.executeUpdate();
                if (!update) {
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        this.id = rs.getInt(1);
                    } else {
                        throw new RuntimeException("Inserting char failed.");
                    }
                } else if (updateRows < 1) {
                    throw new RuntimeException("Character not in database (" + id
                            + ")");
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }

            
            ps = con.prepareStatement("DELETE FROM powerSkills WHERE characterId = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

            for (PowerSkill powerSkill : powerSkills)
                if(powerSkill != null)
                    powerSkill.save(this);

            for (int i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    pets[i].saveToDb();
                }
            }
            deleteWhereCharacterId(con,
                    "DELETE FROM keyMap WHERE characterId = ?");
            ps = con.prepareStatement("INSERT INTO keyMap (characterId, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            for (Entry<Integer, MapleKeyBinding> keybinding : keymap.entrySet()) {
                ps.setInt(2, keybinding.getKey().intValue());
                ps.setInt(3, keybinding.getValue().getType());
                ps.setInt(4, keybinding.getValue().getAction());
                ps.addBatch();
            }
            ps.executeBatch();
            deleteWhereCharacterId(con,
                    "DELETE FROM skillMacros WHERE characterId = ?");
            ps = con.prepareStatement("INSERT INTO skillMacros (characterId, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, getId());
            for (int i = 0; i < 5; i++) {
                SkillMacro macro = skillMacros[i];
                if (macro != null) {
                    ps.setInt(2, macro.getSkill1());
                    ps.setInt(3, macro.getSkill2());
                    ps.setInt(4, macro.getSkill3());
                    ps.setString(5, macro.getName());
                    ps.setInt(6, macro.getShout());
                    ps.setInt(7, i);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            List<Pair<IItem, MapleInventoryType>> itemsWithType = new ArrayList<Pair<IItem, MapleInventoryType>>();

            for (MapleInventory iv : inventory) {
                for (IItem item : iv.list()) {
                    itemsWithType.add(new Pair<IItem, MapleInventoryType>(item,
                            iv.getType()));
                }
            }

            ItemFactory.INVENTORY.saveItems(itemsWithType, id);
            deleteWhereCharacterId(con,
                    "DELETE FROM skills WHERE characterId = ?");
            ps = con.prepareStatement("INSERT INTO skills (characterId, skillId, skillLevel, masterLevel, expiration) VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, id);
            for (Entry<ISkill, SkillEntry> skill : skills.entrySet()) {
                ps.setInt(2, skill.getKey().getId());
                ps.setInt(3, skill.getValue().skillevel);
                ps.setInt(4, skill.getValue().masterlevel);
                ps.setLong(5, skill.getValue().expiration);
                ps.addBatch();
            }
            ps.executeBatch();
            deleteWhereCharacterId(con,
                    "DELETE FROM savedLocations WHERE characterId = ?");
            ps = con.prepareStatement("INSERT INTO savedLocations (characterId, `locationType`, `map`, `portal`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            for (SavedLocationType savedLocationType : SavedLocationType.values()) {
                if (savedLocations[savedLocationType.ordinal()] != null) {
                    ps.setString(2, savedLocationType.name());
                    ps.setInt(3, savedLocations[savedLocationType.ordinal()].getMapId());
                    ps.setInt(4, savedLocations[savedLocationType.ordinal()].getPortal());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            deleteWhereCharacterId(con,
                    "DELETE FROM trockLocations WHERE characterId = ?");
            ps = con.prepareStatement("INSERT INTO trockLocations(characterId, mapId, vip) VALUES (?, ?, 0)");
            for (int i = 0; i < getTrockSize(); i++) {
                if (trockmaps[i] != 999999999) {
                    ps.setInt(1, getId());
                    ps.setInt(2, trockmaps[i]);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            ps = con.prepareStatement("INSERT INTO trockLocations(characterId, mapId, vip) VALUES (?, ?, 1)");
            for (int i = 0; i < getVipTrockSize(); i++) {
                if (viptrockmaps[i] != 999999999) {
                    ps.setInt(1, getId());
                    ps.setInt(2, viptrockmaps[i]);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            deleteWhereCharacterId(con,
                    "DELETE FROM buddies WHERE characterId = ? AND pending = 0");
            ps = con.prepareStatement("INSERT INTO buddies (characterId, `buddyId`, `pending`, `group`) VALUES (?, ?, 0, ?)");
            ps.setInt(1, id);
            for (BuddylistEntry entry : buddylist.getBuddies()) {
                if (entry.isVisible()) {
                    ps.setInt(2, entry.getCharacterId());
                    ps.setString(3, entry.getGroup());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            deleteWhereCharacterId(con,
                    "DELETE FROM eventStats WHERE characterId = ?");
            deleteWhereCharacterId(con,
                    "DELETE FROM questStatus WHERE characterId = ?");
            ps = con.prepareStatement(
                    "INSERT INTO questStatus (`questStatusId`, `characterId`, `quest`, `status`, `time`, `forfeited`) VALUES (DEFAULT, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            PreparedStatement pse = con.prepareStatement("INSERT INTO questProgress VALUES (DEFAULT, ?, ?, ?)");
            PreparedStatement psf = con.prepareStatement("INSERT INTO medalMaps VALUES (DEFAULT, ?, ?)");
            ps.setInt(1, id);
            for (MapleQuestStatus q : quests.values()) {
                ps.setInt(2, q.getQuest().getId());
                ps.setInt(3, q.getStatus().getId());
                ps.setInt(4, (int) (q.getCompletionTime() / 1000));
                ps.setInt(5, q.getForfeited());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                for (int mob : q.getProgress().keySet()) {
                    pse.setInt(1, rs.getInt(1));
                    pse.setInt(2, mob);
                    pse.setString(3, q.getProgress(mob));
                    pse.addBatch();
                }
                for (int i = 0; i < q.getMedalMaps().size(); i++) {
                    psf.setInt(1, rs.getInt(1));
                    psf.setInt(2, q.getMedalMaps().get(i));
                    psf.addBatch();
                }
                pse.executeBatch();
                psf.executeBatch();
                rs.close();
            }
            pse.close();
            psf.close();
            if (cashshop != null) {
                cashshop.save();
            }
            if (storage != null) {
                storage.saveToDB();
            }
            ps.close();
            con.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException se) {
            }
        } finally {
            try {
                con.setAutoCommit(true);
                con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            } catch (Exception e) {
            }
        }
    }

    public void sendPolice(int greason, String reason, int duration) {
        announce(MaplePacketCreator.sendPolice(greason, reason, duration));
        this.isbanned = true;
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                client.disconnect(); // FAGGOTS
            }
        }, duration);
    }

    public void sendPolice(String text) {
        announce(MaplePacketCreator.sendPolice(text));
        this.isbanned = true;
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                client.disconnect(); // FAGGOTS
            }
        }, 6000);
    }

    public void sendKeymap() {
        client.announce(MaplePacketCreator.getKeymap(keymap));
    }

    public void sendMacros() {
        boolean macros = false;
        for (int i = 0; i < 5; i++) {
            if (skillMacros[i] != null) {
                macros = true;
            }
        }
        if (macros) {
            client.announce(MaplePacketCreator.getMacros(skillMacros));
        }
    }

    public void sendNote(String to, String msg, byte fame) throws SQLException {
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "INSERT INTO notes (`to`, `from`, `message`, `timeStamp`, `fame`) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, to);
        ps.setString(2, this.getName());
        ps.setString(3, msg);
        ps.setLong(4, System.currentTimeMillis());
        ps.setByte(5, fame);
        ps.executeUpdate();
        ps.close();
    }

    public void sendNote(String to, String from, String msg) throws SQLException {
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "INSERT INTO notes (`to`, `from`, `message`, `timeStamp`, `fame`) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, to);
        ps.setString(2, from);
        ps.setString(3, msg);
        ps.setLong(4, System.currentTimeMillis());
        ps.setByte(5, (byte) 0);
        ps.executeUpdate();
        ps.close();
    }

    public void setAllianceRank(int rank) {
        allianceRank = rank;
        if (mgc != null) {
            mgc.setAllianceRank(rank);
        }
    }

    public void setAllowWarpToId(int id) {
        this.warpToId = id;
    }

    public static void setAriantRoomLeader(int room, String charname) {
        ariantroomleader[room] = charname;
    }

    public static void setAriantSlotRoom(int room, int slot) {
        ariantroomslot[room] = slot;
    }

    public void setBattleshipHp(int battleshipHp) {
        this.battleshipHp = battleshipHp;
    }

    public void setBuddyCapacity(int capacity) {
        buddylist.setCapacity(capacity);
        client.announce(MaplePacketCreator.updateBuddyCapacity(capacity));
    }

    public void setBuffedValue(MapleBuffStat effect, int value) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return;
        }
        mbsvh.value = value;
    }

    public void setChair(int chair) {
        this.chair = chair;
    }

    public void setChalkboard(String text) {
        this.chalktext = text;
    }

    public void setDex(int dex) {
        this.dex = dex;
        recalcLocalStats();
    }

    public void setDojoEnergy(int x) {
        this.dojoEnergy = x;
    }

    public void setDojoParty(boolean b) {
        this.dojoParty = b;
    }

    public void setDojoPoints(int x) {
        this.dojoPoints = x;
    }

    public void setDojoStage(int x) {
        this.dojoStage = x;
    }

    public void setDojoStart() {
        this.dojoMap = map;
        int stage = (map.getId() / 100) % 100;
        this.dojoFinish = System.currentTimeMillis()
                + (stage > 36 ? 15 : stage / 6 + 5) * 60000;
    }

    public void setCards() {
//        World worldz = Server.getInstance().getWorld(worldid);
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeZone(TimeZone.getTimeZone("GMT-7"));
//        int hr = cal.get(Calendar.HOUR_OF_DAY);
//        if ((haveItem(5360001) && hr > 6 && hr < 12)
//                || (haveItem(5360002) && hr > 9 && hr < 15)
//                || (haveItem(536000) && hr > 12 && hr < 18)
//                || (haveItem(5360004) && hr > 15 && hr < 21)
//                || (haveItem(536000) && hr > 18)
//                || (haveItem(5360006) && hr < 5)
//                || (haveItem(5360007) && hr > 2 && hr < 6)
//                || (haveItem(5360008) && hr >= 6 && hr < 11)) {
//            this.dropRateMultiplier = 2 * worldz.getDropRate();
//            this.mesoRateMultiplier = 2 * worldz.getMesoRate();
//        } else {
//            this.dropRateMultiplier = worldz.getDropRate();
//            this.mesoRateMultiplier = worldz.getMesoRate();
//        }
//        if ((haveItem(5211000) && hr > 17 && hr < 21)
//                || (haveItem(5211014) && hr > 6 && hr < 12)
//                || (haveItem(5211015) && hr > 9 && hr < 15)
//                || (haveItem(5211016) && hr > 12 && hr < 18)
//                || (haveItem(5211017) && hr > 15 && hr < 21)
//                || (haveItem(5211018) && hr > 14)
//                || (haveItem(5211039) && hr < 5)
//                || (haveItem(5211042) && hr > 2 && hr < 8)
//                || (haveItem(5211045) && hr > 5 && hr < 11)
//                || haveItem(5211048)) {
//            if (isBeginnerJob()) {
//                this.expRateMultiplier = 2;
//            } else {
//                this.expRateMultiplier = 2 * worldz.getExpRate();
//            }
//        } else {
//        if (isBeginnerJob()) {
//            this.expRateMultiplier = 1;
//        } else {
//        }
//        }
        if (haveItem(ServerConstants.EXP_CARD_ID)) {
            this.expRateCards = 2 * countItem(ServerConstants.EXP_CARD_ID);
        }
        if (haveItem(ServerConstants.DROP_CARD_ID)) {
            this.dropRateCards = 2 * countItem(ServerConstants.DROP_CARD_ID);
            this.mesoRateCards = 2 * countItem(ServerConstants.DROP_CARD_ID);
        }
    }

    public void setEnergyBar(int set) {
        energybar = set;
    }

    public void setEventInstance(EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }

    public void setExp(int amount) {
        this.exp.set(amount);
    }

    public void setGachaExp(int amount) {
        this.gachaexp.set(amount);
    }

    public void setFace(int face) {
        this.face = face;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }

    public void setFinishedDojoTutorial() {
        this.finishedDojoTutorial = true;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setGMLevel(int l) {
        setGMLevel(MapleGroup.getById(l));
    }

    public void setGMLevel(MapleGroup group) {
        this.gmLevel = group;
    }

    public void setGuildId(int _id) {
        guildid = _id;
        if (guildid > 0) {
            if (mgc == null) {
                mgc = new MapleGuildCharacter(this);
            } else {
                mgc.setGuildId(guildid);
            }
        } else {
            mgc = null;
        }
    }

    public void setGuildRank(int _rank) {
        guildrank = _rank;
        if (mgc != null) {
            mgc.setGuildRank(_rank);
        }
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public void setHasMerchant(boolean set) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                    "UPDATE characters SET hasMerchant = ? WHERE id = ?");
            ps.setInt(1, set ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        hasMerchant = set;
    }

    public void addMerchantMesos(int add) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                    "UPDATE characters SET merchantMesos = ? WHERE id = ?",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, merchantmeso + add);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            return;
        }
        merchantmeso += add;
    }

    public void setMerchantMeso(int set) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                    "UPDATE characters SET merchantMesos = ? WHERE id = ?",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, set);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            return;
        }
        merchantmeso = set;
    }

    public void setHiredMerchant(HiredMerchant merchant) {
        this.hiredMerchant = merchant;
    }

    public void setHp(int newhp) {
        setHp(newhp, false);
    }

    public void setHp(int newhp, boolean silent) {
        int oldHp = hp;
        int thp = newhp;
        if (thp < 0) {
            thp = 0;
        }
        if (thp > localmaxhp) {
            thp = localmaxhp;
        }
        this.hp = thp;
        if (!silent) {
            updatePartyMemberHP();
        }
        if (oldHp > hp && !isAlive()) {
            playerDead();
        }
    }

    public void setHpMpApUsed(int mpApUsed) {
        this.hpMpApUsed = mpApUsed;
    }

    public void setHpMp(int x) {
        setHp(x);
        setMp(x);
        updateSingleStat(MapleStat.HP, hp);
        updateSingleStat(MapleStat.MP, mp);
    }

    public void setInt(int int_) {
        this.int_ = int_;
        recalcLocalStats();
    }

    public void setInventory(MapleInventoryType type, MapleInventory inv) {
        inventory[type.ordinal()] = inv;
    }

    public void setItemEffect(int itemEffect) {
        this.itemEffect = itemEffect;
    }

    public void setJob(MapleJob job) {
        this.job = job;
    }

    public void setLastHealed(long time) {
        this.lastHealed = time;
    }

    public void setLastUsedCashItem(long time) {
        this.lastUsedCashItem = time;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setLuk(int luk) {
        this.luk = luk;
        recalcLocalStats();
    }

    public void setMap(int PmapId) {
        this.mapid = PmapId;
    }

    public void setMap(MapleMap newmap) {
        this.map = newmap;
    }
    
    public void setReturnMap(int id) {
        returnMap = id;
    }

    public void setMarkedMonster(int markedMonster) {
        this.markedMonster = markedMonster;
    }

    public void setMaxHp(int hp) {
        this.maxhp = hp;
        recalcLocalStats();
    }

    public void setMaxHp(int hp, boolean ap) {
        hp = Math.min(30000, hp);
        if (ap) {
            setHpMpApUsed(getHpMpApUsed() + 1);
        }
        this.maxhp = hp;
        recalcLocalStats();
    }

    public void setMaxMp(int mp) {
        this.maxmp = mp;
        recalcLocalStats();
    }

    public void setMaxMp(int mp, boolean ap) {
        mp = Math.min(30000, mp);
        if (ap) {
            setHpMpApUsed(getHpMpApUsed() + 1);
        }
        this.maxmp = mp;
        recalcLocalStats();
    }

    public void setMessenger(MapleMessenger messenger) {
        this.messenger = messenger;
    }

    public void setMessengerPosition(int position) {
        this.messengerposition = position;
    }

    public void setMiniGame(MapleMiniGame miniGame) {
        this.miniGame = miniGame;
    }

    public void setMiniGamePoints(MapleCharacter visitor, int winnerslot,
            boolean omok) {
        if (omok) {
            if (winnerslot == 1) {
                this.omokwins++;
                visitor.omoklosses++;
            } else if (winnerslot == 2) {
                visitor.omokwins++;
                this.omoklosses++;
            } else {
                this.omokties++;
                visitor.omokties++;
            }
        } else {
            if (winnerslot == 1) {
                this.matchcardwins++;
                visitor.matchcardlosses++;
            } else if (winnerslot == 2) {
                visitor.matchcardwins++;
                this.matchcardlosses++;
            } else {
                this.matchcardties++;
                visitor.matchcardties++;
            }
        }
    }

    public void setMonsterBookCover(int bookCover) {
        this.bookCover = bookCover;
    }

    public void setMp(int newmp) {
        int tmp = newmp;
        if (tmp < 0) {
            tmp = 0;
        }
        if (tmp > localmaxmp) {
            tmp = localmaxmp;
        }
        this.mp = tmp;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParty(MapleParty party) {
        if (party == null) {
            this.mpc = null;
        }
        this.party = party;
    }

    public void setPlayerShop(MaplePlayerShop playerShop) {
        this.playerShop = playerShop;
    }

    public void setRemainingAp(int remainingAp) {
        this.remainingAp = remainingAp;
    }

    public void setRemainingSp(int remainingSp) {
        this.remainingSp = remainingSp;
    }

    public void setSearch(String find) {
        search = find;
    }

    public void setSkinColor(MapleSkinColor skinColor) {
        this.skinColor = skinColor;
    }

    public byte getSlots(int type) {
        return type == MapleInventoryType.CASH.getType() ? 96 : inventory[type].getSlotLimit();
    }

    public boolean gainSlots(int type, int slots) {
        return gainSlots(type, slots, true);
    }

    public boolean gainSlots(int type, int slots, boolean update) {
        slots += inventory[type].getSlotLimit();
        if (slots <= 96) {
            inventory[type].setSlotLimit(slots);

            saveToDB(true);
            if (update) {
                client.announce(MaplePacketCreator.updateInventorySlotLimit(
                        type, slots));
            }

            return true;
        }

        return false;
    }

    public void setShop(MapleShop shop) {
        this.shop = shop;
    }

    public void setSlot(int slotid) {
        slots = slotid;
    }

    public void setStr(int str) {
        this.str = str;
        recalcLocalStats();
    }

    public void setTrade(MapleTrade trade) {
        this.trade = trade;
    }

    public void setVanquisherKills(int x) {
        this.vanquisherKills = x;
    }

    public void setVanquisherStage(int x) {
        this.vanquisherStage = x;
    }

    public void setWorld(byte world) {
        this.worldid = world;
    }

    public void shiftPetsRight() {
        if (pets[2] == null) {
            pets[2] = pets[1];
            pets[1] = pets[0];
            pets[0] = null;
        }
    }

    public void showDojoClock() {
        int stage = (map.getId() / 100) % 100;
        long time;
        if (stage % 6 == 1) {
            time = (stage > 36 ? 15 : stage / 6 + 5) * 60;
        } else {
            time = (dojoFinish - System.currentTimeMillis()) / 1000;
        }
        if (stage % 6 > 0) {
            client.announce(MaplePacketCreator.getClock((int) time));
        }
        boolean rightmap = true;
        int clockid = (dojoMap.getId() / 100) % 100;
        if (map.getId() > clockid / 6 * 6 + 6 || map.getId() < clockid / 6 * 6) {
            rightmap = false;
        }
        final boolean rightMap = rightmap; // lol
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (rightMap) {
                    client.getPlayer().changeMap(
                            client.getChannelServer().getMapFactory().getMap(925020000));
                }
            }
        }, time * 1000 + 3000); // let the TIMES UP display for 3 seconds, then
        // warp
    }

    public void showNote() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                    "SELECT * FROM notes WHERE `to`=? AND `deleted` = 0",
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            ps.setString(1, this.getName());
            ResultSet rs = ps.executeQuery();
            rs.last();
            int count = rs.getRow();
            rs.first();
            client.announce(MaplePacketCreator.showNotes(rs, count));
            rs.close();
            ps.close();
        } catch (SQLException e) {
        }
    }

    private void silentEnforceMaxHpMp() {
        setMp(getMp());
        setHp(getHp(), true);
    }

    public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
        for (PlayerBuffValueHolder mbsvh : buffs) {
            mbsvh.effect.silentApplyBuff(this, mbsvh.startTime);
        }
    }

    public void silentPartyUpdate() {
        if (party != null) {
            Server.getInstance().getWorld(worldid).updateParty(party.getId(), PartyOperation.SILENT_UPDATE,
                    getMPC());
        }
    }

    public static class SkillEntry {

        public int masterlevel;
        public byte skillevel;
        public long expiration;

        public SkillEntry(byte skillevel, int masterlevel, long expiration) {
            this.skillevel = skillevel;
            this.masterlevel = masterlevel;
            this.expiration = expiration;
        }

        @Override
        public String toString() {
            return skillevel + ":" + masterlevel;
        }
    }

    public boolean skillisCooling(int skillId) {
        return coolDowns.containsKey(Integer.valueOf(skillId));
    }

    public void startFullnessSchedule(final int decrease, final MaplePet pet,
            int petSlot) {
        ScheduledFuture<?> schedule = TimerManager.getInstance().register(
                new Runnable() {

                    @Override
                    public void run() {
                        int newFullness = pet.getFullness() - decrease;
                        if (newFullness <= 5) {
                            pet.setFullness(15);
                            pet.saveToDb();
                            unequipPet(pet, true);
                        } else {
                            pet.setFullness(newFullness);
                            pet.saveToDb();
                            IItem petz = getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
                            client.announce(MaplePacketCreator.updateSlot(petz));
                        }
                    }
                }, 180000, 18000);
        switch (petSlot) {
            case 0:
                fullnessSchedule = schedule;
                break;
            case 1:
                fullnessSchedule_1 = schedule;
                break;
            case 2:
                fullnessSchedule_2 = schedule;
                break;
        }
    }

    public void startMapEffect(String msg, int itemId) {
        startMapEffect(msg, itemId, 30000);
    }

    public void startMapEffect(String msg, int itemId, int duration) {
        final MapleMapEffect mapEffect = new MapleMapEffect(msg, itemId);
        getClient().announce(mapEffect.makeStartData());
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                getClient().announce(mapEffect.makeDestroyData());
            }
        }, duration);
    }

    public void stopControllingMonster(MapleMonster monster) {
        controlled.remove(monster);
    }

    public void unequipAllPets() {
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                unequipPet(pets[i], true);
            }
        }
    }

    public void unequipPet(MaplePet pet, boolean shift_left) {
        unequipPet(pet, shift_left, false);
    }

    public void unequipPet(MaplePet pet, boolean shift_left, boolean hunger) {
        if (this.getPet(this.getPetIndex(pet)) != null) {
            this.getPet(this.getPetIndex(pet)).setSummoned(false);
            this.getPet(this.getPetIndex(pet)).saveToDb();
        }
        cancelFullnessSchedule(getPetIndex(pet));
        getMap().broadcastMessage(this,
                MaplePacketCreator.showPet(this, pet, true, hunger), true);
        List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
        stats.add(new Pair<MapleStat, Integer>(MapleStat.PET, Integer.valueOf(0)));
        client.getSession().write(MaplePacketCreator.petStatUpdate(this));
        client.getSession().write(MaplePacketCreator.enableActions());
        removePet(pet, shift_left);
    }

    public void updateMacros(int position, SkillMacro updateMacro) {
        skillMacros[position] = updateMacro;
    }

    public void updatePartyMemberHP() {
        if (party != null) {
            byte channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapId() == getMapId()
                        && partychar.getChannel() == channel) {
                    MapleCharacter other = Server.getInstance().getWorld(worldid).getChannel(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        other.client.announce(MaplePacketCreator.updatePartyMemberHP(getId(), this.hp, maxhp));
                    }
                }
            }
        }
    }

    public void updateQuest(MapleQuestStatus quest) {
        quests.put(quest.getQuest(), quest);
        if (quest.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
            announce(MaplePacketCreator.questProgress((short) quest.getQuest().getId(), quest.getProgress(0)));
            if (quest.getQuest().getInfoNumber() > 0) {
                announce(MaplePacketCreator.questProgress(quest.getQuest().getInfoNumber(), Integer.toString(quest.getMedalProgress())));
            }
            announce(MaplePacketCreator.updateQuestInfo((short) quest.getQuest().getId(), quest.getNpc()));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
            announce(MaplePacketCreator.completeQuest((short) quest.getQuest().getId(), quest.getCompletionTime()));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.NOT_STARTED)) {
            announce(MaplePacketCreator.forfeitQuest((short) quest.getQuest().getId()));
        }
    }

    public void questTimeLimit(final MapleQuest quest, int time) {
        ScheduledFuture<?> sf = TimerManager.getInstance().schedule(
                new Runnable() {

                    @Override
                    public void run() {
                        announce(MaplePacketCreator.questExpire(quest.getId()));
                        MapleQuestStatus newStatus = new MapleQuestStatus(
                                quest, MapleQuestStatus.Status.NOT_STARTED);
                        newStatus.setForfeited(getQuest(quest).getForfeited() + 1);
                        updateQuest(newStatus);
                    }
                }, time);
        announce(MaplePacketCreator.addQuestTimeLimit(quest.getId(), time));
        timers.add(sf);
    }

    public void updateSingleStat(MapleStat stat, int newval) {
        updateSingleStat(stat, newval, false);
    }

    private void updateSingleStat(MapleStat stat, int newval,
            boolean itemReaction) {
        announce(MaplePacketCreator.updatePlayerStats(Collections.singletonList(new Pair<MapleStat, Integer>(stat, Integer.valueOf(newval))), itemReaction));
    }

    public void announce(MaplePacket packet) {
        client.announce(packet);
    }

    @Override
    public int getObjectId() {
        return getId();
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(MaplePacketCreator.removePlayerFromMap(this.getObjectId()));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (!this.isHidden() || client.getPlayer().isGM()) {
            if(client.getPlayer().isGM())
                client.announce(MaplePacketCreator.spawnPlayerMapobject(this, true));
            else
                client.announce(MaplePacketCreator.spawnPlayerMapobject(this, false));
        }
    }

    @Override
    public void setObjectId(int id) {
        // Ha!
    }

    @Override
    public String toString() {
        return name;
    }
    private int givenRiceCakes;
    private boolean gottenRiceHat;

    public int getGivenRiceCakes() {
        return givenRiceCakes;
    }

    public void increaseGivenRiceCakes(int amount) {
        this.givenRiceCakes += amount;
    }

    public boolean getGottenRiceHat() {
        return gottenRiceHat;
    }

    public void setGottenRiceHat(boolean b) {
        this.gottenRiceHat = b;
    }

    public int getLinkedLevel() {
        return linkedLevel;
    }

    public String getLinkedName() {
        return linkedName;
    }

    public CashShop getCashShop() {
        return cashshop;
    }

    public void portalDelay(long delay) {
        this.portaldelay = System.currentTimeMillis() + delay;
    }

    public long portalDelay() {
        return portaldelay;
    }

    public void blockPortal(String scriptName) {
        if (!blockedPortals.contains(scriptName) && scriptName != null) {
            blockedPortals.add(scriptName);
            client.announce(MaplePacketCreator.enableActions());
        }
    }

    public void unblockPortal(String scriptName) {
        if (blockedPortals.contains(scriptName) && scriptName != null) {
            blockedPortals.remove(scriptName);
        }
    }

    public List<String> getBlockedPortals() {
        return blockedPortals;
    }

    public boolean getAranIntroState(String mode) {
        if (areaDataContains(mode)) {
            return true;
        }
        return false;
    }

    public void addAreaData(int quest, String data) {
        if (!this.areaDataContains(data)) {
            this.addAreaData(data);
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("INSERT INTO charAreaInfo VALUES (DEFAULT, ?, ?, ?)");
                ps.setInt(1, getId());
                ps.setInt(2, quest);
                ps.setString(3, data);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                System.out.println("[AREA DATA] An error has occured.");
                ex.printStackTrace();
            }
        }
    }

    public void removeAreaData() {
        this.clearAreaData();
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM charAreaInfo WHERE charid = ?");
            ps.setInt(1, getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("[AREA DATA] An error has occured.");
            ex.printStackTrace();
        }
    }

    public void autoban(String reason, int greason) {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE));
        Timestamp TS = new Timestamp(cal.getTimeInMillis());
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banReason = ?, tempBan = ?, greason = ? WHERE id = ?");
            ps.setString(1, reason);
            ps.setTimestamp(2, TS);
            ps.setInt(3, greason);
            ps.setInt(4, accountid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
        }
    }

    public boolean isBanned() {
        return isbanned;
    }

    public int[] getTrockMaps() {
        return trockmaps;
    }

    public int[] getVipTrockMaps() {
        return viptrockmaps;
    }

    public int getTrockSize() {
        int ret = 0;
        for (int i = 0; i < 5; i++) {
            if (trockmaps[i] != 999999999) {
                ret++;
            }
        }
        return ret;
    }

    public void deleteFromTrocks(int map) {
        for (int i = 0; i < 5; i++) {
            if (trockmaps[i] == map) {
                trockmaps[i] = 999999999;
                break;
            }
        }
    }

    public void addTrockMap() {
        if (getTrockSize() >= 5) {
            return;
        }
        trockmaps[getTrockSize()] = getMapId();
    }

    public boolean isTrockMap(int id) {
        for (int i = 0; i < 5; i++) {
            if (trockmaps[i] == id) {
                return true;
            }
        }
        return false;
    }

    public int getVipTrockSize() {
        int ret = 0;
        for (int i = 0; i < 10; i++) {
            if (viptrockmaps[i] != 999999999) {
                ret++;
            }
        }
        return ret;
    }

    public void deleteFromVipTrocks(int map) {
        for (int i = 0; i < 10; i++) {
            if (viptrockmaps[i] == map) {
                viptrockmaps[i] = 999999999;
                break;
            }
        }
    }

    public void addVipTrockMap() {
        if (getVipTrockSize() >= 10) {
            return;
        }

        viptrockmaps[getVipTrockSize()] = getMapId();
    }

    public boolean isVipTrockMap(int id) {
        for (int i = 0; i < 10; i++) {
            if (viptrockmaps[i] == id) {
                return true;
            }
        }
        return false;
    }
    // EVENTS
    private byte team = 0;
    private MapleFitness fitness;
    private MapleOla ola;
    private long snowballattack;

    public byte getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = (byte) team;
    }

    public MapleOla getOla() {
        return ola;
    }

    public void setOla(MapleOla ola) {
        this.ola = ola;
    }

    public MapleFitness getFitness() {
        return fitness;
    }

    public void setFitness(MapleFitness fit) {
        this.fitness = fit;
    }

    public long getLastSnowballAttack() {
        return snowballattack;
    }

    public void setLastSnowballAttack(long time) {
        this.snowballattack = time;
    }
    // Monster Carnival
    private int cp = 0;
    private int obtainedcp = 0;
    private MonsterCarnivalParty carnivalparty;
    private MonsterCarnival carnival;

    public MonsterCarnivalParty getCarnivalParty() {
        return carnivalparty;
    }

    public void setCarnivalParty(MonsterCarnivalParty party) {
        this.carnivalparty = party;
    }

    public MonsterCarnival getCarnival() {
        return carnival;
    }

    public void setCarnival(MonsterCarnival car) {
        this.carnival = car;
    }

    public int getCP() {
        return cp;
    }

    public int getObtainedCP() {
        return obtainedcp;
    }

    public void addCP(int cp) {
        this.cp += cp;
        this.obtainedcp += cp;
    }

    public void useCP(int cp) {
        this.cp -= cp;
    }

    public void setObtainedCP(int cp) {
        this.obtainedcp = cp;
    }

    public int getAndRemoveCP() {
        int rCP = 10;
        if (cp < 9) {
            rCP = cp;
            cp = 0;
        } else {
            cp -= 10;
        }

        return rCP;
    }

    public AutobanManager getAutobanManager() {
        return autoban;
    }

    public void equipPendantOfSpirit() {
        if (pendantOfSpirit == null) {
            pendantOfSpirit = TimerManager.getInstance().register(
                    new Runnable() {

                        @Override
                        public void run() {
                            if (pendantExp < 3) {
                                pendantExp++;
                                message("Pendant of the Spirit has been equipped for "
                                        + pendantExp
                                        + " hour(s), you will now receive "
                                        + pendantExp + "0% bonus exp.");
                            } else {
                                pendantOfSpirit.cancel(false);
                            }
                        }
                    }, 3600000); // 1 hour
        }
    }

    public void unequipPendantOfSpirit() {
        if (pendantOfSpirit != null) {
            pendantOfSpirit.cancel(false);
            pendantOfSpirit = null;
        }
        pendantExp = 0;
    }

    public void increaseEquipExp(int mobexp) {
        MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
        for (IItem item : getInventory(MapleInventoryType.EQUIPPED).list()) {
            Equip nEquip = (Equip) item;
            String itemName = mii.getName(nEquip.getItemId());
            if (itemName == null) {
                continue;
            }

            if ((itemName.contains("Reverse") && nEquip.getItemLevel() < 4)
                    || itemName.contains("Timeless")
                    && nEquip.getItemLevel() < 6) {
                nEquip.gainItemExp(client, mobexp,
                        itemName.contains("Timeless"));
            }
        }
    }

    public Map<String, MapleEvents> getEvents() {
        return events;
    }

    public PartyQuest getPartyQuest() {
        return partyQuest;
    }

    public void setPartyQuest(PartyQuest pq) {
        this.partyQuest = pq;
    }

    public boolean areaDataContains(String data) {
        return area_data.contains(data);
    }

    public void addAreaData(String data) {
        area_data.add(data);
    }

    public void clearAreaData() {
        area_data.clear();
    }

    public void setNextMap(int mid) {
        nextMap = mid;
        nextMapPortal = 0;
    }

    public void setNextMap(int mid, int portal) {
        nextMap = mid;
        nextMapPortal = portal;
    }

    public int getNextMap() {
        return nextMap;
    }

    public int getNextMapPortal() {
        return nextMapPortal;
    }

    
    
    // ###### -- BossStory -- ######
    
    
    
    public long getLastUse() {
        return lastUse;
    }

    public void updateLastUse() {
        lastUse = System.currentTimeMillis();
    }

    public int getOriginalAccountID() {
        return origAccountId;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void forceSpeak(String message, boolean whiteBg, int show) {
        getMap().broadcastMessage(MaplePacketCreator.getChatText(getId(), message, whiteBg, show));
    }

    public boolean inJail() {
        return getMapId() == 200090300 || getMapId() == 200090310;
    }

    public boolean canTalk() {
        return canTalk;
    }

    public void setCanTalk(boolean set) {
        canTalk = set;
    }

    public int getVotePoints() {
        int points = 0;
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT votePoints FROM accounts WHERE id = ?");
            ps.setInt(1, accountid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                points = rs.getInt("votePoints");
            } else {
                points = -1;
            }
            ps.close();
        } catch (SQLException se) {
            System.out.println("getVotePoints: " + se.toString());
        }
        return points;
    }

    public void setVotePoints(int amount) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET votePoints = ? WHERE id = ?");
            ps.setInt(1, amount);
            ps.setInt(2, accountid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
            System.out.println("setVotePoints: " + se.toString());
        }
    }

    public boolean needsHelp() {
        return needsHelp;
    }

    public void setNeedsHelp(boolean need) {
        needsHelp = need;
    }

    public void rebirth() {
        rebirths++;
        totalLevel += level;
        setLevel(1);
        setExp(0);
        updateSingleStat(MapleStat.LEVEL, 1);
        updateSingleStat(MapleStat.EXP, 0);
        changeJob(MapleJob.Aventurer);
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE characters SET level = ?, exp = ?, totalLevel = ?, job = ?, storedAp = ?, rebirths = ? WHERE id = ?");
            ps.setInt(1, level);
            ps.setInt(2, Math.abs(exp.get()));
            ps.setInt(3, totalLevel);
            ps.setInt(4, job.getId());
            ps.setInt(5, storedAp);
            ps.setInt(6, rebirths);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
        }
        silentPartyUpdate();
        guildUpdate();
        //changeMap(540000000);
        message("Congratulations you have been reborn!");
        message("You have " + rebirths + " rebirths for a total of " + totalLevel + " levels.");
        message("You have " + storedAp + " ap currently stored at Cody.");
    }

    public int getRebirths() {
        return rebirths;
    }

    public void setRebirths(int set) {
        rebirths = set;
    }

    public void setAutoAp(int type) {
        this.autoAp = type;
    }

    public int getAutoAp() {
        return this.autoAp;
    }

    public boolean isAutoAp() {
        if (autoAp != 0) {
            return true;
        }
        return false;
    }

    public void removeAutoAp() {
        this.autoAp = 0;
    }

    public void updateStoredAp(int amount) {
        if (storedAp + amount < Integer.MAX_VALUE) {
            this.storedAp += amount;
        }
    }

    public void setStoredAp(int amount) {
        this.storedAp = amount;
    }

    public int getStoredAp() {
        return this.storedAp;
    }

    public void setApPerLevel(int amount) {
        this.apPerLevel = amount;
    }

    public int getApPerLevel() {
        return this.apPerLevel;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean set) {
        vip = set;
    }

    public boolean inTown() {
        return map.isTown();
    }

    public boolean inTutorial() {
        for (int m : tutorialMaps) {
            if (map.getId() == m) {
                return !isGM();
            }
        }
        return false;
    }

    public long getTotalMeso() {
        long ret = meso.get();
        long bronze = 1000000000;
        long silver = 1000000000 * 1000;
        long gold = 1000000000 * 1000 * 1000;
        ret += getItemQuantity(ServerConstants.BRONZE_$_ID, false) * bronze;
        ret += getItemQuantity(ServerConstants.SILVER_$_ID, false) * silver;
        ret += getItemQuantity(ServerConstants.GOLD_$_ID, false) * gold;
        return ret;
    }

    public long getWealth() {
        return wealth;
    }

    public long recalcWealth() {
        int bronze = getInventory(MapleInventoryType.USE).countById(ServerConstants.BRONZE_$_ID);
        int silver = getInventory(MapleInventoryType.USE).countById(ServerConstants.SILVER_$_ID);
        int gold = getInventory(MapleInventoryType.USE).countById(ServerConstants.GOLD_$_ID);
        wealth = bronze + (silver * 1000) + (gold * 1000 * 1000);
        if (wealth > Long.MAX_VALUE) {
            wealth = Long.MAX_VALUE;
        }
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET wealth = ? WHERE id = ?");
            ps.setLong(1, wealth);
            ps.setInt(2, getId());
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
        }
        return wealth;
    }

    public int getWealthRanking() {
        return wealthRanking;
    }

    public void setWealthRanking(int rank) {
        wealthRanking = rank;
    }

    public int getRebirthRanking() {
        return rebirthRanking;
    }

    public void setRebirthRanking(int rank) {
        rebirthRanking = rank;
    }

    public String getPreviousNames() {
        return previousNames;
    }

    public String getReadablePreviousNames() {
        return previousNames.replaceAll(",", ", ");
    }

    public String[] getPreviousNameArray() {
        return previousNames.split(",");
    }

    public void addNewName(String newName) {
        for (String prevName : previousNames.split(",")) {
            if (prevName.equals(newName)) {
                return;
            }
        }
        previousNames += "," + newName;
    }

    public boolean checkCurrencyConverted() {
        if (System.currentTimeMillis() >= this.nextCurrencyCheck) {
            MapleInventory inv = getInventory(MapleInventoryType.USE);
            int bronzeCount = inv.countById(ServerConstants.BRONZE_$_ID);
            int silverCount = inv.countById(ServerConstants.SILVER_$_ID);
            currencyConverted = getMeso() < 1000000000 && bronzeCount < 1000 && silverCount < 1000;
            nextCurrencyCheck = System.currentTimeMillis() + 10000; // 10 seconds later
        }
        return currencyConverted;
    }

    public boolean convertCurrency() {
        boolean converted = false;
        MapleInventory inv = getInventory(MapleInventoryType.USE);
        // Only adds if room in inventory
        while (getMeso() >= 1000000000 && MapleInventoryManipulator.checkSpace(client, ServerConstants.BRONZE_$_ID, (short) 1, "")) {
            MapleInventoryManipulator.addById(client, ServerConstants.BRONZE_$_ID, (short) 1);
            gainMeso(-1000000000, false, true, false);
        }
        int bronzeCount = inv.countById(ServerConstants.BRONZE_$_ID);
        int silverCount = inv.countById(ServerConstants.SILVER_$_ID);
        int goldCount = inv.countById(ServerConstants.GOLD_$_ID);
        while (bronzeCount >= 1000) {
            bronzeCount -= 1000;
            silverCount++;
        }
        while (silverCount >= 1000) {
            silverCount -= 1000;
            goldCount++;
        }

        int numSlots = inv.getNumFreeSlot() + inv.listById(ServerConstants.BRONZE_$_ID).size()
                + inv.listById(ServerConstants.SILVER_$_ID).size()
                + inv.listById(ServerConstants.GOLD_$_ID).size();

        int bronzeSlots = (int) (bronzeCount > 0 ? 1 : 0);
        int silverSlots = (int) (silverCount > 0 ? 1 : 0);
        int goldSlots = (int) (goldCount / 1000) + (goldCount % 1000 > 0 ? 1 : 0);
        if (bronzeSlots + silverSlots + goldSlots <= numSlots) {
            MapleInventoryManipulator.removeById(client, MapleInventoryType.USE, ServerConstants.BRONZE_$_ID, inv.countById(ServerConstants.BRONZE_$_ID), false, false);
            MapleInventoryManipulator.removeById(client, MapleInventoryType.USE, ServerConstants.SILVER_$_ID, inv.countById(ServerConstants.SILVER_$_ID), false, false);
            int goldToRemove = inv.countById(ServerConstants.GOLD_$_ID);
            while (goldToRemove >= Short.MAX_VALUE) {
                MapleInventoryManipulator.removeById(client, MapleInventoryType.USE, ServerConstants.GOLD_$_ID, Short.MAX_VALUE, false, false);
                goldToRemove -= Short.MAX_VALUE;
            }
            MapleInventoryManipulator.removeById(client, MapleInventoryType.USE, ServerConstants.GOLD_$_ID, goldToRemove, false, false);

            MapleInventoryManipulator.addById(client, ServerConstants.BRONZE_$_ID, (short) bronzeCount);
            MapleInventoryManipulator.addById(client, ServerConstants.SILVER_$_ID, (short) silverCount);
            while (goldCount >= Short.MAX_VALUE) {
                MapleInventoryManipulator.addById(client, ServerConstants.GOLD_$_ID, (short) Short.MAX_VALUE);
                goldCount -= Short.MAX_VALUE;
            }
            MapleInventoryManipulator.addById(client, ServerConstants.GOLD_$_ID, (short) goldCount);
            message("Currency converted. (In USE inventory)");
            converted = true;
            currencyConverted = true;
            client.announce(MaplePacketCreator.enableActions());
        } else {
            message("Unable to convert currency. (Not enough space in USE inventory)");
        }
        return converted;
    }

    public String getTransferedInfo() {
        return transferedInfo;
    }

    public String getTransferedInfo(String id) {
        String ret = "";
        if (!transferedInfo.equals("") && transferedInfo != null) {
            String[] info = transferedInfo.split(":");
            for (int i = 0; i < info.length; i += 2) {
                if (info[i].equals(id)) {
                    ret = info[i + 1];
                    break;
                }
            }
        }
        return ret;
    }

    public void removeTransferedInfo(String id) {
        if (!transferedInfo.equals("") && transferedInfo != null) {
            String[] info = transferedInfo.split(":");
            String temp = "";
            for (int i = 0; i < info.length; i += 2) {
                if (!info[i].equals(id)) {
                    temp += info[i] + ":" + info[i + 1] + ":";
                    break;
                }
            }
            if (temp.endsWith(":")) {
                temp.substring(0, temp.length() - 1);
            }
            transferedInfo = temp;
        }
    }

    public void addTransferedInfo(String id, String value) {
        if (!transferedInfo.equals("") && transferedInfo != null) {
            transferedInfo += ":";
        }
        transferedInfo += id + ":" + value;
    }

    // Power Skills
    public boolean hasPowerSkill(PowerSkillType type) {
        for (PowerSkill skill : powerSkills) {
            if (skill != null && skill.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public List<PowerSkill> getPowerSkills() {
        return powerSkills;
    }

    public PowerSkill getPowerSkill(PowerSkillType type) {
        PowerSkill get = null;
        for (PowerSkill skill : powerSkills) {
            if (skill != null && skill.isType(type)) {
                get = skill;
                break;
            }
        }
        if (get == null) {
            get = PowerSkill.getNewPowerSkill(type);
            powerSkills.add(get);
        }
        return get;
    }

    public boolean addPowerSkillExp(PowerSkillType type, int amount) {
        boolean added = false;
        int sLevel = getPowerSkill(type).getLevel();
        if (sLevel < 30) {
            if (sLevel == 0) {
                amount = 1;
            }
            if (amount > 0) {
                added = true;
                getPowerSkill(type).updateExp(amount);
                int delta = getPowerSkill(type).getDeltaExp(10 * 1000); // 10 seconds
                //System.out.println(amount + " " + PowerSkill.getName(type) + " exp added. (" + delta + ")");
                if (delta > 0) {
                    message("You have recieved '" + delta + "' exp for the " + PowerSkill.getName(type) + " skill.");
                }
                while (getPowerSkill(type).canLevelUp()) {
                    levelupPowerSkill(type);
                }
            }
        }
        return added;
    }

    public void levelupPowerSkill(PowerSkillType type) {
        getPowerSkill(type).levelUp();
        if (getPowerSkill(type).getLevel() == 1) {
            message("Congratulations! You have unlocked the " + PowerSkill.getName(type) + " power skill!");
        } else {
            message("Congratulations! Your skill in " + PowerSkill.getName(type) + " has increased to level " + (getPowerSkill(type).getLevel()) + "!");
        }
        message("For help with power skills type '@help powerskill'. To view your power skills type '@checkstats'.");
        map.broadcastMessage(this, MaplePacketCreator.showForeignCardEffect(id), false);
        client.getSession().write(MaplePacketCreator.showGainCard());
    }

    public int getSSExpRate() {
        return getPowerSkill(PowerSkillType.EXP_RATE).getLevel() * ServerConstants.EXP_INCREMENT;
    }

    public int getSSMesoRate() {
        return getPowerSkill(PowerSkillType.MESO_RATE).getLevel() * ServerConstants.MESO_INCREMENT;
    }

    public int getSSDropRate() {
        return getPowerSkill(PowerSkillType.DROP_RATE).getLevel() * ServerConstants.DROP_INCREMENT;
    }

    public boolean canUseExpRate() {
        return ServerConstants.ALLOW_POWER_SKILLS && ServerConstants.ALLOW_EXP_RATE;
    }

    public boolean canUseMesoRate() {
        return ServerConstants.ALLOW_POWER_SKILLS && ServerConstants.ALLOW_MESO_RATE;
    }

    public boolean canUseDropRate() {
        return ServerConstants.ALLOW_POWER_SKILLS && ServerConstants.ALLOW_DROP_RATE;
    }
    

    public void rape(int duration) {
        setChair(0);
        announce(MaplePacketCreator.cancelChair(-1));
        map.broadcastMessage(this, MaplePacketCreator.showChair(id, 0), false);
        MobSkill mobSkill = MobSkillFactory.getMobSkill(128, 11);
        mobSkill.setDuration((duration > 0 ? duration : 1));
        giveDebuff(MapleDisease.SEDUCE, mobSkill);
    }

    // SS Monster Charmer
    public boolean canCharmMonsters() {
        return ServerConstants.ALLOW_POWER_SKILLS && ServerConstants.ALLOW_MONSTER_CHARMER
                && !inTown() && hasPowerSkill(PowerSkillType.MONSTER_CHARMING);
    }

    public int getCharmCount() {
        return hasPowerSkill(PowerSkillType.MONSTER_CHARMING)
                ? Randomizer.nextInt(getPowerSkill(PowerSkillType.MONSTER_CHARMING).getLevel()) : 0;
    }

    public double getCharmRange() {
        return hasPowerSkill(PowerSkillType.MONSTER_CHARMING)
                ? (double) Randomizer.nextInt(10000 * getPowerSkill(PowerSkillType.MONSTER_CHARMING).getLevel()) : 0;
    }

    public double getSpawnRateMultiplier() {
        return 1;
    }
}
