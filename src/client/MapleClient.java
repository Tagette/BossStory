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
package client;

import constants.ServerConstants;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import net.MaplePacket;
import tools.DatabaseConnection;
import net.server.Channel;
import net.server.Server;
import net.server.MapleMessengerCharacter;
import net.server.MaplePartyCharacter;
import net.server.PartyOperation;
import net.server.World;
import net.server.guild.MapleGuildCharacter;
import org.apache.mina.core.session.IoSession;
import scripting.npc.NPCConversationManager;
import scripting.npc.NPCScriptManager;
import scripting.quest.QuestActionManager;
import scripting.quest.QuestScriptManager;
import server.MapleTrade;
import server.TimerManager;
import server.maps.HiredMerchant;
import tools.MapleAESOFB;
import tools.MaplePacketCreator;
import tools.HexTool;
import server.MapleMiniGame;
import server.quest.MapleQuest;

public class MapleClient {
    public static final int LOGIN_NOTLOGGEDIN = 0;
    public static final int LOGIN_SERVER_TRANSITION = 1;
    public static final int LOGIN_LOGGEDIN = 2;
    public static final String CLIENT_KEY = "CLIENT";
    private MapleAESOFB send;
    private MapleAESOFB receive;
    private IoSession session;
    private MapleCharacter player;
    private byte channel = 1;
    private int accId = 1;
    private boolean loggedIn = false;
    private boolean serverTransition = false;
    private Calendar birthday = null;
    private Calendar tempban = null;
    private String accountName = "";
    private byte world;
    private long lastPong;
    private int charactersTaken;
    
    // BossStory
    
    private double donations;
    private Timestamp lastLogin;
    
    // End BossStory
    
    private Set<String> macs = new HashSet<String>();
    private Map<String, ScriptEngine> engines = new HashMap<String, ScriptEngine>();
    private ScheduledFuture<?> idleTask = null;
    private short characterSlots = 3;
    private byte loginattempt = 0;
    private String pin = null;
    private int pinattempt = 0;
    private String pic = null;
    private int picattempt = 0;
    private byte greason = 0, gender = -1;

    public MapleClient(MapleAESOFB send, MapleAESOFB receive, IoSession session) {
        this.send = send;
        this.receive = receive;
        this.session = session;   
    }

    public synchronized MapleAESOFB getReceiveCrypto() {
        return receive;
    }

    public synchronized MapleAESOFB getSendCrypto() {
        return send;
    }

    public synchronized IoSession getSession() {
        return session;
    }

    public MapleCharacter getPlayer() {
        return player;
    }

    public void setPlayer(MapleCharacter player) {
        this.player = player;
    }

    public void sendCharList(int server) {
        this.session.write(MaplePacketCreator.getCharList(this, server));
    }

    public List<MapleCharacter> loadCharacters(int serverId) {
        List<MapleCharacter> chars = new ArrayList<MapleCharacter>(6);//6?
        try {
            for (CharNameAndId cni : loadCharactersInternal(serverId)) {
                chars.add(MapleCharacter.loadCharFromDB(cni.id, this, false));
            }
        } catch (Exception e) {
        }
        return chars;
    }

    public List<String> loadCharacterNames(int serverId) {
        List<String> chars = new ArrayList<String>(6);
        for (CharNameAndId cni : loadCharactersInternal(serverId)) {
            chars.add(cni.name);
        }
        return chars;
    }

