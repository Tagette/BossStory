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
/*
 * @Name:		Politian
 * @Author:		Tagette
 * @NPC:		9901100
 * @Purpose:	The master for the politics secondary skill. Provides help in the tutorial for the sskill.
 */
var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1)
		cm.dispose();
	else {
		if (status >= 0 && mode == 0) {
			// Code on exit...
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		} else if (status == 1) {
				cm.sendNext("#bThe Politics SSkill allows for a player, such as yourself, to perform commands that other players cannot, enter restricted areas, and lead guilds and communities.#k");
		} else if(status == 2) {
	}
}