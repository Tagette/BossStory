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
License.te

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client.command;

import client.MapleCharacter;
import client.MapleClient;
import client.groups.MapleGroups;
import tools.StringUtil;

public class CommandProcessor {

    public static boolean processCommand(MapleClient c, String[] split, char heading) {
        boolean isCommand = true;
        String command = heading + StringUtil.joinStringFrom(split, 0);
        MapleCharacter chr = c.getPlayer();
        switch (heading) {
            case '!':
                if (MapleGroups.INTERN.atleast(chr)) {
                    if (!InternCmd.executeCommand(c, split)) {
                        if (MapleGroups.GM.atleast(chr)) {
                            if (!GMCmd.executeCommand(c, split)) {
                                if (MapleGroups.SUPER.atleast(chr)) {
                                    if (!SuperCmd.executeCommand(c, split)) {
                                        if (MapleGroups.ADMIN.atleast(chr)) {
                                            if (!AdminCmd.executeCommand(c, split)) {
                                                chr.message("Unknown command: '"
                                                        + command + "'");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    isCommand = false;
                }
                break;
            case '/':
                if (!PlayerCmd.executeCommand(c, split)) {
                    if(chr.isVip() && VIPCmd.executeCommand(c, split)){
                    } else if(c.isDonator() && DonatorCmd.executeCommand(c, split)){
                    } else {
                        chr.message("Unknown command: '" + command + "'");
                    }
                }
                break;
            default:
                isCommand = false;
                break;
        }
        return isCommand;
    }

    public static String joinStringFrom(String arr[], int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < arr.length; i++) {
            builder.append(arr[i]);
            if (i != arr.length - 1) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }
}