    private List<CharNameAndId> loadCharactersInternal(int serverId) {
        PreparedStatement ps;
        List<CharNameAndId> chars = new ArrayList<CharNameAndId>(6);
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("SELECT id, name FROM characters WHERE accountId = ? AND world = ?");
            ps.setInt(1, this.getAccID());
            ps.setInt(2, serverId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                chars.add(new CharNameAndId(rs.getString("name"), rs.getInt("id")));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chars;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean hasBannedIP() {
        boolean ret = false;
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT COUNT(*) FROM ipBans WHERE ? LIKE CONCAT(ip, '%')");
            ps.setString(1, session.getRemoteAddress().toString());
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                ret = true;
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
        }
        return ret;
    }

    public boolean hasBannedMac() {
        if (macs.isEmpty()) {
            return false;
        }
        boolean ret = false;
        int i = 0;
        try {
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM macBans WHERE mac IN (");
            for (i = 0; i < macs.size(); i++) {
                sql.append("?");
                if (i != macs.size() - 1) {
                    sql.append(", ");
                }
            }
            sql.append(")");
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql.toString());
            i = 0;
            for (String mac : macs) {
                i++;
                ps.setString(i, mac);
            }
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                ret = true;
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
        }
        return ret;
    }

    private void loadMacsIfNescessary() throws SQLException {
        if (macs.isEmpty()) {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT macs FROM accounts WHERE id = ?");
            ps.setInt(1, accId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                for (String mac : rs.getString("macs").split(", ")) {
                    if (!mac.equals("")) {
                        macs.add(mac);
                    }
                }
            }
            rs.close();
            ps.close();
        }
    }

    public void banMacs() {
        Connection con = DatabaseConnection.getConnection();
        try {
            loadMacsIfNescessary();
            List<String> filtered = new LinkedList<String>();
            PreparedStatement ps = con.prepareStatement("SELECT filter FROM macFilters");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                filtered.add(rs.getString("filter"));
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("INSERT INTO macBans (mac) VALUES (?)");
            for (String mac : macs) {
                boolean matched = false;
                for (String filter : filtered) {
                    if (mac.matches(filter)) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    ps.setString(1, mac);
                    ps.executeUpdate();
                }
            }
            ps.close();
        } catch (SQLException e) {
        }
    }

    public int finishLogin() {
        synchronized (MapleClient.class) {
            if (getLoginState() > LOGIN_NOTLOGGEDIN) {
                loggedIn = false;
                return 7;
            }
            updateLoginState(LOGIN_LOGGEDIN);
        }
        return 0;
    }

    public void setPin(String pin) {
        this.pin = pin;
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET pin = ? WHERE id = ?");
            ps.setString(1, pin);
            ps.setInt(2, accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
        }
    }

    public String getPin() {
        return pin;
    }

    public boolean checkPin(String other) {
        pinattempt++;
        if (pinattempt > 5) {
            getSession().close(true);
        }
        if (pin.equals(other)) {
            pinattempt = 0;
            return true;
        }
        return false;
    }

    public void setPic(String pic) {
        this.pic = pic;
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET pic = ? WHERE id = ?");
            ps.setString(1, pic);
            ps.setInt(2, accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
        }
    }

    public String getPic() {
        return pic;
    }

    public boolean checkPic(String other) {
        picattempt++;
        if (picattempt > 5) {
            getSession().close(true);
        }
        if (pic.equals(other)) {
            picattempt = 0;
            return true;
        }
        return false;
    }

    public int login(String login, String pwd, boolean ipMacBanned) {
        loginattempt++;
        if (loginattempt > 6) {
            getSession().close(true);
        }
        int loginok = 5;
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT id, password, salt, gender, banned, donation, pin, pic, characterSlots, greason, tempBan, charactersTaken FROM accounts WHERE name = ?");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                
                // Check if account is activated.
                PreparedStatement ps2 = con.prepareStatement("SELECT * FROM mailCodes WHERE accountId = ? AND `type` = 'activation'");
                ps2.setInt(1, rs.getInt("id"));
                ResultSet rs2 = ps2.executeQuery();
                if(rs2.next()) {
                    loginok = 16; // Please verify your account through email...
                } else {
                    int banned = rs.getInt("banned");
                    this.accId = rs.getInt("id");
                    pin = rs.getString("pin");
                    pic = rs.getString("pic");
                    gender = rs.getByte("gender");
                    donations = rs.getDouble("donation");
                    characterSlots = rs.getShort("characterSlots");
                    greason = rs.getByte("greason");
                    tempban = getTempBanCalendar(rs);
                    charactersTaken = rs.getInt("charactersTaken");
                    String passhash = rs.getString("password");
                    String salt = rs.getString("salt");
                    if ((banned == 0 && !ipMacBanned) || banned == -1) {
                        PreparedStatement ips = con.prepareStatement("INSERT INTO ipLog (accountId, ip) VALUES (?, ?)");
                        ips.setInt(1, accId);
                        ips.setString(2, session.getRemoteAddress().toString());
                        ips.executeUpdate();
                        ips.close();
                    }
                    ps.close();
                    if (banned == 1) {
                        loginok = 3;
                    } else {
                        if (banned == -1) { // unban
                            int i;
                            try {
                                loadMacsIfNescessary();
                                StringBuilder sql = new StringBuilder("DELETE FROM macBans WHERE mac IN (");
                                for (i = 0; i < macs.size(); i++) {
                                    sql.append("?");
                                    if (i != macs.size() - 1) {
                                        sql.append(", ");
                                    }
                                }
                                sql.append(")");
                                ps = con.prepareStatement(sql.toString());
                                i = 0;
                                for (String mac : macs) {
                                    ps.setString(++i, mac);
                                }
                                ps.executeUpdate();
                                ps.close();
                                ps = con.prepareStatement("DELETE FROM ipBans WHERE ip LIKE CONCAT(?, '%')");
                                ps.setString(1, getSession().getRemoteAddress().toString().split(":")[0]);
                                ps.executeUpdate();
                                ps.close();
                                ps = con.prepareStatement("UPDATE accounts SET banned = 0 WHERE id = ?");
                                ps.setInt(1, accId);
                                ps.executeUpdate();
                                ps.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        if (getLoginState() > LOGIN_NOTLOGGEDIN) { // already loggedin
                            loggedIn = false;
                            loginok = 7;
                        } else if (pwd.equals(passhash)) { // Assuming theres no chance the user entered a hashed password.
                            loginok = 0;
                            // Hash the password.
                            String newPwd = pwd;
                            try {
                                MessageDigest digester = MessageDigest.getInstance("SHA-1");
                                digester.update(pwd.getBytes("UTF-8"), 0, pwd.length());
                                newPwd = HexTool.toString(digester.digest()).replace(" ", "").toLowerCase();
                            } catch(Exception e) {}
                            
                            if(!newPwd.equals(pwd)) {
                                PreparedStatement ps3 = con.prepareStatement("UPDATE accounts SET password = ? WHERE id = ?");
                                ps3.setString(1, newPwd);
                                ps3.setInt(2, accId);
                                ps3.executeUpdate();
                                ps3.close();
                            }
                        } else if(checkHash(passhash, "SHA-1", pwd) || checkHash(passhash, "SHA-512", pwd + salt)) {
                            loginok = 0;
                        } else {
                            loggedIn = false;
                            loginok = 4;
                        }
                    }
                }
                ps2.close();
                rs2.close();
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (loginok == 0) {
            loginattempt = 0;
        }
        return loginok;
    }
    
    public boolean isRegistered() {
        boolean isRegistered = false;
        Connection connection = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT `id` FROM accounts WHERE name=?");
            ps.setString(1, accountName);
            ResultSet rs = ps.executeQuery();
            isRegistered = rs.next();
            rs.close();
            ps.close();
        } catch(SQLException se) {
            se.printStackTrace();
        }
        return isRegistered;
    }
    
    /**
     * 
     * @param login The username the user wishes to register with.
     * @param password The password the user wishes to register with.
     * @return Returns 0 if successful.
     * 0 = Successful registration.
     * 1 = User name to short. ( 3 or more )
     * 2 = Invalid characters. ( A-Z a-z 0-9 _ . )
     * 3 = Name is blacklisted. ( ServerConstants.REGISTER_BLACKLIST )
     */
    public int register(String login, String password) {
        int registerSuccess = 0;
        if(login.length() < 3) {
            return 1;
        }
        Pattern pattern = Pattern.compile("(?:[^A-Za-z\\._]|\\^)");
        Matcher matcher = pattern.matcher(login);
        if(matcher.matches()) {
            return 2;
        }
        for (int i = 0; i < ServerConstants.REGISTER_BLACKLIST.length; i++) {
            if(login.equalsIgnoreCase(ServerConstants.REGISTER_BLACKLIST[i]))
            {
                return 3;
            }
        }
        Connection connection = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO accounts (name, password) VALUES (?, ?)");
            ps.setString(1, accountName);
            
            String newPwd = password;
            try {
                MessageDigest digester = MessageDigest.getInstance("SHA-1");
                digester.update(password.getBytes("UTF-8"), 0, password.length());
                newPwd = HexTool.toString(digester.digest()).replace(" ", "").toLowerCase();
            } catch(Exception e) {}
            
            ps.setString(2, newPwd);
            
            ps.executeUpdate();
            
            ps.close();
        } catch(SQLException se) {
            se.printStackTrace();
        }
        return registerSuccess;
    }

    private Calendar getTempBanCalendar(ResultSet rs) throws SQLException {
	Calendar lTempban = Calendar.getInstance();
	long blubb = rs.getLong("tempBan");
	if (blubb == 0) { // basically if timestamp in db is 0000-00-00
            lTempban.setTimeInMillis(0);
            return lTempban;
	}
	Calendar today = Calendar.getInstance();
	lTempban.setTimeInMillis(rs.getTimestamp("tempBan").getTime());
	if (today.getTimeInMillis() < lTempban.getTimeInMillis()) {
            return lTempban;
	}

        lTempban.setTimeInMillis(0);
	return lTempban;
    }

    public Calendar getTempBanCalendar() {
	return tempban;
    }

    public static long dottedQuadToLong(String dottedQuad) throws RuntimeException {
        String[] quads = dottedQuad.split("\\.");
        if (quads.length != 4) {
            throw new RuntimeException("Invalid IP Address format.");
        }
        long ipAddress = 0;
        for (int i = 0; i < 4; i++) {
            int quad = Integer.parseInt(quads[i]);
            ipAddress += (long) (quad % 256) * (long) Math.pow(256, (double) (4 - i));
        }
        return ipAddress;
    }

    public static String getChannelServerIPFromSubnet(String clientIPAddress, byte channel) {
        long ipAddress = dottedQuadToLong(clientIPAddress);
        Properties subnetInfo = Server.getInstance().getSubnetInfo();
        if (subnetInfo.contains("net.login.subnetcount")) {
            int subnetCount = Integer.parseInt(subnetInfo.getProperty("net.login.subnetcount"));
            for (int i = 0; i < subnetCount; i++) {
                String[] connectionInfo = subnetInfo.getProperty("net.login.subnet." + i).split(":");
                long subnet = dottedQuadToLong(connectionInfo[0]);
                long channelIP = dottedQuadToLong(connectionInfo[1]);
                byte channelNumber = Byte.parseByte(connectionInfo[2]);
                if (((ipAddress & subnet) == (channelIP & subnet)) && (channel == channelNumber)) {
                    return connectionInfo[1];
                }
            }
        }
        return "0.0.0.0";
    }

    public void updateMacs(String macData) {
        for (String mac : macData.split(", ")) {
            macs.add(mac);
        }
        StringBuilder newMacData = new StringBuilder();
        Iterator<String> iter = macs.iterator();
        while (iter.hasNext()) {
            String cur = iter.next();
            newMacData.append(cur);
            if (iter.hasNext()) {
                newMacData.append(", ");
            }
        }
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET macs = ? WHERE id = ?");
            ps.setString(1, newMacData.toString());
            ps.setInt(2, accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setAccID(int id) {
        this.accId = id;
    }

    public int getAccID() {
        return accId;
    }

    public void updateLoginState(int newstate) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedIn = ?, lastLogin = CURRENT_TIMESTAMP() WHERE id = ?");
            ps.setInt(1, newstate);
            ps.setInt(2, getAccID());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (newstate == LOGIN_NOTLOGGEDIN) {
            loggedIn = false;
            serverTransition = false;
        } else {
            serverTransition = (newstate == LOGIN_SERVER_TRANSITION);
            loggedIn = !serverTransition;
        }
    }

    public int getLoginState() {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT loggedIn, lastLogin, UNIX_TIMESTAMP(birthday) as birthday FROM accounts WHERE id = ?");
            ps.setInt(1, getAccID());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                throw new RuntimeException("getLoginState - MapleClient");
            }
            birthday = Calendar.getInstance();
            long blubb = rs.getLong("birthday");
            if (blubb > 0) {
                birthday.setTimeInMillis(blubb * 1000);
            }
            int state = rs.getInt("loggedIn");
            if (state == LOGIN_SERVER_TRANSITION) {
                lastLogin = rs.getTimestamp("lastLogin");
                if (lastLogin.getTime() + 30000 < System.currentTimeMillis()) {
                    state = LOGIN_NOTLOGGEDIN;
                    updateLoginState(LOGIN_NOTLOGGEDIN);
                }
            }
            rs.close();
            ps.close();
            if (state == LOGIN_LOGGEDIN) {
                loggedIn = true;
            } else if (state == LOGIN_SERVER_TRANSITION) {
                ps = con.prepareStatement("UPDATE accounts SET loggedIn = 0 WHERE id = ?");
                ps.setInt(1, getAccID());
                ps.executeUpdate();
                ps.close();
            } else {
                loggedIn = false;
            }
            return state;
        } catch (SQLException e) {
            loggedIn = false;
            e.printStackTrace();
            throw new RuntimeException("login state");
        }
    }

