package client.powerskills;

/**
 *
 * @author Tristan
 * @date Apr 30, 2012
 */
public class Farming extends PowerSkill{

    public Farming(int exp, int level) {
        super(PowerSkillType.FARMING, exp, level);
    }

//    @Override
//    public void save(MapleCharacter chr) throws SQLException {
//        if (getLevel() > 0 || getExp() > 0) {
//            Connection con = DatabaseConnection.getConnection();
//            PreparedStatement ps = con.prepareStatement("INSERT INTO powerskills (characterid, type, exp, level) VALUES (?, ?, ? ,?)");
//            ps.setInt(1, chr.getId());
//            ps.setInt(2, getType().ordinal());
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
//        ps.setInt(2, getType().ordinal());
//        ResultSet rs = ps.executeQuery();
//        if(rs.next()) {
//            exp = rs.getInt("exp");
//            level = rs.getInt("level");
//        }
//    }
    
    @Override
    public boolean canUse() {
        return super.canUse();
    }

    @Override
    public void setLevel(int set) {
        super.setLevel(set);
    }
    
}
