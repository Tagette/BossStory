package client.powerskills;

import client.MapleCharacter;
import constants.ServerConstants;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.server.handlers.channel.AbstractDealDamageHandler.AttackInfo;
import server.life.MapleMonster;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.Pair;

/**
 *
 * @author Tristan
 * @date Apr 30, 2012
 */
public class Skinning extends PowerSkill {
    
    protected boolean attackAllowed;
    protected long lastAttackTime;
    protected float successChance;
    protected int skinAmount;
    protected float koChance;
    protected int demiAmount;
    protected double demiRange;
    protected boolean stackItems;

    public Skinning(int exp, int level) {
        super(PowerSkillType.SKINNING, exp, level);
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
        return attackAllowed() && ServerConstants.ALLOW_SKINNING && Math.random() < successChance && super.canUse();
    }

    @Override
    public void setLevel(int set) {
        super.setLevel(set);
        successChance = Math.max(1.0f, level * 0.03f);
        skinAmount = (int) ((level / 3) + 1);
        koChance = level * 0.005f;
        demiAmount = (level > 19 ? level - 19 : 0);
        demiRange = (level < 30 ? demiAmount * 30000 : Double.POSITIVE_INFINITY);
        stackItems = level >= 20;
        
    }
    
    public boolean attackAllowed() {
        return attackAllowed && lastAttackTime + 500 > System.currentTimeMillis(); // expires after 1 second.
    }
    
    public long lastAttackTime() {
        return lastAttackTime;
    }
    
    public void attack(int skillId) {
        attackAllowed = ServerConstants.isSkinningSkill(skillId) || level >= 25;
//        if(!attackAllowed)
//            System.err.println("Skinning attack " + skillId + " not allowed.");
        if(attackAllowed)
            lastAttackTime = System.currentTimeMillis();
    }
    
    public float getSuccessChance() {
        return successChance;
    }
    
    public int getSkinAmount() {
        return skinAmount;
    }
    
    public float koChance() {
        return koChance;
    }
    
    public boolean bossSkinning() {
        return level == 30;
    }
    
    public boolean allSkill() {
        return level >= 25;
    }
    
    public int demiAmount() {
        return demiAmount;
    }
    
    public double demiRange() {
        return demiRange;
    }
    
    public AttackInfo applyDemi(MapleCharacter player, AttackInfo oldAttack) {
        AttackInfo attack = oldAttack;
        // Get list of monsters in map.
        List<MapleMapObject> monsters =
                player.getMap().getMapObjectsInRange(player.getPosition(), demiRange, Arrays.asList(MapleMapObjectType.MONSTER));
        // Shuffle the monsters.
        Collections.shuffle(monsters);
        // Choose amount to attack.
        List<Integer> damages = attack.allDamage.get((int) (Math.random() * attack.allDamage.size())).getRight();
        int mobCount = 0;
        attack.initialNum = attack.numAttacked;
        for(MapleMapObject mob : monsters) {
            // Stop loop after certain amount of monsters.
            if(mobCount > demiAmount) {
                break;
            }
            // Already damaged.
            if(
                    (((MapleMonster) mob).getController() != null 
                        && ((MapleMonster) mob).getController() != player) 
                    || damagesContain(attack, mob.getObjectId())) {
                continue;
            }
            // Add the amount of monsters to the attack.
            List<Pair<Integer, List<Integer>>> allDamage = attack.allDamage;
            allDamage.add(new Pair<Integer, List<Integer>>(mob.getObjectId(), damages));
            attack.numAttacked++;
            attack.numAttackedAndDamage++;
            mobCount++;
        }
        return attack;
    }
    
    private boolean damagesContain(AttackInfo attack, int oid) {
        boolean contains = false;
        for(Pair<Integer, List<Integer>> pair : attack.allDamage) {
            if(pair.getLeft() == oid) {
                contains = true;
                break;
            }
        }
        return contains;
    }
    
    public boolean stackItems() {
        return stackItems;
    }
}
