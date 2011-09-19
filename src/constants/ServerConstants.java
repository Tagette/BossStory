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
    public static String[] WORLD_NAMES = {"BossStory", "Bera", "Broa", "Windia", "Khaini", "Bellocan", "Mardia", "Kradia", "Yellonde", "Demethos", "Galicia", "El Nido", "Zenith", "Arcenia", "Kastia", "Judis", "Plana", "Kalluna", "Stius", "Croa", "Medere"};;
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
    public static String RECOMMEND_MESSAGE = "Hello";
    //Event Configuration
    public static final boolean PERFECT_PITCH = true;
    public static final String EVENTS = "automsg KerningPQ Boats Subway AirPlane elevator";
    // IP Configuration
    public static final String HOST = "96.53.111.250";
    //Database Configuration
    public static final String DB_URL = "jdbc:mysql://localhost:3306/BossStory?autoReconnect=true";
    public static final String DB_USER = "root";
    public static final String DB_PASS = "";
    
    public static final int REBIRTH_LEVEL = 200;
    public static final int KOC_REBIRTH_LEVEL = 200;
    public static final int MAX_LEVEL = 250;
    
    // Secondary SKills
    
    public static boolean ALLOW_SSKILLS = true;

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
    
    public static final int EXP_POT_ID = 2022157;
    public static final int MESO_POT_ID = 2022158;
    public static final int DROP_POT_ID = 2022159;
    
    public static final int EXP_CARD_ID = 5211000;
    public static final int DROP_CARD_ID = 5360042;
    
    public static final int EXP_INCREMENT = 65;
    public static final int MESO_INCREMENT = 40;
    public static final int DROP_INCREMENT = 10;
}