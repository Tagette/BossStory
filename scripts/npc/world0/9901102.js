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
 * @Name:		Magneto
 * @Author:	Tagette
 * @NPC:		9901102
 * @Purpose:	The master for the Magneto skill.
 */var status = 0;var q1 = new Array(	// ---	"What does the #rMagneto#k skill do?\r\n#b#e" + 	"#L0#Pulls monsters towards you.#l\r\n" + 	"#L1#Charges an item that posesses magnet power.#l\r\n" + 	"#L2#Picks up items at a distance.#l\r\n" + 	"#L3#Allows you to hang your computer on your fridge.#l", 	// ---	"What skill involves picking up items from a distance?\r\n#b#e" + 	"#L0#Magneto.#l\r\n" + 	"#L1#Skinning.#l\r\n" + 	"#L2#Politics.#l\r\n" + 	"#L3#Enchanter.#l", 	// ---	"When does the #rMagneto#k skill gain exp?\r\n#b#e" + 	"#L0#When damaging a monster.#l\r\n" + 	"#L1#When spamming the loot key.#l\r\n" + 	"#L2#When an item is picked up at range.#l\r\n" + 	"#L3#When you say 'I NEDSZ MAGNETO!!1!'.#l");var a1 = new Array(2, 0, 2);var q2 = new Array(	// ---	"Using the #rMagneto#k skill, at what skill level is it no longer possible to destroy items?\r\n#b#e" + 	"#L0#10#l\r\n" + 	"#L1#30#l\r\n" + 	"#L2#25#l\r\n" + 	"#L3#5#l",	// ---	"What is the only requirement to use the #rMagneto#k skill?\r\n#b#e" + 	"#L0#Must have a claw equipped.#l\r\n" + 	"#L1#Must be atleast level 20.#l\r\n" + 	"#L2#Must have 1 trillion mesos.#l\r\n" + 	"#L3#Must have no equipped weapon.#l",	// ---	"Using the #rMagneto#k skill, at what skill level can you pickup all of the items on the map at once?\r\n#b#e" + 	"#L0#25#l\r\n" + 	"#L1#30#l\r\n" + 	"#L2#20#l\r\n" + 	"#L3#28#l");var a2 = new Array(0, 3, 1);var chosen = new Array();
function start() {	chosen[0] = Math.floor(Math.random() * q1.length);	chosen[1] = Math.floor(Math.random() * q2.length);
	status = -1;
	action(1, 0, 0);
}
function action(mode, type, selection) {
	if (mode == -1)
		cm.dispose();
	else {
		if (status >= 0 && mode == 0) {			cm.sendOk("Alright come back when you are ready. Remember you can always find me in the treehouse in Henesys.");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {			if(!cm.haveItem(4031674)) {				cm.sendSimple("Hello #b#h ##k! I am called #bThe Super Magnet#k. I am a master in the #eMagneto#n Secondary Skill.\r\n\r\n" +					"How can I help you?\r\n#b#e" + 					"#L0#I would like to know more about the Magneto skill.#l\r\n" +					"#L1#I am ready for the test.#l\r\n" +					"#L2#Nevermind.#l");			} else {				cm.sendSimple("Hello #b#h ##k! I am called #bThe Super Magnet#k. I am a master in the #eMagneto#n Secondary Skill.\r\n\r\n" +					"How can I help you?\r\n#b#e" + 					"#L0#I would like to know more about the Magneto skill.#l\r\n" +					"#L2#Nevermind.#l");			}		} else if (status == 1) {			if(selection == 0) {				cm.sendOk("#eSkill Name:#n Magneto\r\n" + 					"#eDescription:#n Picks up multiple items at a distance.\r\n" +					"#eMethod of gaining exp:#n Successfully picking up items at a distance.\r\n" +					"#eRequirements to Use:#n No weapon equipped.\r\n" +					"#eTechnical information:#n\r\n\r\n" +					"#ePickup Range#n (15000 - 375000)\r\n= 15000 * level\r\ndisabled @ level 25\r\n\r\n" +					"#eFull Map Range#n\r\nunlocked @ level 25\r\n\r\n" +					"#eAmount Per Pickup#n (15 - 450)\r\n= 15 * level\r\ndisabled @ level 30\r\n\r\n" +					"#eAll Item Pickup#n\r\nunlock @ level 30\r\n\r\n" +					"#eChance of Success#n (5% - 100%)\r\n= 5% * level\r\nmax @ level 20\r\n\r\n" +					"#eToggle Equip Pickup#n\r\nunlock @ level 10\r\n\r\n" +					"#eMeso Destroy Rate#n (60% - 0%)\r\n= %60 - (4% * level)\r\ndisable @ level 15\r\n\r\n" +					"#eItem Destroy Rate#n (50% - 0%)\r\n= 50% - (5% * level)\r\ndisable @ level 10");				cm.dispose();			} else if(selection == 1) {				cm.sendYesNo("#eThis quiz costs 1 red pill for each attempt.#n#i2022076#\r\nAre you sure you want to continue?");			} else {				cm.sendOk("Alright come back when you are ready. Remember you can always find me in the treehouse in Henesys.");				cm.dispose();			}		} else if(status == 2) {			if(cm.itemQuantity(2022076) > 0) {				cm.gainItem(2022076, -1);				cm.sendSimple("#eQuestion 1:#n\r\n\r\n" + q1[chosen[0]]);			} else {				cm.sendOk("#r#eYou do not have a red pill.#n #i2022076##k\r\nPlease go gather more in the #enext room#n and come back when you do.\r\n\r\nFor help with out in-game currency type '@help money' (without the quotes).");				cm.dispose();			}		} else if(status == 3) {			if(selection == a1[chosen[0]]) {				cm.sendSimple("#e#gCorrect!#k\r\nFinal Question:#n\r\n\r\n" + q2[chosen[1]]);			} else {				cm.sendOk("#e#rNot correct.#k#n Please try again.");				cm.dispose();			}		} else if(status == 4) {			if(selection == a2[chosen[1]]) {				cm.sendOk("#e#gCongratulations!#k#n\r\nHeres some items you will need.\r\nGood luck on your journey!\r\n\r\n#i4031674# #i2022076#");				cm.gainItem(4031674, 10);				cm.gainItem(2022076, 1);				cm.dispose();			} else {				cm.sendOk("#e#rNot correct.#k#n Please try again.");				cm.dispose();			}		}
	}
}