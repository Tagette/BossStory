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
 * @Name:		
 * @Author:		
 * @NPC:		
 * @Purpose:	
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
			cm.sendOk("K..");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			var total = cm.getPlayer().getTotalMeso();
			if(total >= 11000000000) {
				cm.sendYesNo("I don't want to talk to my mommy go away ... Oh is that my allowance thanks!\r\nUmm.. I don't know how to open these new cases(pills). #v2022076#\r\n#eCan you show me?#n");
				cm.message("To open the case you need to consume the red pill.");
			} else if(total >= 2000000000 && total < 3000000000 && cm.itemQuantity(2022076) >= 2) { // 2 - 3
				cm.sendYesNo("#eCan you show me how?#n\r\n\r\n#rTo open the case you need to consume the red pill.");
				cm.message("To open the case you need to consume the red pill.");
				cm.dispose();
			} else if(total >= 2000000000 && total < 3000000000 && cm.getMeso() >= 1000000000) { // 2 - 3
				cm.sendYesNo("Woah cool its that easy? How do I put it back into the case(pill)? #eCan you show me?#n");
				cm.message("You can convert mesos in your inventory into pills by clicking the red sort button at the top of the inventory window. 1 Red Pill = 1 Billion Mesos");
			} else if(total >= 3000000000 && total < 4000000000 && cm.getMeso() >= 1000000000) { // 3 - 4
				cm.sendNext("#eCan you show me how?#n\r\n\r\n#rYou can convert mesos in your inventory into pills by clicking the red sort button at the top of the inventory window.\r\n1 Red Pill = 1 Billion Mesos");
				cm.message("You can convert mesos in your inventory into pills by clicking the red sort button at the top of the inventory window. 1 Red Pill = 1 Billion Mesos");
				cm.dispose();
			} else if(total >= 3000000000 && total < 4000000000 && cm.getMeso() < 1000000000) { // 3 - 4
				cm.sendNext("Thats so awesome! I like these new cases(pills)! I'm going to show all my friends!");
				cm.message("You can now leave this stage and resume to the next part of the tutorial.");
				cm.gainItem(2022076, 1);
				cm.dispose();
			} else {
				cm.sendOk("I don't want to talk to my mommy go away ...");
				cm.dispose();
			}
		} else if(status == 1) {
			var total = cm.getPlayer().getTotalMeso();
			if(total >= 11000000000) {
				cm.gainItem(2022076, 1);
				cm.gainItem(2022076, -10);
				cm.dispose();
			} else if(total >= 2000000000 && total < 3000000000 && cm.itemQuantity(2022076) < 2) { // 2 - 3
				cm.gainItem(2022076, 1);
				cm.dispose();
			}
		}
	}
}