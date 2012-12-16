package client;

import client.groups.MapleGroup;
import client.powerskills.PowerSkill;
import client.powerskills.PowerSkillType;
import constants.ExpTable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.server.Server;
import tools.DatabaseConnection;

public class CharInfo {
    private String name, map, partnerName, guild, guildRank, previousNames, lastLogin, createDate;
    private int accountId, charId, level, totalLevel, 
            rebirths, meso, job, fame, apPerLevel, 
            storedAp, gender, gm, buddies;
    private List<PowerSkill> powerskills = new ArrayList<PowerSkill>();
    private int expTotalRate, mesoTotalRate, dropTotalRate;
    private int expRate, mesoRate, dropRate;
    private int expCards, mesoCards, dropCards;
    private PowerSkillType focusedSkillType;
    private boolean canTalk, online, banned;
    private double donation;
    private int votePoints, nxCash;
    private long wealth;

    public boolean loadStats(String name){
        boolean found = false, success = false;
        MapleCharacter player = Server.getInstance().getWorld(0).getPlayerStorage().getCharacterByName(name);
        if(player != null){
            loadPlayerStats(player.getClient());
            success = true;
        } else
            success = loadPlayerStatsFromDb(name);
        return success;
    }

    private void loadPlayerStats(MapleClient c){
        MapleCharacter player = c.getPlayer();
        online = true;
        accountId = c.getAccID();
        charId = player.getId();
        name = player.getName();
        level = player.getLevel();
        totalLevel = player.getTotalLevel();
        rebirths = player.getRebirths();
        meso = player.getMeso();
        player.recalcWealth();
        wealth = player.getWealth();
        job = player.getJob().getId();
        fame = player.getFame();
        apPerLevel = player.getApPerLevel();
        storedAp = player.getStoredAp();
        gender = player.getGender();
        map = player.getMap().getMapName();
        gm = player.gmLevel();
        if(player.getMarriageRing() != null) {
            partnerName = player.getMarriageRing().getPartnerName();
        } else {
            partnerName = "None";
        }
        previousNames = player.getReadablePreviousNames();
        if(player.getGuild() != null){
            guild = player.getGuild().getName();
            guildRank = player.getGuild().getRankTitle(player.getGuildRank())+" ("+player.getGuildRank()+")";
        } else {
            guild = "None";
            guildRank = "None";
        }
        buddies = player.getBuddylist().getBuddies().size();
        lastLogin = convertLastLogin(c.getLastLogin());
        createDate = convertCreateDate(player.getCreateDate());
        donation = c.getDonations();
        votePoints = player.getVotePoints();
        nxCash = player.getCashShop().getCash(1);
        banned = c.hasBannedIP() || c.hasBannedMac();
        canTalk = player.canTalk();
        
        powerskills = player.getPowerSkills();
        
        expTotalRate = player.getTotalExpRate();
        mesoTotalRate = player.getTotalMesoRate();
        dropTotalRate = player.getTotalDropRate();
        expRate = player.getExpRate();
        mesoRate = player.getMesoRate();
        dropRate = player.getDropRate();
        expCards = player.getExpCards();
        mesoCards = player.getMesoCards();
        dropCards = player.getDropCards();
    }

