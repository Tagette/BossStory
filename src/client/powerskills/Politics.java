package client.powerskills;

import constants.ServerConstants;

/**
 *
 * @author Tristan
 * @date Apr 30, 2012
 */
public class Politics extends PowerSkill {

    protected int securityLevel;
    
    public Politics(int exp, int level) {
        super(PowerSkillType.POLITICS, exp, level);
        setExp(exp);
        setLevel(level);
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
        return ServerConstants.ALLOW_POLITICS && super.canUse();
    }

    @Override
    public void setLevel(int set) {
        super.setLevel(set);
        securityLevel = (level < 30 ? (int) ((level - 10) / 2) + 1 : 10);
        useDelay = (960 - (level * 30)) * 1000;
    }
    
    public int getSecurityLevel() {
        return securityLevel;
    }
    
    public boolean accessBSHelp() {
        return level >= 20;
    }
    
    public boolean canHostCommunity() {
        return level >= 10;
    }
    
    public boolean canHostPublic() {
        return level >= 20;
    }
    
    public boolean communityLeadership() {
        return level >= 15;
    }
    
    public boolean communityCreation() {
        return level >= 25;
    }
    
    public boolean canStartVoteCommunity() {
        boolean canUse = level >= 10 && canUse();
        if(canUse)
            use();
        return canUse;
    }
    
    public boolean canStartVotePublic() {
        boolean canUse = level >= 20 && canUse();
        if(canUse)
            use();
        return canUse;
    }

    // Politics
    /*
     * 
    - @kill → Kills a specified player.
    - @killall → Kills all players in the same map.
    - @rape → Makes a specified player lay down and unable to move for a short time.
    - @heal → Heals a specified player.
    - @healall → Heals all players in the same map.
    - @curse → Puts a lot of curses on a player.
    - @kick → Kicks a specified player out of the town.
    - @bomb → Creates a bomb on the floor that will explode.
    - @mimic → Changes your clothes and body to mimic what a player looks like.
    - @strip → Strips a player to nothing but thier skin.
    - @slap → Hurts a player leaving only 1 hp.
    - @tax → Takes the mesos currently being held in the inventory except for 1.
     * 
     */
    
    public boolean useHeal() {
        boolean canUse = level >= 2 && canUse();
        if(canUse)
            use();
        return canUse;
    }
    
    public boolean useMimic() {
        boolean canUse = level >= 4 && canUse();
        if(canUse)
            use();
        return canUse;
    }
    
    public boolean useStrip() {
        boolean canUse = level >= 6 && canUse();
        if(canUse)
            use();
        return canUse;
    }
    
    public boolean useCurse() {
        boolean canUse = level >= 8 && canUse();
        if(canUse)
            use();
        return canUse;
    }
    
    public boolean useHealAll() {
        boolean canUse = level >= 10 && canUse();
        if(canUse)
            use();
        return canUse;
    }
    
    public boolean useBomb() {
        boolean canUse = level >= 12 && canUse();
        if(canUse)
            use();
        return canUse;
    }
    
    public boolean useSlap() {
        boolean canUse = level >= 14 && canUse();
        if(canUse)
            use();
        return canUse;
    }
    
    public boolean useKill() {
        boolean canUse = level >= 16 && canUse();
        if(canUse)
            use();
        return canUse;
    }
    
    public int getRapeDuration() {
        return (level >= 18 ? (int)(level / 2) : 0);
    }
    
    public boolean useRape() {
        boolean canUse = level >= 18 && canUse();
        if(canUse)
            use();
        return canUse;
    }
    
    public boolean useKick() {
        boolean canUse = level >= 20 && canUse();
        if(canUse)
            use();
        return canUse;
    }
    
    public boolean useTax() {
        boolean canUse = level >= 22 && canUse();
        if(canUse)
            use();
        return canUse;
    }
    
    public boolean useKillAll() {
        boolean canUse = level >= 24 && canUse();
        if(canUse)
            use();
        return canUse;
    }
    
}
