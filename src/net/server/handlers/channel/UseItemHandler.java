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
package net.server.handlers.channel;

import constants.ServerConstants;
import client.IItem;
import client.MapleClient;
import client.MapleDisease;
import client.MapleInventoryType;
import client.powerskills.PowerSkillType;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Matze
 */
public final class UseItemHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().isAlive()) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        slea.readInt();
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        IItem toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemId) {
            if(itemId == ServerConstants.BRONZE_$_ID){
                if((int) (c.getPlayer().getMeso() + 1000000000) > 0) {
                    c.getPlayer().gainMeso(1000000000, true, true, true);
                    remove(c, slot);
                } else {
                    c.getPlayer().message(1, "You do not have enough room in your inventory to convert that into mesos.");
                    c.getPlayer().message("You do not have enough room in your inventory to convert that into mesos.");
                }
                return;
            } else if(itemId == ServerConstants.SILVER_$_ID || itemId == ServerConstants.GOLD_$_ID) {
                if(MapleInventoryManipulator.checkSpace(c, itemId - 1, (short) 1000, "")) {
                    MapleInventoryManipulator.addById(c, itemId - 1, (short) 1000);
                    remove(c, slot);
                    c.announce(MaplePacketCreator.enableActions());
                } else {
                    c.getPlayer().message(1, "You do not have enough room in your inventory to convert that.");
                    c.getPlayer().message("You do not have enough room in your inventory to convert that.");
                }
                return;
            }
            if(itemId == ServerConstants.EXP_POT_ID && c.getPlayer().canUseExpRate()){
            	c.getPlayer().addPowerSkillExp(PowerSkillType.EXP_RATE, Randomizer.nextInt(5) + 1);
                c.getPlayer().setCards();
                remove(c, slot);
                return;
            } else if(itemId == ServerConstants.MESO_POT_ID && c.getPlayer().canUseMesoRate()){
            	c.getPlayer().addPowerSkillExp(PowerSkillType.MESO_RATE, Randomizer.nextInt(5) + 1);
                c.getPlayer().setCards();
                remove(c, slot);
                return;
            } else if(itemId == ServerConstants.DROP_POT_ID && c.getPlayer().canUseDropRate()){
            	c.getPlayer().addPowerSkillExp(PowerSkillType.DROP_RATE, Randomizer.nextInt(5) + 1);
                c.getPlayer().setCards();
                remove(c, slot);
                return;
            }
            if (itemId == 2022178 || itemId == 2022433 || itemId == 2050004) {
                c.getPlayer().dispelDebuffs();
                remove(c, slot);
                return;
            } else if (itemId == 2050003) {
                c.getPlayer().dispelDebuff(MapleDisease.SEAL);
                remove(c, slot);
                return;
            }
            if (isTownScroll(itemId)) {
                if (ii.getItemEffect(toUse.getItemId()).applyTo(c.getPlayer())) {
                    remove(c, slot);
                }
                c.announce(MaplePacketCreator.enableActions());
                return;
            }
            
            remove(c, slot);
            ii.getItemEffect(toUse.getItemId()).applyTo(c.getPlayer());
            c.getPlayer().checkBerserk();
        }
    }

    private void remove(MapleClient c, byte slot) {
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
        c.announce(MaplePacketCreator.enableActions());
    }

    private boolean isTownScroll(int itemId) {
        return itemId >= 2030000 && itemId < 2030021;
    }
}
