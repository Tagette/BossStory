/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package constants;

public class ServerConstants {
    public static short VERSION = 83;
    public static String[] DEFAULT_WORLD_NAMES = {"Scania", "Bera", "Broa", "Windia", "Khaini", "Bellocan", "Mardia", "Kradia", "Yellonde", "Demethos", "Galicia", "El Nido", "Zenith", "Arcenia", "Kastia", "Judis", "Plana", "Kalluna", "Stius", "Croa", "Medere"};
    // Rate Configuration
    public static final byte QUEST_EXP_RATE = 4;
    public static final byte QUEST_MESO_RATE = 3;
    // Login Configuration
    public static final int CHANNEL_LOAD = 150;//Players per channel
    public static final long RANKING_INTERVAL = 3600000;
    public static final boolean ENABLE_PIN = false;
    public static final boolean ENABLE_PIC = true;
    // Channel Configuration
    public static String SERVER_MESSAGE = "";
    public static String RECOMMEND_MESSAGE = "";
    //Event Configuration
    public static final boolean PERFECT_PITCH = true;
    public static final String EVENTS = "automsg KerningPQ Boats Subway AirPlane elevator";
    // IP Configuration
    public static final String HOST = "96.53.111.250";
    //Database Configuration
    public static final String DB_URL = "jdbc:mysql://localhost:3306/BossStory?autoReconnect=true";
    public static final String DB_USER = "root";
    public static final String DB_PASS = "";
    // Misc Configuration    
    public static final boolean SPAWN_MOB_WHEN_EMPTY = true;
    public static final int REBIRTH_LEVEL = 200;
    public static final int KOC_REBIRTH_LEVEL = 200;
    public static final int MAX_LEVEL = 250;
    public static final short MAX_STAT = 32000;
    public static final boolean DROPPABLE_CASH = true;
    public static final boolean DROPPABLE_RESTRICTED = false;
    public static final int BRONZE_$_ID = 2022076;
    public static final int SILVER_$_ID = 2022077;
    public static final int GOLD_$_ID = 2022078;
    public static final int NX100_ID = 4031865;
    public static final int NX250_ID = 4031866;
    
    // Power Skills
    
    public static boolean ALLOW_POWER_SKILLS = true;

    public static boolean ALLOW_POLITICS = true;
    public static boolean ALLOW_SKINNING = true;
    public static boolean ALLOW_MAGNETO = true;
    public static boolean ALLOW_MONSTER_CHARMER = true;
    public static boolean ALLOW_ENCHANTMENT = true;
    public static boolean ALLOW_NECROMANCY = true;
    public static boolean ALLOW_BREEDING = true;
    public static boolean ALLOW_THIEVERY = true;
    public static boolean ALLOW_FARMING = true;
    public static boolean ALLOW_ALCHEMY = true;
    public static boolean ALLOW_FLASH_JUMP = true;
    public static boolean ALLOW_EXP_RATE = true;
    public static boolean ALLOW_MESO_RATE = true;
    public static boolean ALLOW_DROP_RATE = true;
    
    public static final int EXP_POT_ID = 2030008;
    public static final int MESO_POT_ID = 2030009;
    public static final int DROP_POT_ID = 20300010;
    
    public static final int EXP_CARD_ID = 5211000;
    public static final int DROP_CARD_ID = 5360042;
    
    public static final int EXP_INCREMENT = 65;
    public static final int MESO_INCREMENT = 40;
    public static final int DROP_INCREMENT = 10;
    
    public static boolean isSkinningSkill(int id) {
        return id == 0
                || id == 1001004
                || id == 1001005
                || id == 1300003
                || id == 2001004
                || id == 2001005
                || id == 3000001
                || id == 3001005
                || id == 4001334
                || id == 4001344
                || id == 2301005
                || id == 3201003
                || id == 4201005
                || id == 1111003
                || id == 1111004
                || id == 1211002
                || id == 2111006
                || id == 2211003
                || id == 2211006
                || id == 3111003
                || id == 3211006
                || id == 4211002
                || id == 1121008
                || id == 1221009
                || id == 3221007
                || id == 4120005
                || id == 4121007
                || id == 4221001
                || id == 4220005
                || id == 5001003
                || id == 5101003
                || id == 5201006
                || id == 5121007
                || id == 5221007
                || id == 11001002
                || id == 11001003
                || id == 13111001
                || id == 15111004
                || id == 14111005
                || id == 13101005
                || id == 14001004
                || id == 14110004
                || id == 11111002
                || id == 11111004
                || id == 12001003
                || id == 12101002
                || id == 13001003
                || id == 21000002
                || id == 21111001
                || id == 21110007
                || id == 21110008
                || id == 21120009
                || id == 9001001
                || id == 9001006;
    }
}