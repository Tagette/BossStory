//Author: Tagette from BossStory
//Treasure Chest : Vote Shop NPC
var status = 0;
var text = "";
var method = 0;
var restart = false;
var restartNx = false;
var toScroll = 0;

var specialItemIds = Array();
var specialItemAmount = Array();
var specialItemCost = Array();


function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1)
		cm.dispose();
	else {
		if (status >= 0 && mode == 0) {
			//code on exit...
			//cm.getPlayer().saveToDB(true, true);
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if(restart){
			restart = false;
			if(!restartNx){
				status = 1;
				selection = 0;
				method = 0;
			} else {
				status = 2;
				selection = 2;
			}
		}
		if(cm.getTransferedInfo("vpshop") == "nxcash"){
			cm.removeTransferedInfo("vpshop");
			restartNx = true;
			status = 2;
			selection = 2;
		}
		if (status == 0) {
			text = "#eWelcome #b#h ##k to the In-Game Vote Shop!#n\r\nYou currently have #e#g" + cm.getPlayer().getVotePoints() + "#k#n VP.\r\n\r\n#eWhat would you like to do with your VP?#n\r\n#L0#Spend Vote Points#l\r\n#L1#What are Vote Points?#l";
			cm.sendSimple(text);
		} else if (status == 1) {
			if(selection == 0){
				cm.sendSimple("#eWhat would you like to Buy?#n\r\n\r\n#L0#Buy Gachapon Tickets (1 VP each)#l\r\n#L1#Scroll an Equip in your Inventory#l\r\n#L2#Buy Nx Cash (1 VP = 500nx)#l\r\n#L3#Buy Special Items#l");
			} else {
				cm.sendOk("#eWhat is VP?#n\r\nVote points(VP) can be used to buy special things that you wouldn't be able to buy normally with mesos. Such as gachapon tickets, which are used at the large red gachapon machine for tetris peices, NX cash, Special Chairs, Special Mounts, Special potions, and so much more!\r\n\r\n#eHow do I get Vote Points?#n\r\nYou can get vote points by logging onto BossStory's website and voting at the bottom of any page.\r\n#e#bwww.BossStory.tk#k#n\r\n\r\n#eWhy can't I vote again?#n\r\nYou can only vote every 12 hours otherwize they won't count on the voting sites.\r\n\r\n#eHow do I spend VP?#n\r\nYou can spend your VP by re-visiting this npc and selecting \"Spend Vote Points\" from the options.");
				cm.dispose();
			}
		} else if (status == 2) {
			if(selection == 0){
				method = 1;
				text = "You currently have #e#g" + cm.getPlayer().getVotePoints() + "#k#n VP.\r\nHow many Gachapon tickets would you like to buy?\r\n";
				text += "#L1##i5220000# 1 Gachapon Ticket#l\r\n#L5##i5220000# 5 Gachapon Tickets#l\r\n#L10##i5220000# 10 Gachapon Tickets#l\r\n#L25##i5220000# 25 Gachapon Tickets#l\r\n#L50##i5220000# 50 Gachapon Tickets#l";
				cm.sendSimple(text);
			} else if(selection == 1){
				method = 2;
				text = "#eWhich equip item would you like to scroll?#n\r\n\r\n";
				text += cm.EquipList();
				cm.sendSimple(text);
			} else if(selection == 2){
				method = 3;
				text = "You currently have #e#g" + cm.getPlayer().getVotePoints() + "#k#n VP and #e#g" + cm.getPlayer().getCSPoints(1) + "#k#n Nx Cash.\r\n#eHow much Nx Cash would you like to buy?#n\r\n";
				text += "#L2#1000 Nx Cash(2 VP)#l\r\n#L10#5000 Nx Cash(10 VP)#l\r\n#L20#10000 Nx Cash(20 VP)#l\r\n#L50#25000 Nx Cash(50 VP)#l";
				cm.sendSimple(text);
			} else if(selection == 3){
				cm.sendOk("Coming soon!");
				restart = true;
				//method = 4;
				//text = "You currently have #e#g" + cm.getPlayer().getVotePoints() + "#k#n VP.\r\nWhat item would you like to buy?\r\n\r\n";
				//for(var i = 0; i < specialItemIds.length; i++){
				//	text += "#L" + i + "##v" + specialItemIds[i] + "# #t" + specialItemIds[i] + "# x " + specialItemAmount[i] + " (#e" + specialItemCost[i] + " VP#n)\r\n"
				//}
				//cm.sendSimple(text);
			}
		} else if (status == 3) {
			if(method == 1){
				if(cm.getPlayer().getVotePoints() >= selection){
					cm.getPlayer().setVotePoints(cm.getPlayer().getVotePoints() - selection);
					cm.gainItem(5220000, selection);
					text = "#eYou have bought #g" + selection + " Gachapon Tickets#k for #g" + selection + " VP#k#n.\r\nYou have #e#g" + cm.getPlayer().getVotePoints() + "#k#n VP left over.";
					if(cm.getPlayer().getVotePoints() > 0){
						text += "\r\n\r\n#eWould you like to buy something else?#n";
						cm.sendYesNo(text);
						restart = true;
					} else {
						cm.sendOk(text);
						cm.getPlayer().saveToDB(true, true);
						cm.dispose();
					}
				} else {
					text = "#rYou do not have enough Vote Points to buy that many Gachapon Tickets.#k\r\nYou currently have #e#g" + cm.getPlayer().getVotePoints() + "#k#n VP.";
					if(cm.getPlayer().getVotePoints() > 0){
						text += "\r\n\r\n#eWould you like to buy something else?#n";
						cm.sendYesNo(text);
						restart = true;
					} else {
						cm.sendOk(text);
						cm.dispose();
					}
				}
			} else if(method == 2){
				toScroll = selection;
				text = "#eHow much do you want to scroll your #t" + toScroll + "#?#n #v" + toScroll + "#\r\nYou currently have #e#g" + cm.getPlayer().getVotePoints() + "#k#n VP.\r\n#b1 time = 1 VP#k\r\n\r\n";
				text += "#L3#1 time(+10 WATK) for 3 VP#l\r\n#L12#5 times(+50 WATK) for 12 VP#l\r\n#L25#10 times(+100 WATK) for 25 VP#l\r\n#L60#25 times(+250 WATK) for 60 VP#l\r\n#L100#50 times(+500 WATK) for 115 VP#l";
				cm.sendSimple(text);
			} else if(method == 3){
				if(cm.getPlayer().getVotePoints() >= selection){
					cm.getPlayer().setVotePoints(cm.getPlayer().getVotePoints() - selection);
					cm.getPlayer().modifyCSPoints(1, selection * 500);
					text = "#eYou have bought #g" + selection * 500 + " Nx Cash#k for #g" + selection + " VP#k#n.\r\nYou have #e#g" + cm.getPlayer().getVotePoints() + "#k#n VP left over.";
					if(cm.getPlayer().getVotePoints() > 0){
						if(!restartNx)
							text += "\r\n\r\n#eWould you like to buy something else?#n";
						else
							text += "\r\n\r\n#eWould you like to buy more Nx Cash?#n";
						cm.sendYesNo(text);
						restart = true;
					} else {
						cm.sendOk(text);
						cm.getPlayer().saveToDB(true, true);
						cm.dispose();
					}
				} else {
					text = "#rYou do not have enough Vote Points to buy that much Nx Cash.#k\r\nYou currently have #e#g" + cm.getPlayer().getVotePoints() + "#k#n VP.";
					if(cm.getPlayer().getVotePoints() > 0){
						if(!restartNx)
							text += "\r\n\r\n#eWould you like to buy something else?#n";
						else
							text += "\r\n\r\n#eWould you like to buy more Nx Cash?#n";
						cm.sendYesNo(text);
						restart = true;
					} else {
						cm.sendOk(text);
						cm.dispose();
					}
				}
			} else if(method == 4){
				if(cm.getPlayer().getVotePoints() >= specialItemCost[selection]){
					cm.getPlayer().setVotePoints(cm.getPlayer().getVotePoints() - specialItemCost[selection]);
					cm.gainItem(specialItemIds[selection], specialItemAmount[selection]);
					text = "#e#gYou have bought " + specialItemAmount[selection] + " " + specialItemIds[selection]  + " for " + specialItemCost[selection] + " VP#k#n.\r\nYou have #e#g" + cm.getPlayer().getVotePoints() + "#k#n VP left over.";
					if(cm.getPlayer().getVotePoints() > 0){
						text += "\r\n\r\n#eWould you like to buy something else?#n";
						cm.sendYesNo(text);
						restart = true;
					} else {
						cm.sendOk(text);
						cm.getPlayer().saveToDB(true, true);
						cm.dispose();
					}
				} else {
					text = "#rYou do not have enough Vote Points to buy that much Nx Cash.#k\r\nYou currently have #e#g" + cm.getPlayer().getVotePoints() + "#k#n VP.";
					if(cm.getPlayer().getVotePoints() > 0){
						text += "\r\n\r\n#eWould you like to buy something else?#n";
						cm.sendYesNo(text);
						restart = true;
					} else {
						cm.sendOk(text);
						cm.getPlayer().saveToDB(true, true);
						cm.dispose();
					}
				}
			}
		} else if (status == 4) {
			if(method == 2){
				if(cm.getPlayer().getVotePoints() >= specialItemCost[selection]){
					var many = 0;
					if(selection == 3)
						many = 1;
					else if(selection == 12)
						many = 5;
					else if(selection == 25)
						many = 10;
					else if(selection == 60)
						many = 25;
					else if(selection == 100)
						many = 50;
					var equip = cm.getEquipById(toScroll, true);
					if(equip.getWatk() + (many * 10) <= 32000){
						cm.getPlayer().setVotePoints(cm.getPlayer().getVotePoints() - selection);
						text = "#eYou have scrolled your #v" + toScroll + "##g " + many + " times#k for #g" + selection + " VP#k#n.\r\nYou have #e#g" + cm.getPlayer().getVotePoints() + "#k#n VP left over.";
						equip.setWatk(equip.getWatk() + (many * 10));
						var giveEquip = cm.getEquipWithSameStats(equip);
						cm.removeItem(equip.getPosition());
						cm.giveEquip(giveEquip, true);
						//cm.getPlayer().equipChanged();
						//cm.enableActions();
						if(cm.getPlayer().getVotePoints() > 0){
							text += "\r\n\r\n#eWould you like to buy something else?#n";
							cm.sendYesNo(text);
							restart = true;
						} else {
							cm.sendOk(text);
							cm.dispose();
						}
						cm.getPlayer().saveToDB(true, true);
					} else {
						text = "#rThis item has its WATK maxed already. Please choose another.#k\r\nYou currently have #e#g" + cm.getPlayer().getVotePoints() + "#k#n VP.";
						if(cm.getPlayer().getVotePoints() > 0){
							text += "\r\n\r\n#eWould you like to buy something else?#n";
							cm.sendYesNo(text);
							restart = true;
						} else {
							cm.sendOk(text);
							cm.dispose();
						}
					}
				} else {
					text = "#rYou do not have enough Vote Points to scroll #i" + toScroll + "#.#k\r\nYou currently have #e#g" + cm.getPlayer().getVotePoints() + "#k#n VP.";
					if(cm.getPlayer().getVotePoints() > 0){
						text += "\r\n\r\n#eWould you like to buy something else?#n";
						cm.sendYesNo(text);
						restart = true;
					} else {
						cm.sendOk(text);
						cm.getPlayer().saveToDB(true, true);
						cm.dispose();
					}
				}
			}
		} else {
			cm.dispose();
		}
	}
}
/* Shumi JQ Chest #1


var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    var prizes = Array(1040045, 1040055, 1040129, 1040137, 1041109, 1041009, 1041134, 1041132, 1041005, 1041138, 1042018, 1042035, 1042038, 1042024, 1042002, 1702000, 1702027);
    var chances = Array(10, 10, 10, 5, 10, 10, 10, 10, 10, 5, 10, 10, 10, 10, 10, 5, 3);
    var totalodds = 0;
    var choice = 0;
    for (var i = 0; i < chances.length; i++) {
        var itemGender = (Math.floor(prizes[i]/1000)%10);
        if ((cm.getPlayer().getGender() != itemGender) && (itemGender != 2))
            chances[i] = 0;
    }
    for (var i = 0; i < chances.length; i++)
        totalodds += chances[i];
    var randomPick = Math.floor(Math.random()*totalodds)+1;
    for (var i = 0; i < chances.length; i++) {
        randomPick -= chances[i];
        if (randomPick <= 0) {
            choice = i;
            randomPick = totalodds + 100;
        }
    }
    if (cm.isQuestStarted(2055))
        cm.gainItem(4031039,1);
    cm.gainItem(prizes[choice],1);
    cm.warp(103000100, 0);
    cm.dispose();
}*/


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
/* Shumi JQ Chest #1


function start() {
    prizes = [4020000,4020001,4020002,4020003,4020004];
    if (cm.isQuestStarted(2055))
        cm.gainItem(4031039,1);
    else
        cm.gainItem(4020000 + ((Math.random()*5)|0), 1);
    cm.warp(103000100);
    cm.dispose();
}*/