package client.groups;

import client.MapleCharacter;


public enum MapleGroups {
    PLAYER(0), // Ordinary Player
    INTERN(1), 
    GM(2),
    SUPER(3),
    ADMIN(4);
    
    private int code = 0;
    
    private MapleGroups(int code){
        this.code = code;
    }
    
    public boolean is(MapleCharacter p){
        return p.gmLevel() == code;
    }
    
    public boolean atleast(MapleCharacter p){
        return p.gmLevel() >= code;
    }
    
    public boolean atMost(MapleCharacter p){
        return p.gmLevel() <= code;
    }
    
    public boolean more(MapleCharacter p){
        return p.gmLevel() > code;
    }
    
    public boolean less(MapleCharacter p){
        return p.gmLevel() < code;
    }
}
