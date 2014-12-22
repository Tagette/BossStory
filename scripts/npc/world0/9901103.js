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
 * @Name:		Skinner
 * @Author:		Tagette
 * @NPC:		9901103
 * @Purpose:	Master of Skinning.
 */
 var status = 0;

var q1 = new Array(
	// ---
	"What does the #rSkinning#k skill do?\r\n#b#e" + 
	"#L0#Allows you to teleports to Ellinia.#l\r\n" + 
	"#L1#Allows you to get more monster peices from a monster.#l\r\n" + 
	"#L2#Allows you to grow more skin.#l\r\n" + 
	"#L3#Allows you to get under your parents' skin.#l", 
	// ---
	"What skill allows you to get more monster peices from a single monster?\r\n#b#e" + 
	"#L0#Farming.#l\r\n" + 
	"#L1#Skinning.#l\r\n" + 
	"#L2#Necromancy.#l\r\n" + 
	"#L3#Pet Breeding.#l", 
	// ---
	"When does the #rSkinning#k skill gain exp?\r\n#b#e" + 
	"#L0#When you click this option.#l\r\n" + 
	"#L1#When beating a sibling. (Don't do it)#l\r\n" + 
	"#L2#When you successfully skin a monster.#l\r\n" + 
	"#L3#When you scream so loud that every monster gets chills under their skin.#l");
var a1 = new Array(1, 1, 2);

var q2 = new Array(
	// ---
	"Using the #rSkinning#k skill, at what skill level can you attack monsters across the map?\r\n#b#e" + 
	"#L0#10#l\r\n" + 
	"#L1#8#l\r\n" + 
	"#L2#15#l\r\n" + 
	"#L3#20#l",
	// ---
	"What is the highest chance of a skinning to be successful, using the #rSkinning#k skill?\r\n#b#e" + 
	"#L0#50%#l\r\n" + 
	"#L1#100%#l\r\n" + 
	"#L2#90%#l\r\n" + 
	"#L3#10%#l",
	// ---
	"Using the #rMagneto#k skill, what is the most amount of items that can be skinned from one monster?\r\n#b#e" + 
	"#L0#16#l\r\n" + 
	"#L1#30#l\r\n" + 
	"#L2#20#l\r\n" + 
	"#L3#8#l");
var a2 = new Array(3, 2, 0);

var chosen = new Array();

function start() {
	chosen[0] = Math.floor(Math.random() * q1.length);
	chosen[1] = Math.floor(Math.random() * q2.length);
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1)
		cm.dispose();
	else {
		if (status >= 0 && mode == 0) {
			cm.sendOk("Alright come back when you are ready. Remember you can always find me in the treehouse in Henesys.");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			if(!cm.haveItem(4001077)) {
				cm.sendSimple("Hello #b#h ##k! I am called #bBungo Ungo#k. I am a master in the #eSkinning#n Secondary Skill.\r\n\r\n" +
					"How can I help you?\r\n#b#e" + 
					"#L0#I would like to know more about the Skinning skill.#l\r\n" +
					"#L1#I am ready for the test.#l\r\n" +
					"#L2#Nevermind.#l");
			} else {
				cm.sendSimple("Hello #b#h ##k! I am called #bBungo Ungo#k. I am a master in the #eSkinning#n Secondary Skill.\r\n\r\n" +
					"How can I help you?\r\n#b#e" + 
					"#L0#I would like to know more about the Magneto skill.#l\r\n" +
					"#L2#Nevermind.#l");
			}
		} else if (status == 1) {
			if(selection == 0) {
				cm.sendOk("#eSkill Name:#n Skinning\r\n" + 
					"#eDescription:#n Drops more monster peices from a monster when attacked at close range.\r\n" +
					"#eMethod of gaining exp:#n Successfully skinning a monster.\r\n" +
					"#eRequirements to Use:#n None.\r\n" +
					"#eTechnical information:#n\r\n\r\n" +
					"#eNumber of Items Dropped#n (1 - 16)\r\n= (level / 2) + 1\r\nmax @ level 30\r\n\r\n" +
					"#eChance of Success#n (0% - 90%)\r\n= level * 3%\r\nmax @ level 30\r\n\r\n" +
					"#eKO Hit Chance#n (0% - 15%)\r\n= level * 0.5%\r\nmax @ level 30\r\n\r\n" +
					"#eDemi Attack Amount#n (1 - 11)\r\n= level - 19\r\nunlock @ level 20\r\nmax @ level 30\r\n\r\n" +
					"#eItem Drop Stacking#n\r\nunlock @ level 20\r\n\r\n" +
					"#eAll Skill Skinning#n\r\nunlock @ level 25\r\n\r\n");
					//"#eBoss Skinning#n (0% - 15%)\r\nunlock @ level 30\r\n\r\n");
				cm.dispose();
			} else if(selection == 1) {
				cm.sendYesNo("#eThis quiz costs 1 red pill for each attempt.#n#i2022076#\r\nAre you sure you want to continue?");
			} else {
				cm.sendOk("Alright come back when you are ready. Remember you can always find me in the treehouse in Henesys.");
				cm.dispose();
			}
		} else if(status == 2) {
			if(cm.itemQuantity(2022076) > 0) {
				cm.gainItem(2022076, -1);
				cm.sendSimple("#eQuestion 1:#n\r\n\r\n" + q1[chosen[0]]);
			} else {
				cm.sendOk("#r#eYou do not have a red pill.#n #i2022076##k\r\nPlease go gather more in the #enext room#n and come back when you do.\r\n\r\nFor help with out in-game currency type '@help money' (without the quotes).");
				cm.dispose();
			}
		} else if(status == 3) {
			if(selection == a1[chosen[0]]) {
				cm.sendSimple("#e#gCorrect!#k\r\nFinal Question:#n\r\n\r\n" + q2[chosen[1]]);
			} else {
				cm.sendOk("#e#rNot correct.#k#n Please try again.");
				cm.dispose();
			}
		} else if(status == 4) {
			if(selection == a2[chosen[1]]) {
				cm.sendOk("#e#gCongratulations!#k#n\r\nHeres some items you will need.\r\nGood luck on your journey!\r\n\r\n#i4001077# #i2022076#");
				cm.gainItem(4001077, 10);
				cm.gainItem(2022076, 1);
				cm.dispose();
			} else {
				cm.sendOk("#e#rNot correct.#k#n Please try again.");
				cm.dispose();
			}
		}
	}
}