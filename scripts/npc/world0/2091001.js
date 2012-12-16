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
var text = "";
var method = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	var victim = cm.getTransferedInfo("checkstats");
	if(victim != "") {
		status = 2;
		method = 1;
	}
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
		if (status == 0) {
			text = cm.getTop5Stats();
			text += "\r\n"+
					"#bCheckStats:#k\r\n"+
					"#L0#Check your stats#l\r\n"+
					"#L1#Check someone elses stats#l\r\n"+
					"\r\n"+
					"#bRanking:#k\r\n"+
					"#L2#Rebirths#l\r\n"+
					"#L3#Wealth#l";
			cm.sendSimple(text);
		} else if(status == 1){
			method = selection;
			if(selection == 0){
				cm.sendOk(cm.getPlayerStats(cm.getName()));
				cm.dispose();
			} else if(selection == 1){
				cm.sendGetText("Enter the name of the user you wish to check the stats of in the box below:");
			} else {
				cm.sendOk("Thats not coded yet!");
				cm.dispose();
			}
		} else if(status == 2) {
			if(method == 1) {
				if(victim == "")
					victim = cm.getText();
				cm.sendOk(cm.getPlayerStats(victim));
				cm.dispose();
			}
		} else {
			cm.dispose();
		}
	}
}
