package client.groups;

import client.MapleCharacter;


public enum MapleGroups {
    PLAYER(0), // Ordinary Player
    DONATOR(1),
    VIP(2),
    INTERN(10), // 10+ are reserved for GM's
    GM(11),
    SUPER(12),
    ADMIN(13);
    
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
