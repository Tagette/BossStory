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
package client;

public enum MapleJob {
    Aventurer(0),

    Warrior(100),
    Fighter(110), Crusader(111), Hero(112),
    Page(120), White_Knight(121), Paladin(122),
    Spearman(130), Dragon_Knight(131), Dark_Knight(132),

    Magician(200),
    Fire_Poison_Wizard(210), Fire_Poison_Mage(211), Fire_Poison_Arch_Mage(212),
    Ice_Lightning_Wizard(220), Ice_Lightning_Mage(221), Ice_Lightning_Arch_Mage(222),
    Cleric(230), Priest(231), Bishop(232),

    Bowman(300),
    Hunter(310), Ranger(311), Bow_Master(312),
    Crossbowman(320), Sniper(321), Marksman(322),

    Thief(400),
    Assassin(410), Hermit(411), Night_Lord(412),
    Bandit(420), Chief_Bandit(421), Shadower(422),

    Pirate(500),
    Brawler(510), Marauder(511), Buccaneer(512),
    Gun_Slinger(520), Outlaw(521), Corsair(522),

    Mapleleaf_Brigadier(800),
    GM(900), Super_GM(910),

    Noblesse(1000),
    Dawn_Warrior_1(1100), Dawn_Warrior_2(1110), Dawn_Warrior_3(1111), Dawn_Warrior_4(1112),
    Blaze_Wizard_1(1200), Blaze_Wizard_2(1210), Blaze_Wizard_3(1211), Blaze_Wizard_4(1212),
    Wind_Archer_1(1300), Wind_Archer_2(1310), Wind_Archer_3(1311), Wind_Archer_4(1312),
    Night_Walker_1(1400), Night_Walker_2(1410), Night_Walker_3(1411), Night_Walker_4(1412),
    Thunder_Breaker_1(1500), Thunder_Breaker_2(1510), Thunder_Breaker_3(1511), Thunder_Breaker_4(1512),

    Legend(2000),
    Aran_1(2100), Aran_2(2110), Aran_3(2111), Aran_4(2112);

    final int jobid;

    private MapleJob(int id) {
        jobid = id;
    }

    public int getId() {
        return jobid;
    }

    public static MapleJob getById(int id) {
        for (MapleJob l : MapleJob.values()) {
            if (l.getId() == id) {
                return l;
            }
        }
        return null;
    }

    public static MapleJob getBy5ByteEncoding(int encoded) {
        switch (encoded) {
            case 2:
                return Warrior;
            case 4:
                return Magician;
            case 8:
                return Bowman;
            case 16:
                return Thief;
            case 32:
                return Pirate;
            case 1024:
                return Noblesse;
            case 2048:
                return Dawn_Warrior_1;
            case 4096:
                return Blaze_Wizard_1;
            case 8192:
                return Wind_Archer_1;
            case 16384:
                return Night_Walker_1;
            case 32768:
                return Thunder_Breaker_1;
            default:
                return Aventurer;
        }
    }

    public boolean isA(MapleJob basejob) {
        return getId() >= basejob.getId() && getId() / 100 == basejob.getId() / 100;
    }
    
    public String getName(){
        return this.name().replaceAll("_", " ");
    }
}
