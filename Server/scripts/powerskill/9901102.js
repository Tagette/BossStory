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
 * @Name:		Tagette
 * @Author:		Tagette
 * @NPC:		9901100
 * @Purpose:	Provides in-game help with everything.
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
		if (status == 0) {			cm.sendSimple("Hello #b#h ##k! I am called #bThe Super Magnet#k. I am a master in the #Magneto#n Secondary Skill.\r\n\r\n" +				"How can I help you?\r\n#b" + 				"#L0#I would like to know more about the Magneto skill.#l\r\n" +				"#L1#I am ready for the test.#l\r\n" +				"#L2#Nevermind.#l");		} else if (status == 1) {			if(selection == 0) {				cm.sendOk("#eSkill Name:#n Magneto\r\n" + 					"#eDescription:#n Picks up multiple items at a distance.\r\n" +					"#eMethod of gaining exp:#n Successfully picking up items at a distance.\r\n" +					"#eTechnical information:#n\r\n\r\n" +					"#ePickup Range#n (15000 - 375000)\r\n= 15000 * level\r\ndisabled @ level 25\r\n\r\n" +					"#eFull Map Range#n\r\nunlocked @ level 25\r\n\r\n" +					"#eAmount Per Pickup#n (15 - 450)\r\n= 15 * level\r\ndisabled @ level 30\r\n\r\n" +					"#eAll Item Pickup#n\r\nunlock @ level 30\r\n\r\n" +					"#eChance of Success#n (5% - 100%)\r\n= 5% * level\r\nmax @ level 20\r\n\r\n" +					"#eChance of Success#n (5% - 100%)\r\n= 5% * level\r\nmax @ level 20\r\n\r\n");				cm.dispose();			} else if(selection == 1) {				cm.sendSimple("#eQuestion 1:#n\r\n\r\n" + 					"What is a rawr?\r\n#b" + 					"#L0#Answer 1#l\r\n" + 					"#L1#Answer 2#l\r\n" + 					"#L2#Answer 3#l\r\n" + 					"#L3#Answer 4#l");				test = true;			} else {				cm.sendOk("Alright come back when you are ready. Remember you can always find me in the treehouse in Henesys.");				cm.dispose();			}		} else if(status == 2) {			if(test) {				cm.sendSimple("#eQuestion 2:#n\r\n\r\n" + 					"What is a rawr?\r\n#b" + 					"#L0#Answer 1#l\r\n" + 					"#L1#Answer 2#l\r\n" + 					"#L2#Answer 3#l\r\n" + 					"#L3#Answer 4#l");			} else {				cm.sendNext("");			}		} else if(status == 3) {			if(test) {				cm.sendSimple("#eQuestion 3:#n\r\n\r\n" + 					"What is a rawr?\r\n#b" + 					"#L0#Answer 1#l\r\n" + 					"#L1#Answer 2#l\r\n" + 					"#L2#Answer 3#l\r\n" + 					"#L3#Answer 4#l");			} else {				cm.sendNext("");			}		} else if(status == 4) {			if(test) {				cm.sendSimple("#eQuestion 4:#n\r\n\r\n" + 					"What is a rawr?\r\n#b" + 					"#L0#Answer 1#l\r\n" + 					"#L1#Answer 2#l\r\n" + 					"#L2#Answer 3#l\r\n" + 					"#L3#Answer 4#l");			} else {				cm.sendNext("");			}		} else if(status == 5) {			if(test) {				cm.sendSimple("#eQuestion 5:#n\r\n\r\n" + 					"What is a rawr?\r\n#b" + 					"#L0#Answer 1#l\r\n" + 					"#L1#Answer 2#l\r\n" + 					"#L2#Answer 3#l\r\n" + 					"#L3#Answer 4#l");			} else {				cm.sendNext("");			}		} else if(status == 6) {			if(test) {				cm.sendOk("Finish!");				cm.dispose();			} else {				cm.sendOk("");				cm.dispose();			}
		}
	}
}