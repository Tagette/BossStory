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
        
    }
}
