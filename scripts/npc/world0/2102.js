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
			if(total >= 1000000000 && total < 2000000000) { // 1 - 2 pills
				cm.sendYesNo("Oh another person doing the tutorial. My name is #bNina#k, could I ask you a favor? I need to bring my son his allowance but he is in a grumpy mood right now and doesn't want to see me. I was wondering if you could #ebring his allowance to him for me.#n");
			} else if(total >= 11000000000 || total < 4000000000) { // 10 pills
				cm.sendOk("Please bring him his allowance.");
				cm.dispose();
			} else if(total >= 4000000000 && total < 5000000000) {
				cm.sendOk("Thanks for doing that for me! Heres a gift for you.");
				cm.gainItem(2022076, 1);
				cm.gainItem(2022308, 1);
				cm.dispose();
			} else {
				cm.sendOk("Nice to meet you.");
				cm.dispose();
			}
		} else if(status == 1) {
			cm.sendOk("Thank you!");
			cm.gainItem(2022076, 10);
			cm.dispose();
		}
	}
}