    public boolean checkBirthDate(Calendar date) {
        return date.get(Calendar.YEAR) == birthday.get(Calendar.YEAR) && date.get(Calendar.MONTH) == birthday.get(Calendar.MONTH) && date.get(Calendar.DAY_OF_MONTH) == birthday.get(Calendar.DAY_OF_MONTH);
    }

    public void disconnect() {
        try {
        if (player != null && isLoggedIn()) {
            World worlda = getWorldServer();
            if (player.getTrade() != null) {
                MapleTrade.cancelTrade(player);
            }
            player.saveCooldowns();
            player.unequipPendantOfSpirit();
            MapleMiniGame game = player.getMiniGame();
            if (game != null) {
                player.setMiniGame(null);
                if (game.isOwner(player)) {
                    player.getMap().broadcastMessage(MaplePacketCreator.removeCharBox(player));
                    game.broadcastToVisitor(MaplePacketCreator.getMiniGameClose());
                } else {
                    game.removeVisitor(player);
                }
            }
            player.cancelAllBuffs();
            if (player.getEventInstance() != null) {
                player.getEventInstance().playerDisconnected(player);
            }
            HiredMerchant merchant = player.getHiredMerchant();
            if (merchant != null) {
                if (merchant.isOwner(player)) {
                    merchant.setOpen(true);
                } else {
                    merchant.removeVisitor(player);
                }
                try {
                    merchant.saveItems(false);
                } catch (SQLException ex) {
                    System.out.println("Error while saving Hired Merchant items.");
                }
            }
                if (player.getMessenger() != null) {
                    MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(player);
                    worlda.leaveMessenger(player.getMessenger().getId(), messengerplayer);
                    player.setMessenger(null);
                }
            NPCScriptManager npcsm = NPCScriptManager.getInstance();
            if (npcsm != null) {
                npcsm.dispose(this);
            }
            if (!player.isAlive()) {
                player.setHp(50, true);
            }

            player.setPartyQuest(null);
            player.setMessenger(null);
            player.cancelExpirationTask();
            for (ScheduledFuture<?> sf : player.getTimers())
                sf.cancel(true);

            player.getTimers().clear();
            for (MapleQuestStatus status : player.getStartedQuests()) {
                MapleQuest quest = status.getQuest();
                if (quest.getTimeLimit() > 0) {
                    MapleQuestStatus newStatus = new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
                    newStatus.setForfeited(player.getQuest(quest).getForfeited() + 1);
                    player.updateQuest(newStatus);
                }
            }
            player.saveToDB(true);
            player.getMap().removePlayer(player);
            try {
                if (player.getParty() != null) {
                    MaplePartyCharacter chrp = player.getMPC();
                    chrp.setOnline(false);
                    worlda.updateParty(player.getParty().getId(), PartyOperation.LOG_ONOFF, chrp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (!this.serverTransition && isLoggedIn()) {
                    worlda.loggedOff(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
                } else {
                    worlda.loggedOn(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
                }
                if (player.getGuildId() > 0) {
                    Server.getInstance().setGuildMemberOnline(player.getMGC(), false, (byte) -1);
                    int allianceId = player.getGuild().getAllianceId();
                    if (allianceId > 0) {
                        Server.getInstance().allianceMessage(allianceId, MaplePacketCreator.allianceMemberOnline(player, false), player.getId(), -1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                worlda.removePlayer(player);
                
                player = null;
                Server.getInstance().getLoad(world).get(channel).decrementAndGet();
                session.close(true);
            }
        }
        if (!this.serverTransition && isLoggedIn()) {
            this.updateLoginState(LOGIN_NOTLOGGEDIN);
        }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error while disconnecting character.");
            getWorldServer().removePlayer(player);
            player = null;
            session.close(true);
        }
    }

    public byte getChannel() {
        return channel;
    }

    public Channel getChannelServer() {
        return Server.getInstance().getChannel(world, channel);
    }

    public World getWorldServer() {
        return Server.getInstance().getWorld(world);
    }

    public Channel getChannelServer(byte channel) {
        return Server.getInstance().getChannel(world, channel);
    }

    public boolean deleteCharacter(int cid) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT id, guildId, guildRank, name, allianceRank FROM characters WHERE id = ? AND accountId = ?");
            ps.setInt(1, cid);
            ps.setInt(2, accId);
            ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
	    rs.close();
	    ps.close();
	    return false;
	}
            if (rs.getInt("guildId") > 0) {
                try {
                    Server.getInstance().deleteGuildCharacter(new MapleGuildCharacter(cid, 0, rs.getString("name"), (byte) -1, (byte) -1, 0, rs.getInt("guildRank"), rs.getInt("guildId"), false, rs.getInt("allianceRank")));
                } catch (Exception re) {
                    rs.close();
                    ps.close();
                    return false;
                }
            }
            rs.close();
            ps = con.prepareStatement("DELETE FROM wishLists WHERE charId = ?");
            ps.setInt(1, cid);
            ps.executeUpdate();
            ps = con.prepareStatement("DELETE FROM characters WHERE id = ?");
            ps.setInt(1, cid);
            ps.executeUpdate();
            String[] toDel = {"fameLog", "inventoryItems", "keyMap", "questStatus", "savedLocations", "skillMacros", "skills", "eventStats"};
            for (String s : toDel) {
                ps = con.prepareStatement("DELETE FROM `" + s + "` WHERE characterId = ?");
                ps.setInt(1, cid);
                ps.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String a) {
        this.accountName = a;
    }

    public void setChannel(byte channel) {
        this.channel = channel;
    }

    public byte getWorld() {
        return world;
    }

    public void setWorld(byte world) {
        this.world = world;
    }

    public void pongReceived() {
        lastPong = System.currentTimeMillis();
    }

    public void sendPing() {
        final long then = System.currentTimeMillis();
        announce(MaplePacketCreator.getPing());
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    if (lastPong < then) {
                        if (getSession() != null && getSession().isConnected()) {
                            getSession().close(true);
                        }
                    }
                } catch (NullPointerException e) {
                }
            }
        }, 15000);
    }

    public Set<String> getMacs() {
        return Collections.unmodifiableSet(macs);
    }

    public void setScriptEngine(String name, ScriptEngine e) {
        engines.put(name, e);
    }

    public ScriptEngine getScriptEngine(String name) {
        return engines.get(name);
    }

    public void removeScriptEngine(String name) {
        engines.remove(name);
    }

    public ScheduledFuture<?> getIdleTask() {
        return idleTask;
    }

    public void setIdleTask(ScheduledFuture<?> idleTask) {
        this.idleTask = idleTask;
    }

    public NPCConversationManager getCM() {
        return NPCScriptManager.getInstance().getCM(this);
    }

    public QuestActionManager getQM() {
        return QuestScriptManager.getInstance().getQM(this);
    }

    private static class CharNameAndId {
        public String name;
        public int id;

        public CharNameAndId(String name, int id) {
            super();
            this.name = name;
            this.id = id;
        }
    }

    public static boolean checkHash(String hash, String type, String password) {
        try {
            MessageDigest digester = MessageDigest.getInstance(type);
            digester.update(password.getBytes("UTF-8"), 0, password.length());
            return HexTool.toString(digester.digest()).replace(" ", "").toLowerCase().equals(hash);
        } catch (Exception e) {
            throw new RuntimeException("Encoding the string failed", e);
        }
    }

    public short getCharacterSlots() {
        return characterSlots;
    }

    public boolean gainCharacterSlot() {
        if (characterSlots < 15) {
            Connection con = DatabaseConnection.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE accounts SET characterSlots = ? WHERE id = ?");
                ps.setInt(1, this.characterSlots += 1);
                ps.setInt(2, accId);
                ps.executeUpdate();
                ps.close();  
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        }
            return false;
    }

    public byte getGReason() {
        return greason;
    }

    public byte getGender() {
        return gender;
    }

    public void setGender(byte m) {
        this.gender = m;
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET gender = ? WHERE id = ?");
            ps.setByte(1, gender);
            ps.setInt(2, accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
        }
    }

    public void announce(MaplePacket packet) {
        session.write(packet);
    }

    public boolean isDonator() {
            return donations > 0;
    }

    public double getDonations() {
            return donations;
    }

    public void setDonations(double set) {
            donations = set;
    }

    public Timestamp getLastLogin(){
        return lastLogin;
    }
    
    public boolean hasCharactersTaken() {
        return charactersTaken > 0;
    }
    
    public int getCharactersTaken() {
        return charactersTaken;
    }
    
    public void returnCharacter() throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        ps = con.prepareStatement("UPDATE accounts SET charactersTaken = ? WHERE id = ?");
        ps.setInt(1, charactersTaken - 1);
        ps.setInt(2, accId);
        ps.executeUpdate();
        ps.close();
        charactersTaken--;
    }
    
    public void takeCharacter() throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        ps = con.prepareStatement("UPDATE accounts SET charactersTaken = ? WHERE id = ?");
        ps.setInt(1, charactersTaken + 1);
        ps.setInt(2, accId);
        ps.executeUpdate();
        ps.close();
        charactersTaken++;
    }
}
