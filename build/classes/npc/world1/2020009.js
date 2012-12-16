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
//BY MOOGRA
/* Robeira
	Magician 3rd job advancement
	El Nath: Chief's Residence (211000001)
	Custom Quest 100100, 100102
*/

var status = 0;
var job;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 1) {
            cm.sendOk("Make up your mind and visit me again.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (!(cm.getJobId()==210 ||cm.getJobId()==220||cm.getJobId()==230)) {
                cm.sendOk("May #rOdin#k be with you!");
                cm.dispose();
                return;
            }
            cm.completeQuest(100100);
            cm.completeQuest(100102);
            if (cm.isQuestCompleted(100102))
                cm.sendNext("#rBy Odin's ring!#k Indeed, you have proven to be worthy of the strength I will now bestow upon you.");
            else if (cm.isQuestStarted(100102)) {
                cm.sendOk("Go and find me the #rNecklace of Wisdom#k which is hidden on the Holy Ground at the Snowfield.");
                cm.dispose();
            } else if (cm.isQuestCompleted(100100))
                cm.sendNext("#rBy Odin's raven!#k I was right, your strength is truly excellent.");
            else if (cm.isQuestStarted(100100)) {
                cm.sendOk("Well, well. Now go and see #bGrendel the Really Old#k. He will show you the way.");
                cm.dispose();
            } else if ((cm.getJobId()==210 ||cm.getJobId()==220||cm.getJobId()==230) &&cm.getLevel() >= 70 && cm.getPlayer().getRemainingSp() <= (cm.getLevel() - 70) * 3)
                cm.sendNext("#rBy Odin's beard!#k You are a strong one.");
            else {
                cm.sendOk("Your time has yet to come...");
                cm.dispose();
            }
        } else if (status == 1) {
            if (cm.isQuestCompleted(100102)) {
                if (cm.getJobId()==210) {
                    cm.changeJobById(211);
                    //                    cm.getPlayer().gainAp(5);
                    cm.sendOk("You are now a #bFire/Poison Mage#k. May #rOdin#k be with you!");
                    cm.dispose();
                } else if (cm.getJobId()==220) {
                    cm.changeJobById(221);
                    //                    cm.getPlayer().gainAp(5);
                    cm.sendOk("You are now an #bIce/Lightning Mage#k. May #rOdin#k be with you!");
                    cm.dispose();
                } else if (cm.getJobId()==230) {
                    cm.changeJobById(231);
                    //                    cm.getPlayer().gainAp(5);
                    cm.sendOk("You are now a #bPriest#k. May #rOdin#k be with you!");
                    cm.dispose();
                }
            } else if (cm.isQuestCompleted(100100))
                cm.sendAcceptDecline("Is your mind ready to undertake the final test?");
            else
                cm.sendAcceptDecline("But I can make you even stronger. Although you will have to prove not only your strength but your knowledge. Are you ready for the challenge?");
        } else if (status == 2) {
            if (cm.isQuestCompleted(100100)) {
                cm.startQuest(100102);
                cm.sendOk("Go and find me the #rNecklace of Wisdom#k which is hidden on the Holy Ground at the Snowfield.");
                cm.dispose();
            } else {
                cm.startQuest(100100);
                cm.sendOk("Well, well. Now go and see #bGrendel the Really Old#k. He will show you the way.");
                cm.dispose();
            }
        }
    }
}