    private boolean loadPlayerStatsFromDb(String cname){
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps, ps2;
        ResultSet rs, rs2;
        boolean ret = true;
        online = false;
        try {
            ps = con.prepareStatement("SELECT * FROM characters WHERE name = ?");
            ps.setString(1, cname);
            rs = ps.executeQuery();
            if(rs.next()){
                charId = rs.getInt("id");
                accountId = rs.getInt("accountid");
                name = rs.getString("name");
                level = rs.getInt("level");
                totalLevel = rs.getInt("totallevel");
                rebirths = rs.getInt("rebirths");
                meso = rs.getInt("meso");
                wealth = rs.getLong("wealth");
                job = rs.getInt("job");
                fame = rs.getInt("fame");
                apPerLevel = rs.getInt("apperlevel");
                storedAp = rs.getInt("storedAp");
                gender = rs.getInt("gender");
                map = Server.getInstance().getChannel((byte)0, (byte)1).getMapFactory().getMap(rs.getInt("map")).getMapName();
                gm = rs.getInt("gm");
                canTalk = rs.getInt("cantalk") == 1;
                expRate = rs.getInt("exprate");
                mesoRate = rs.getInt("mesorate");
                dropRate = rs.getInt("droprate");
                expCards = rs.getInt("lastexpcards");
                mesoCards = rs.getInt("lastmesocards");
                dropCards = rs.getInt("lastdropcards");
                int world = rs.getInt("world");
                expTotalRate = (expRate + Server.getInstance().getWorld(world).getExpRate()) * expCards;
                mesoTotalRate = (mesoRate + Server.getInstance().getWorld(world).getMesoRate()) * mesoCards;
                dropTotalRate = (dropRate + Server.getInstance().getWorld(world).getDropRate()) * dropCards;

                //if(rs.getInt("married") == 1){
                //    int partnerId = rs.getInt("partnerid");
                //    ps2 = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
                //    ps2.setInt(1, partnerId);
                //    rs2 = ps2.executeQuery();
                //    partnerName = rs2.getString("name");
                //    rs2.close();
                //    ps2.close();
                //} else
                partnerName = "None";
                if(rs.getString("previousnames") != null) {
                    previousNames = makeReadablePreviousNames(rs.getString("previousnames"));
                } else {
                    previousNames = "None";
                }
                int guildId = rs.getInt("guildid");
                int guildRankId = rs.getInt("guildrank");
                ps2 = DatabaseConnection.getConnection().prepareStatement("SELECT name, rank1title, rank2title, rank3title, rank4title, rank5title FROM guilds WHERE guildid = ?");
                ps2.setInt(1, guildId);
                rs2 = ps2.executeQuery();
                if(rs2.next()){
                    guild = rs2.getString("name");
                    switch(guildRankId){
                        case 1:
                            guildRank = rs2.getString("rank1title")+" ("+guildRankId+")";
                            break;
                        case 2:
                            guildRank = rs2.getString("rank2title")+" ("+guildRankId+")";
                            break;
                        case 3:
                            guildRank = rs2.getString("rank3title")+" ("+guildRankId+")";
                            break;
                        case 4:
                            guildRank = rs2.getString("rank4title")+" ("+guildRankId+")";
                            break;
                        case 5:
                            guildRank = rs2.getString("rank5title")+" ("+guildRankId+")";
                            break;
                        default:
                            guildRank = "Unknown";
                    }
                } else {
                    guild = "None";
                    guildRank = "None";
                }
                rs2.close();
                ps2.close();

                ps2 = con.prepareStatement("SELECT COUNT(*) FROM buddies WHERE characterid = ?");
                ps2.setInt(1, charId);
                rs2 = ps2.executeQuery();
                if(rs2.next())
                    buddies = rs2.getInt(1);
                else
                    buddies = 0;
                rs2.close();
                ps2.close();

                ps2 = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
                ps2.setInt(1, accountId);
                rs2 = ps2.executeQuery();
                if(rs2.next()) {
                    Timestamp ts = rs2.getTimestamp("lastlogin");
                    lastLogin = convertLastLogin(ts);
                    //lastLogin = null;
                    ts = rs2.getTimestamp("createdat");
                    createDate = convertCreateDate(ts);
                    //createDate = null;
                    donation = rs2.getInt("donation");
                    nxCash = rs2.getInt("nxCredit");
                    banned = rs2.getInt("banned") == 1;
                    votePoints = rs2.getInt("votepoints");
                }
                rs2.close();
                ps2.close();
                
                ps2 = con.prepareStatement("SELECT type, exp, level FROM powerskills WHERE characterid = ?");
                ps2.setInt(1, charId);
                rs2 = ps2.executeQuery();

                while (rs.next()) {
                    PowerSkillType sType = PowerSkillType.valueOf(rs2.getString("type"));
                    int sExp = rs2.getInt("exp");
                    int sLevel = rs2.getInt("level");
                    powerskills.add(new PowerSkill(sType, sExp, sLevel));
                }

                rs.close();
                ps.close();
                
            } else {
                ret = false;
            }
        } catch(SQLException se){
            System.out.println("CharInfo: " + se.toString());
            se.printStackTrace();
            ret = false;
        }
        return ret;
    }

