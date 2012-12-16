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
import client.command.CommandProcessor;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class GeneralchatHandler extends net.AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().canTalk()) {
            c.getPlayer().message(1, "Your chat has been disabled.");
            return;
        }
        String msg = slea.readMapleAsciiString();
        MapleCharacter chr = c.getPlayer();
        String[] sp = msg.split(" ");
        sp[0] = sp[0].toLowerCase().substring(1);
        char heading = msg.charAt(0);
        if (!CommandProcessor.processCommand(c, heading, sp)) {
            if (!chr.isHidden())
                chr.getMap().broadcastMessage(MaplePacketCreator.getChatText(chr.getId(), msg, chr.isGM(), slea.readByte()));
            else
                chr.getMap().broadcastGMMessage(MaplePacketCreator.getChatText(chr.getId(), msg, chr.isGM(), slea.readByte()));
        }
    }
}

