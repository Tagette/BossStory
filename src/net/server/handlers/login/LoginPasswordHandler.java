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
package net.server.handlers.login;

import client.MapleCharacter;
import client.MapleClient;
import constants.ServerConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import net.MaplePacketHandler;
import server.TimerManager;
import tools.DatabaseConnection;
import tools.DateUtil;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class LoginPasswordHandler implements MaplePacketHandler {
    public boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int loginok = 0;
        String login = slea.readMapleAsciiString();
        String pwd = slea.readMapleAsciiString();
        c.setAccountName(login);
        if(!c.isRegistered() && ServerConstants.AUTO_REGISTER) {
            int registerOk = c.register(login, pwd);
            if(registerOk == 0) {
                System.out.println(login + " has auto registered. (" + c.getSession().getRemoteAddress().toString() + ")");
                c.announce(MaplePacketCreator.serverNotice(1, (byte) 0, "You have registered as " + login + "."));
            } else if(registerOk == 1) {
                c.announce(MaplePacketCreator.serverNotice(1, (byte) 0, "Username must be atleast 3 characters long."));
                return;
            } else if(registerOk == 2) {
                c.announce(MaplePacketCreator.serverNotice(1, (byte) 0, "Username can only contain these characters: A-Z a-z 0-9 _ or ."));
                return;
            }
        }
        final boolean isBanned = c.hasBannedIP() || c.hasBannedMac();
        loginok = c.login(login, pwd, isBanned);
        Calendar tempban = c.getTempBanCalendar();
        if (tempban != null) {
            if (tempban.getTimeInMillis() > System.currentTimeMillis()) {
                long till = DateUtil.getFileTimestamp(tempban.getTimeInMillis());
                c.announce(MaplePacketCreator.getTempBan(till, c.getGReason()));
                return;
            }
        }
        if (loginok == 3 && !isBanned) {
            c.announce(MaplePacketCreator.getTempBan(Integer.MAX_VALUE, c.getGReason()));//Or Long.MAX_VALUE?
            return;
        }
        if (loginok == 0 && isBanned) {
            loginok = 3;
            MapleCharacter.ban(c.getSession().getRemoteAddress().toString().split(":")[0], "Mac/IP Re-ban", false);
        } else if (loginok != 0) {
            c.announce(MaplePacketCreator.getLoginFailed(loginok));
            return;
        }
        if (c.finishLogin() == 0) {
            c.announce(MaplePacketCreator.getAuthSuccess(c));
            if(c.hasCharactersTaken()) {
                c.announce(MaplePacketCreator.serverNotice(1, c.getCharactersTaken() + " character(s) have/has been taken from your account by an admin. The chararacter(s) should be returned within 24 hours."));
            }
            System.out.println(c.getAccountName() + " has logged in. (" + c.getSession().getRemoteAddress() + ")");
            final MapleClient client = c;
            c.setIdleTask(TimerManager.getInstance().schedule(new Runnable() {
                public void run() {
                    client.getSession().close(true);
                }
            }, 600000));
        } else {
            c.announce(MaplePacketCreator.getLoginFailed(7));
        }
    }
}
