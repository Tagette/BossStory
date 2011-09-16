/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client.sskills;

/**
 *
 * @author tchambers2
 */
public class SSkill {
    
    private SSkillType type;
    private int exp;
    private int level;
    
    public SSkill(SSkillType type, int exp, int level){
        this.type = type;
        this.exp = exp;
        this.level = level;
    }
    
    public SSkillType getType(){
        return type;
    }
    
    public boolean isType(SSkillType type){
        return this.type.equals(type);
    }
    
    public int getExp(){
        return exp;
    }
    
    public void setExp(int set){
        exp = set;
    }
    
    public int getLevel(){
        return level;
    }
    
    public void setLevel(int set){
        level = set;
    }
    
    public void addExp(int amount){
        exp += amount;
    }
    
    public static String getName(SSkillType type){
        if(type == SSkillType.ALCHEMY)
            return "Alchemy";
        else if(type == SSkillType.BREEDING)
            return "Breeding";
        else if(type == SSkillType.DROP_RATE)
            return "Drop Rate";
        else if(type == SSkillType.ENCHANTMENT)
            return "Enchantment";
        else if(type == SSkillType.EXP_RATE)
            return "EXP Rate";
        else if(type == SSkillType.FARMING)
            return "Farming";
        else if(type == SSkillType.FLASH_JUMP)
            return "Flash Jump";
        else if(type == SSkillType.MAGNETO)
            return "Magneto";
        else if(type == SSkillType.MESO_RATE)
            return "Meso Rate";
        else if(type == SSkillType.MONSTER_CHARMER)
            return "Monster Charmer";
        else if(type == SSkillType.NECROMANCY)
            return "Necromancy";
        else if(type == SSkillType.POLITICS)
            return "Politics";
        else if(type == SSkillType.SKINNING)
            return "Skinning";
        else if(type == SSkillType.THIEVERY)
            return "Theivery";
        return "";
    }
}
