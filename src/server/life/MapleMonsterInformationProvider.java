/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package server.life;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import tools.DatabaseConnection;

public class MapleMonsterInformationProvider {
// Author : LightPepsi
    private static final MapleMonsterInformationProvider instance = new MapleMonsterInformationProvider();
    private final Map<Integer, List<MonsterDropEntry>> drops = new HashMap<Integer, List<MonsterDropEntry>>();
    private final List<MonsterGlobalDropEntry> globaldrops = new ArrayList<MonsterGlobalDropEntry>();

    protected MapleMonsterInformationProvider() {
        retrieveGlobal();
    }

    public static MapleMonsterInformationProvider getInstance() {
        return instance;
    }

    public final List<MonsterGlobalDropEntry> getGlobalDrop() {
        return globaldrops;
    }

    private void retrieveGlobal() {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM dropDataGlobal WHERE chance > 0");
            rs = ps.executeQuery();

            while (rs.next()) {
            globaldrops.add(
                new MonsterGlobalDropEntry(
                rs.getInt("itemId"),
                rs.getInt("chance"),
                rs.getInt("continent"),
                rs.getByte("dropType"),
                rs.getInt("minimumQuantity"),
                rs.getInt("maximumQuantity"),
                rs.getShort("questId")));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving drop" + e);
        } finally {
            try {
            if (ps != null) {
                ps.close();
            }
            if (rs != null) {
                rs.close();
            }
            } catch (SQLException ignore) {
            }
        }
    }

    public List<MonsterDropEntry> retrieveDrop(final int monsterId) {
        if (drops.containsKey(monsterId)) {
            return drops.get(monsterId);
        }
        List<MonsterDropEntry> ret = new LinkedList<MonsterDropEntry>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM dropData WHERE dropperId = ?");
            ps.setInt(1, monsterId);
            rs = ps.executeQuery();

            while (rs.next()) {
            ret.add(
                new MonsterDropEntry(
                rs.getInt("itemId"),
                rs.getInt("chance"),
                rs.getInt("minimumQuantity"),
                rs.getInt("maximumQuantity"),
                rs.getShort("questId")));
            }
        } catch (SQLException e) {
            return ret;
        } finally {
            try {
            if (ps != null) {
                ps.close();
            }
            if (rs != null) {
                rs.close();
            }
            } catch (SQLException ignore) {
            return ret;
            }
        }
        drops.put(monsterId, ret);
        return ret;
    }

    public final void clearDrops() {
        drops.clear();
        globaldrops.clear();
        retrieveGlobal();
    }
}