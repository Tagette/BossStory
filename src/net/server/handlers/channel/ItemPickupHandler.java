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

import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MaplePet;
import client.autoban.AutobanFactory;
import client.powerskills.Magneto;
import client.powerskills.PowerSkillType;
import constants.ItemConstants;
import constants.ServerConstants;
import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import net.AbstractMaplePacketHandler;
import net.server.MaplePartyCharacter;
import scripting.item.ItemScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleItemInformationProvider.scriptedItem;
import server.TimerManager;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public final class ItemPickupHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        slea.readInt(); //Timestamp
        slea.readByte();
        Point cpos = slea.readPos();
        int oid = slea.readInt();
        MapleCharacter chr = c.getPlayer();
        MapleMapObject ob = chr.getMap().getMapObject(oid);
        
        if (pickupItem(c, cpos, ob)) {
            // Item Vac
            Magneto magneto = (Magneto) chr.getPowerSkill(PowerSkillType.MAGNETO);
            chr.addPowerSkillExp(PowerSkillType.MAGNETO, 1);
            if (magneto.canUse()) {
                magneto.use();
                List<MapleMapObject> items =
                        chr.getMap().getShuffledMapObjectsInRange(
                        cpos, magneto.getVacAmount(),
                        magneto.getVacRange(), Arrays.asList(MapleMapObjectType.ITEM));
                int pickupInc = 50;
                if(items.size() > 1000) {
                    pickupInc = 10;
                } else if(items.size() > 500) {
                    pickupInc = 25;
                }
                int count = 0;
                for (MapleMapObject item : items) {
                    final Point fcpos = cpos;
                    final MapleMapObject fitem = item;
                    final boolean pickupEquips = magneto.pickupEquips();
                    TimerManager.getInstance().schedule(new Runnable() {

                        @Override
                        public void run() {
                            pickupItem(c, fcpos, fitem, true, pickupEquips);
                        }
                        
                    }, count * pickupInc); // Space the item pickups apart to give a cooler look.
                    count++;
//                    if (item instanceof MapleMapItem) {
//                        ((MapleMapItem) item).setPickedUp(true);
//                    }
                }
                chr.addPowerSkillExp(PowerSkillType.MAGNETO, count);
            } else if(magneto.getLevel() == 0) {
                magneto.use(); // Unlock
            }
        }
        c.announce(MaplePacketCreator.enableActions());
    }
    
    public static boolean pickupItem(MapleClient c, Point cpos, MapleMapObject ob) {
        return pickupItem(c, cpos, ob, false, true);
    }

    public static boolean pickupItem(MapleClient c, Point cpos, MapleMapObject ob, boolean skillVac, boolean pickupEquips) {
        if(c == null || c.getPlayer() == null)
            return false;
        MapleCharacter chr = c.getPlayer();
        if (chr.getInventory(MapleItemInformationProvider.getInstance().getInventoryType(ob.getObjectId())).getNextFreeSlot() > -1) {
            int itemid = ((MapleMapItem) ob).getItemId();
            if (chr.getMapId() > 209000000 && chr.getMapId() < 209000016) {//happyville trees
                MapleMapItem mapitem = (MapleMapItem) ob;
                if (mapitem.getDropper().getObjectId() == c.getPlayer().getObjectId()) {
                    if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), false)) {
                        chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
                        chr.getMap().removeMapObject(ob);
                    } else {
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                    mapitem.setPickedUp(true);
                } else {
                    System.out.println("That item is not yours.");
                    c.announce(MaplePacketCreator.getInventoryFull());
                    c.announce(MaplePacketCreator.getShowInventoryFull());
                    return false;
                }
                c.announce(MaplePacketCreator.enableActions());
                return true;
            }
            if (ob == null) {
                System.out.println("MapleMapItem was null.");
                c.announce(MaplePacketCreator.getInventoryFull());
                c.announce(MaplePacketCreator.getShowInventoryFull());
                return false;
            }
            if (ob instanceof MapleMapItem) {
                MapleMapItem mapitem = (MapleMapItem) ob;
                synchronized (mapitem) {
                    // Stop item vac from sucking items that were dropped by a player.
                    // Stop equips from flooding the players equip inventory.
                    if(mapitem.isPlayerDrop() && skillVac || (!pickupEquips && mapitem.getMeso() <= 0 && MapleItemInformationProvider.getInstance().getInventoryType(itemid) == MapleInventoryType.EQUIP)){
                        System.out.println("Don't pickup equips in vac.");
//                        c.announce(MaplePacketCreator.showItemUnavailable());
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                    // Create Pet on Pickup
                    if(ItemConstants.isPet(mapitem.getItemId())){
                        int petId = MaplePet.createPet(mapitem.getItemId());
                        MapleInventoryManipulator.addById(c, mapitem.getItemId(), (short) 1, null, petId);
                        return true;
                    }
                    if (mapitem.getQuest() > 0 && !chr.needQuestItem(mapitem.getQuest(), mapitem.getItemId())) {
                        System.out.println("Removed quest item on pickup.");
                        chr.getMap().removeMapObject(ob);
                        c.announce(MaplePacketCreator.showItemUnavailable());
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                    if (mapitem.isPickedUp()) {
                        System.out.println("Already picked up.");
                        c.announce(MaplePacketCreator.getInventoryFull());
                        c.announce(MaplePacketCreator.getShowInventoryFull());
                        return false;
                    }
                    final double Distance = cpos.distanceSq(mapitem.getPosition());
                    if (Distance > 2500 && !skillVac) {
                        AutobanFactory.SHORT_ITEM_VAC.autoban(chr, cpos.toString() + Distance);
                    } else if (chr.getPosition().distanceSq(mapitem.getPosition()) > 90000.0 && !skillVac) {
                        AutobanFactory.ITEM_VAC.autoban(chr, cpos.toString() + Distance);
                    }
                    if (mapitem.getMeso() > 0) {
                        if (chr.getParty() != null) {
                            int mesosamm = mapitem.getMeso();
                            if (mesosamm > 50000 * chr.getTotalMesoRate()) {
                                System.out.println("Meso is greater than max mesos.");
                                return false;
                            }
                            int partynum = 0;
                            for (MaplePartyCharacter partymem : chr.getParty().getMembers()) {
                                if (partymem.isOnline() && partymem.getMapId() == chr.getMap().getId() && partymem.getChannel() == c.getChannel()) {
                                    partynum++;
                                }
                            }
                            for (MaplePartyCharacter partymem : chr.getParty().getMembers()) {
                                if (partymem.isOnline() && partymem.getMapId() == chr.getMap().getId()) {
                                    MapleCharacter somecharacter = c.getChannelServer().getPlayerStorage().getCharacterById(partymem.getId());
                                    if (somecharacter != null) {
                                        somecharacter.gainMeso(mesosamm / partynum, true, true, false);
                                    }
                                }
                            }
                        } else {
                            chr.gainMeso(mapitem.getMeso(), true, true, false);
                        }
                    } else if(mapitem.getItemId() == ServerConstants.NX100_ID) {
                        chr.getCashShop().gainCash(1, 100 * c.getWorldServer().getNxRate());
                        chr.message("You have gained " + (100 * c.getWorldServer().getNxRate()) + " NX!");
                    } else if(mapitem.getItemId() == ServerConstants.NX250_ID) {
                        chr.getCashShop().gainCash(1, 250 * c.getWorldServer().getNxRate());
                        chr.message("You have gained " + (250 * c.getWorldServer().getNxRate()) + " NX!");
                    } else if (mapitem.getItemId() / 10000 == 243) {
                        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                        scriptedItem info = ii.getScriptedItemInfo(mapitem.getItem().getItemId());
                        if (info.runOnPickup()) {
                            ItemScriptManager ism = ItemScriptManager.getInstance();
                            String scriptName = info.getScript();
                            if (ism.scriptExists(scriptName)) {
                                ism.getItemScript(c, scriptName);
                            }

                        } else {
                            if (!MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true)) {
                                System.err.println("Could not run item pickup script.");
                                c.announce(MaplePacketCreator.enableActions());
                                return false;
                            }
                        }
                    } else if (useItem(c, mapitem.getItem().getItemId())) {
                        if (mapitem.getItem().getItemId() / 10000 == 238) {
                            chr.getMonsterBook().addCard(c, mapitem.getItem().getItemId());
                        }
                    } else if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true)) {
                    } else if (mapitem.getItem().getItemId() == 4031868) {
                        chr.getMap().broadcastMessage(MaplePacketCreator.updateAriantPQRanking(chr.getName(), chr.getItemQuantity(4031868, false), false));
                    } else {
                        System.err.println("Ran out of options.");
                        c.announce(MaplePacketCreator.enableActions());
                        return false;
                    }
                    mapitem.setPickedUp(true);
                    chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
                    chr.getMap().removeMapObject(ob);
                }
            }
        }
        return true;
    }

    static boolean useItem(final MapleClient c, final int id) {
        if (id / 1000000 == 2) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (ii.isConsumeOnPickup(id)) {
                if (id > 2022430 && id < 2022434) {
                    for (MapleCharacter mc : c.getPlayer().getMap().getCharacters()) {
                        if (mc.getParty() == c.getPlayer().getParty()) {
                            ii.getItemEffect(id).applyTo(mc);
                        }
                    }
                } else {
                    ii.getItemEffect(id).applyTo(c.getPlayer());
                }
                return true;
            }
        }
        return false;
    }
}
