package client.groups;

import client.MapleCharacter;


public enum MapleGroup {
    PLAYER(0), // Ordinary Player
    DONATOR(1),
    VIP(2),
    INTERN(10), 
    GM(11),
    SUPER(12),
    ADMIN(13);
    
    private int id = 0;
    
    private MapleGroup(int id){
        this.id = id;
    }
    
    public boolean is(MapleCharacter p){
        return p.gmLevel() == id;
    }
    
    public boolean atleast(MapleCharacter p){
        return p.gmLevel() >= id;
    }
    
    public boolean atMost(MapleCharacter p){
        return p.gmLevel() <= id;
    }
    
    public boolean more(MapleCharacter p){
        return p.gmLevel() > id;
    }
    
    public boolean less(MapleCharacter p){
        return p.gmLevel() < id;
    }
    
    public boolean is(MapleGroup g){
        return id == g.getId();
    }
    
    public boolean atleast(MapleGroup g){
        return id >= g.getId();
    }
    
    public boolean atMost(MapleGroup g){
        return id <= g.getId();
    }
    
    public boolean more(MapleGroup g){
        return id > g.getId();
    }
    
    public boolean less(MapleGroup g){
        return id < g.getId();
    }
    
    public int getId() {
        return id;
    }

    public static MapleGroup getById(int id) {
        for (MapleGroup l : MapleGroup.values()) {
            if (l.getId() == id) {
                return l;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return this.name();
    }
}
