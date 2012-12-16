package client.powerskills;

import constants.ServerConstants;
import tools.Randomizer;

/**
 *
 * @author Tristan
 * @date Apr 30, 2012
 */
public class Enchantment extends PowerSkill {

    protected float noSlotUse;
    protected int rerolls;
    protected float extraStats;
    protected float successIncrease;
    protected float multiScrollChance;
    protected int multiScrollAmount;
    protected float doubleScroll;
    protected float equipStatIncrease;
    
    public Enchantment(int exp, int level) {
        super(PowerSkillType.ENCHANTMENT, exp, level);
    }

//    @Override
//    public void save(MapleCharacter chr) throws SQLException {
//        if (getLevel() > 0 || getExp() > 0) {
//            Connection con = DatabaseConnection.getConnection();
//            PreparedStatement ps = con.prepareStatement("INSERT INTO powerskills (characterid, type, exp, level) VALUES (?, ?, ? ,?)");
//            ps.setInt(1, chr.getId());
//            ps.setString(2, getType().name());
//            ps.setInt(3, getExp());
//            ps.setInt(4, getLevel());
//            ps.execute();
//            ps.close();
//        }
//    }
//    
//    @Override
//    public void load(MapleCharacter chr) throws SQLException {
//        Connection con = DatabaseConnection.getConnection();
//        PreparedStatement ps = con.prepareStatement("SELECT exp, level FROM powerskills WHERE characterid = ? AND type = ?");
//        ps.setInt(1, chr.getId());
//        ps.setString(2, type.name());
//        ResultSet rs = ps.executeQuery();
//        if(rs.next()) {
//            exp = rs.getInt("exp");
//            level = rs.getInt("level");
//        }
//    }
    
    @Override
    public boolean canUse() {
        return ServerConstants.ALLOW_ENCHANTMENT;
    }

    @Override
    public void setLevel(int set) {
        super.setLevel(set);
        noSlotUse = level * 0.03f;
        rerolls = (level >= 15 ? (level - 15) : 0);
        extraStats = (level >= 5 ? (level - 5) * 0.02f : 0);
        successIncrease = (level >= 10 ? (level - 10) * 0.045f : 0);
        multiScrollChance = (level >= 10 ? ((level - 10) * 0.04f) + 0.2f : 0);
        multiScrollAmount = (level >= 10 ? (level - 10) : 0);
        doubleScroll = level >= 20 ? 0.25f : 0;
        equipStatIncrease = (level >= 25 ? ((level - 25) * 0.03f) + 0.05f : 0);
    }
    
    public float getNoSlotUse() {
        return noSlotUse;
    }
    
    public int getRerolls() {
        return rerolls > 0 ? Randomizer.rand(0, rerolls) : 0;
    }
    
    public float getExtraStats() {
        return extraStats;
    }
    
    public float getSuccessIncrease() {
        return successIncrease;
    }
    
    public float getMultiScrollChance() {
        return multiScrollChance;
    }
    
    public int getMultiScrollAmount() {
        return multiScrollAmount;
    }
    
    public float getDoubleScroll() {
        return doubleScroll;
    }
    
    public float getEquipStatIncrease() {
        return equipStatIncrease;
    }
    
}
