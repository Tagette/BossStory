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
package server.maps;

import client.IItem;
import client.MapleCharacter;
import java.awt.Point;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import client.MapleClient;
import client.MapleInventoryType;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import net.server.Channel;
import net.server.Server;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

/**
 *
 * @author XoticStory
 */
public class PlayerNPC extends AbstractMapleMapObject {
    private Map<Byte, Integer> equips = new HashMap<Byte, Integer>();
    private int npcId, face, hair;
    private byte skin;
    private String name = "";
    private int FH, RX0, RX1, CY, dir;

    public PlayerNPC(ResultSet rs) {
        try {
            CY = rs.getInt("cy");
            name = rs.getString("name");
            hair = rs.getInt("hair");
            face = rs.getInt("face");
            skin = rs.getByte("skin");
            FH = rs.getInt("Foothold");
            RX0 = rs.getInt("rx0");
            RX1 = rs.getInt("rx1");
            dir = rs.getInt("dir");
            npcId = rs.getInt("ScriptId");
            setPosition(new Point(rs.getInt("x"), CY));
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT equippos, equipid FROM playernpcs_equip WHERE NpcId = ?");
            ps.setInt(1, rs.getInt("id"));
            ResultSet rs2 = ps.executeQuery();
            while (rs2.next()) {
                equips.put(rs2.getByte("equippos"), rs2.getInt("equipid"));
            }
            rs2.close();
            ps.close();
        } catch (SQLException e) {
        }
    }

    public Map<Byte, Integer> getEquips() {
        return equips;
    }

    public int getId() {
        return npcId;
    }

    public int getFH() {
        return FH;
    }

    public int getRX0() {
        return RX0;
    }

    public int getRX1() {
        return RX1;
    }

    public int getCY() {
        return CY;
    }
    
    public int getDir() {
        return dir;
    }

    public byte getSkin() {
        return skin;
    }

    public String getName() {
        return name;
    }

    public int getFace() {
        return face;
    }

    public int getHair() {
        return hair;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        return;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER_NPC;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.spawnPlayerNPC(this));
        client.getSession().write(MaplePacketCreator.getPlayerNPC(this));
    }
    
    public static boolean createPlayer(String name, int scriptId) {
        MapleCharacter chr = null;
        try {
            int charId = 0;
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM characters WHERE name = ?");
            ps.setInt(1, scriptId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                charId = rs.getInt("id");
                ps.close();
                rs.close();
                chr = MapleCharacter.loadCharFromDB(charId, null, false);
            } else {
                ps.close();
                rs.close();
                return false;
            }
            
        } catch(SQLException se) {
            
        }
        return createPlayer(chr, scriptId);
    }

    public static boolean createPlayer(MapleCharacter chr, int scriptId) {
        boolean ret = true;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM playernpcs WHERE ScriptId = ?");
            ps.setInt(1, scriptId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps = con.prepareStatement(
                        "INSERT INTO playernpcs (name, hair, face, skin, x, cy, dir, map, ScriptId, Foothold, rx0, rx1) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, chr.getName());
                ps.setInt(2, chr.getHair());
                ps.setInt(3, chr.getFace());
                ps.setInt(4, chr.getSkinColor().getId());
                ps.setInt(5, chr.getPosition().x);
                ps.setInt(6, chr.getPosition().y);
                ps.setInt(7, chr.isFacingLeft() ? 1 : 0);
                ps.setInt(8, chr.getMapId());
                ps.setInt(9, scriptId);
                ps.setInt(10, chr.getMap().getFootholds().findBelow(chr.getPosition()).getId());
                ps.setInt(11, chr.getPosition().x + 50);
                ps.setInt(12, chr.getPosition().x - 50);
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                rs.next();
                int npcId = rs.getInt(1);
                ps.close();
                ps = con.prepareStatement("INSERT INTO playernpcs_equip (NpcId, equipid, equippos) VALUES (?, ?, ?)");
                ps.setInt(1, npcId);
                for (IItem equip : chr.getInventory(MapleInventoryType.EQUIPPED)) {
                    int position = Math.abs(equip.getPosition());
                    if ((position < 12 && position > 0)
                            || (position > 100 && position < 112)) {
                        ps.setInt(2, equip.getItemId());
                        ps.setInt(3, equip.getPosition());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
                ps.close();
                rs.close();
                ps = con.prepareStatement("SELECT * FROM playernpcs WHERE ScriptId = ?");
                ps.setInt(1, scriptId);
                rs = ps.executeQuery();
                rs.next();
                PlayerNPC pn = new PlayerNPC(rs);
                for (Channel channel : Server.getInstance().getChannelsFromWorld(chr.getWorld())) {
                    MapleMap m = channel.getMapFactory().getMap(chr.getMapId());
                    m.broadcastMessage(MaplePacketCreator.spawnPlayerNPC(pn));
                    m.broadcastMessage(MaplePacketCreator.getPlayerNPC(pn));
                    m.addMapObject(pn);
                }
            } else {
                ret = false;
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            ret = false;
            e.printStackTrace();
        }
        return ret;
    }
}