    private String convertLastLogin(Timestamp ts){
        String newLastLogin = "";
        if(ts == null)
            return "Unknown";
        Date today = new Date();
        Timestamp nts = new Timestamp(today.getTime());
        int year = ts.getYear() - nts.getYear();
        int month = ts.getMonth() - nts.getMonth();
        int day = ts.getDay() - nts.getDay();
        int hour = ts.getHours() - nts.getHours();
        int min = ts.getMinutes() - nts.getMinutes();
        if(month < 0)
            month += 12;
        while(day < 0)
            day++;
        if(hour < 0)
            hour += 24;
        if(min < 0)
            min += 60;
        ArrayList temp = new ArrayList();
        if(year == 1)
            temp.add("1 year");
        else if(year > 1)
            temp.add(year+" years");
        if(month == 1)
            temp.add("1 month");
        else if(month > 1)
            temp.add(month+" months");
        if(day == 1)
            temp.add("1 day");
        else if(day > 1)
            temp.add(day+" days");
        if(hour == 1)
            temp.add("1 hour");
        else if(hour > 1)
            temp.add(hour+" hours");
        if(min == 1)
            temp.add("and 1 minute.");
        else if(min > 1)
            temp.add("and "+min+" minutes.");
        for(int a=0; a<temp.size(); a++)
            newLastLogin += temp.get(a);
        return newLastLogin;
    }

    private String convertCreateDate(Timestamp ts){
        String newCreateDate = "";
        if(ts == null)
            return "Unknown";
        int year = ts.getYear() + 1900;
        int month = ts.getMonth() + 1;
        int day = ts.getDay();
        int hour = ts.getHours();
        int min = ts.getMinutes();
        newCreateDate = year+"/"+month+"/"+day+" "+hour+":";
        if(String.valueOf(min).length() == 1)
            newCreateDate += "0"+min;
        else
            newCreateDate += min;
        return newCreateDate;
    }

    private String makeReadablePreviousNames(String names){
        return names.replaceAll(",", ", ");
    }

    public String getCName(){
        return name;
    }

    public String getMapName(){
        return map;
    }

    public String getPartnerName(){
        return partnerName;
    }

    public String getGuildName(){
        return guild;
    }

    public String getGuildRank(){
        return guildRank;
    }

    public String getPreviousNames(){
        return previousNames;
    }

    public String getLastLogin(){
        return lastLogin;
    }

    public String getCreateDate(){
        return createDate;
    }

    public int getAccountId(){
        return accountId;
    }

    public int getCharId(){
        return charId;
    }

    public int getLevel(){
        return level;
    }

    public int getTotalLevel(){
        return totalLevel;
    }

    public int getRebirths(){
        return rebirths;
    }

    public int getMeso(){
        return meso;
    }

    public String getJobName(){
        return MapleJob.getById(job).getName();
    }

    public int getFame(){
        return fame;
    }

    public int getApPerLevel(){
        return apPerLevel;
    }

    public int getStoredAp(){
        return storedAp;
    }

    public String getGender(){
        return gender == 0 ? "Male" : "Female";
    }

    public String getGmTitle(){
        return MapleGroup.getById(gm).name()  + " (" + gm + ")";
    }

    public int getBuddies(){
        return buddies;
    }

    public boolean canTalk(){
        return canTalk;
    }

    public boolean isOnline(){
        return online;
    }

    public boolean isBanned(){
        return banned;
    }

    public double getDonation(){
        return donation;
    }
    
    public int getVotePoints() {
        return votePoints;
    }
    
    public int getNxCash() {
        return nxCash;
    }

    public long getWealth(){
        return wealth;
    }
    
    public void setSkillFocus(PowerSkillType type){
        focusedSkillType = type;
    }
    
    private PowerSkill getSkill() {
        PowerSkill ret = null;
        for(PowerSkill s : powerskills) {
            if(s != null && s.getType() == focusedSkillType) {
                ret = s;
                break;
            }
        }
        return ret;
    }
    
    public boolean hasSkill() {
        return getSkill() != null;
    }
    
    public int getSkillExp() {
        return hasSkill() ? getSkill().getExp() : 0;
    }
    
    public int getSkillLevel() {
        return hasSkill() ? getSkill().getLevel() : 0;
    }
    
    public int getSkillNextExp() {
        return ExpTable.getSkillExpNeededForLevel(getSkill().getLevel() + 1);
    }
    
    public int getTotalExpRate() {
        return expTotalRate;
    }
    
    public int getTotalMesoRate() {
        return mesoTotalRate;
    }
    
    public int getTotalDropRate() {
        return dropTotalRate;
    }
    
    public int getExpRate() {
        return expRate;
    }
    
    public int getMesoRate() {
        return mesoRate;
    }
    
    public int getDropRate() {
        return dropRate;
    }
    
    public int getExpCards() {
        return expCards;
    }
    
    public int getMesoCards() {
        return mesoCards;
    }
    
    public int getDropCards() {
        return dropCards;
    }
}
