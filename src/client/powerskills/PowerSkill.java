package client.powerskills;

import client.MapleCharacter;
import constants.ExpTable;
import constants.ServerConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import tools.DatabaseConnection;

/**
 *
 * @author Tagette
 */
public class PowerSkill {

    protected PowerSkillType type;
    protected int exp;
    protected int level;
    protected long lastUse;
    protected int useDelay;
    protected long lastShow;
    protected int deltaExp;

    public PowerSkill(PowerSkillType type, int exp, int level) {
        this.type = type;
        this.exp = exp;
        this.level = level;
    }
    
    public void save(MapleCharacter chr) throws SQLException {
        if (getLevel() > 0 || getExp() > 0) {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO powerSkills (characterId, type, exp, level) VALUES (?, ?, ? ,?)");
            ps.setInt(1, chr.getId());
            ps.setInt(2, getType().ordinal());
            ps.setInt(3, getExp());
            ps.setInt(4, getLevel());
            ps.execute();
            ps.close();
        }
    }
    
    public void load(MapleCharacter chr) throws SQLException {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT exp, level FROM powerSkills WHERE characterId = ? AND type = ?");
            ps.setInt(1, chr.getId());
            ps.setInt(2, getType().ordinal());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                exp = rs.getInt("exp");
                setLevel(rs.getInt("level"));
            }
    }

    public PowerSkillType getType() {
        return type;
    }

    public boolean isType(PowerSkillType type) {
        return this.type.equals(type);
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int set) {
        exp = set;
        deltaExp = 0;
    }

    public void updateExp(int amount) {
        exp += amount;
        if(amount > 0) {
            deltaExp += amount;
        }
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int set) {
        level = set;
    }
    
    public boolean canLevelUp() {
        return exp >= ExpTable.getSkillExpNeededForLevel(level);
    }
    
    public void levelUp() {
        if(exp > ExpTable.getExpNeededForLevel(level + 1)) {
            updateExp(-ExpTable.getExpNeededForLevel(level + 1));
        } else {
            setExp(0);
        }
        setLevel(level + 1);
    }
    
    public long getUseDelay() {
        return useDelay;
    }
    
    public void setUseDelay(int delay) {
        useDelay = delay;
    }
    
    public boolean canUse() {
        return lastUse + useDelay < System.currentTimeMillis() && ServerConstants.ALLOW_POWER_SKILLS;
    }
    
    public void use() {
        lastUse = System.currentTimeMillis();
    }
    
    public int getDeltaExp(int delay) {
        int delta = 0;
        if(System.currentTimeMillis() - lastShow > delay) {
            delta = deltaExp;
            deltaExp = 0;
            lastShow = System.currentTimeMillis();
        }
        return delta;
    }

    public static String getName(PowerSkillType type) {
        String name = type.name().toLowerCase();
        // Makes first letter capital
        name = name.replaceFirst(name.substring(0, 0), name.substring(0, 0).toUpperCase());
        return name;
    }
    
    public static PowerSkill getNewPowerSkill(PowerSkillType type) {
        PowerSkill powerSkill = null;
        switch(type) {
            case ALCHEMY:
                powerSkill = new Alchemy(0, 0);
                break;
            case BREEDING:
                powerSkill = new Breeding(0, 0);
                break;
            case ENCHANTMENT:
                powerSkill = new Enchantment(0, 0);
                break;
            case FARMING:
                powerSkill = new Farming(0, 0);
                break;
            case MAGNETO:
                powerSkill = new Magneto(0, 0);
                break;
            case MONSTER_CHARMING:
                powerSkill = new MonsterCharming(0, 0);
                break;
            case NECROMANCY:
                powerSkill = new Necromancy(0, 0);
                break;
            case POLITICS:
                powerSkill = new Politics(0, 0);
                break;
            case SKINNING:
                powerSkill = new Skinning(0, 0);
                break;
            case THIEVERY:
                powerSkill = new Thievery(0, 0);
                break;
        }
        return powerSkill;
    }
}
