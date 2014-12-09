package client.powerskills;

import client.MapleCharacter;
import constants.ServerConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import tools.DatabaseConnection;

/**
 *
 * @author Tristan
 * @date Apr 30, 2012
 */
public class Magneto extends PowerSkill {
    
    protected int vacAmount;
    protected double vacRange;
    protected boolean pickupEquips;

    public Magneto(int exp, int level) {
        super(PowerSkillType.MAGNETO, exp, level);
    }

    @Override
    public void save(MapleCharacter chr) throws SQLException {
        if (getLevel() > 0 || getExp() > 0) {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO powerSkills (characterId, type, exp, level, pickupEquips) VALUES (?, ?, ? ,?, ?)");
            ps.setInt(1, chr.getId());
            ps.setString(2, getType().name());
            ps.setInt(3, getExp());
            ps.setInt(4, getLevel());
            ps.setInt(5, pickupEquips ? 1 : 0);
            ps.execute();
            ps.close();
        }
    }
    
    @Override
    public void load(MapleCharacter chr) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT exp, level, pickupEquips FROM powerSkills WHERE characterId = ? AND type = ?");
        ps.setInt(1, chr.getId());
        ps.setString(2, type.name());
        ResultSet rs = ps.executeQuery();
        if(rs.next()) {
            exp = rs.getInt("exp");
            setLevel(rs.getInt("level"));
            pickupEquips = rs.getInt("pickupEquips") == 1;
        }
    }

    @Override
    public boolean canUse() {
        return ServerConstants.ALLOW_MAGNETO && super.canUse();
    }

    @Override
    public void setLevel(int set) {
        super.setLevel(set);
        useDelay = 9100 - (level * 300);
        vacAmount = (level < 30 ? level * 5 : Short.MAX_VALUE);
        vacRange = (level < 25 ? 15000 * level : Double.POSITIVE_INFINITY);
        pickupEquips = level < 10;
    }
    
    public int getVacAmount() {
        return vacAmount;
    }
    
    public double getVacRange() {
        return vacRange;
    }
    
    public boolean canToggleEquips() {
        return level >= 10;
    }
    
    public void ToggleEquipPickup() {
        pickupEquips = !pickupEquips;
    }
    
    public boolean pickupEquips() {
        return pickupEquips;
    }
}
