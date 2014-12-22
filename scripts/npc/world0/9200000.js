var status = 0;
var method = 0;
var apmode = 0;
var stat = 0;
var newJob = -1;
var amount = 0;
var text = "";

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
	var level = cm.getPlayer().getLevel();
	var job = cm.getJobId();
	var maxStat = cm.getMaxStat();
    if (mode == -1)
        cm.dispose();
    else {
        if (status >= 0 && mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if(cm.getTransferedInfo("codyrebirth") == "rebirth"){
            cm.removeTransferedInfo("codyrebirth");
            status = 2;
            selection = -1;
            newJob = 9999;
        }
        if (status == 0) {
			var jobClass = parseInt(job / 1000);
			var jobBase = parseInt((job % 1000) / 100); // 0 = not chosen
			var jobSpec = parseInt((job % 100) / 10); // 0 = not chosen
        	if(cm.getPlayer().getMapId() == 10000) {
        		if(level < 10) {
	        		cm.sendOk("Welcome, #b#h ##k to #bBossStory#k's tutorial! My name is #bCody#k and I am you main guide for BossStory. First I would like to ask you to get to level 10 for your first job.\r\n\r\n#eOnce you have reached level 10 come see me again.");
	        		cm.dispose();
        		} else if(level >= 10 && jobBase == 0) { // 1st Jobs
                    if(jobClass != 2) {
                        text = "What #b1st job#k would you like to have?\r\n#b";
                        text += cm.getJobsMatching(jobClass, -1, 0, 0);
                        cm.sendSimple(text);
                    } else {
                        // Aran doesn't have a choice for first job.
                        text += "Would you like to become an #bAran 1#k?";
                        cm.sendYesNo(text);
                        newJob = 2100;
                    }
                } else if(level < 30) {
	        		cm.sendOk("Congratulations on your first job! Now in order to continue to the next map I should ask you to get to level 30 for your second job.\r\n\r\n#eOnce you have reached level 30 come and see me again.");
	        		cm.dispose();
                } else if(level >= 30 && jobSpec == 0) { // 2nd Jobs
                    if(jobClass == 0) {
                        text = "What #b2nd job#k would you like to have?\r\n#b";
                        text += cm.getJobsMatching(jobClass, jobBase, -1, 0);
                        cm.sendSimple(text);
                    } else {
                        // Aran and KOC doesn't have a choice for 2nd job.
                        cm.sendYesNo("Would you like to become an #b" + cm.getJobName(job + 10) + "#k?");
                        newJob = job + 10;
                    }
                } else {
                	cm.sendOk("You have finished the first part of the tutorial!\r\nPlease proceed to the portal on the right to continue to the next part.");
                	cm.dispose();
                }
        	} else if(cm.getPlayer().getMapId() == 20000) {
        		if(cm.getPlayer().getTotalMeso() > 1000000000){
        			cm.sendOk("Proceed to the next stage through the portal on the right.");
        			cm.dispose();
        		} else {
        			cm.sendNext("Welcome to the second part of the tutorial. Here I will quiz you on some things you probably never heard of. However if you mess up your answer you can retake the quiz as many times as you want until you pass.\r\n\r\n#eClick next to continue to the first question.");
        		}
        	} else if(cm.getPlayer().getMapId() == 40000 || cm.getPlayer().getMapId() == 50000) {
        		// if(playerHasTutItems())
//         		{
//         			cm.sendOk("You have ALL the items!");
//         		}
//         		else
//         		{
//         			cm.sendOk("Talk to each skill master and get an item from them. Bring those items to me to continue.");
//         		}
				cm.warp(100000000);
        		cm.dispose();
        	} else {
            	cm.sendSimple("Hello #r#h ##k, I am #bBossStory#k's Job advancer. However, I do much more then just job advance.\r\nWhat would you like me to do for you?\r\n#b#L0#Job Advance#l\r\n#L1#Ability Points (AP) Management#l\r\n#L2#Skill Managment#l");
            }
        } else if (status == 1) {
        	if(cm.getPlayer().getMapId() == 10000) {
        		if(selection != -1){
					newJob = selection;
					text = "Are you sure you want to become a #b" + cm.getJobName(newJob) + "#k?";
					cm.sendYesNo(text);
				} else {
					cm.changeJobById(newJob);
					text = "Congratulations you are now a #b" + cm.getJobName(newJob) + "#k!";
					cm.sendOk(text);
					cm.dispose();
				}
			} else if(cm.getPlayer().getMapId() == 20000) {
        		cm.sendSimple ("#eQuestion 1:#n\r\n" +
        			"What is the curency of BossStory?\r\n\r\n#b" +
        			"#L0#Chicken (Pfft..)#l\r\n" +
        			"#L1#Coin Bags#l\r\n" +
        			"#L2#Pills#l\r\n" +
        			"#L3#Maple Coins#l\r\n");
        	} else {
				if(selection == 0) {
						method = 0;
						
						var jobClass = parseInt(job / 1000);
						var jobBase = parseInt((job % 1000) / 100); // 0 = not chosen
						var jobSpec = parseInt((job % 100) / 10); // 0 = not chosen
						var jobUnique = job % 10; // 0 = not chosen, 1 = third job, 2 = fourth job
						
						if(level >= 8 && level < 10 && jobClass == 0 && jobBase == 0) {
							cm.sendYesNo("Would you like to become a #bMagician#k?");
							newJob = 200;
						} else if(level >= 10 && jobBase == 0) { // 1st Jobs
							if(jobClass != 2) {
								text = "What #b1st job#k would you like to have?\r\n#b";
								text += cm.getJobsMatching(jobClass, -1, 0, 0);
								cm.sendSimple(text);
							} else {
								// Aran doesn't have a choice for first job.
								text += "Would you like to become an #bAran 1#k?";
								cm.sendYesNo(text);
								newJob = 2100;
							}
						} else if(level >= 30 && jobSpec == 0) { // 2nd Jobs
							if(jobClass == 0) {
								text = "What #b2nd job#k would you like to have?\r\n#b";
								text += cm.getJobsMatching(jobClass, jobBase, -1, 0);
								cm.sendSimple(text);
							} else {
								// Aran and KOC doesn't have a choice for 2nd job.
								cm.sendYesNo("Would you like to become an #b" + cm.getJobName(job + 10) + "#k?");
								newJob = job + 10;
							}
						} else if(level >= 70 && jobUnique == 0) { // 3rd Jobs
							cm.sendYesNo("Would you like to become a #b" + cm.getJobName(job + 1) + "#k?");
							newJob = job + 1;
						} else if(level >= 120 && jobUnique == 1) { // 4th Jobs
							cm.sendYesNo("Would you like to become a #b" + cm.getJobName(job + 1) + "#k?");
							newJob = job + 1;
						} else if(level >= cm.getRebirthLevel(jobClass == 1) && jobUnique == 2) { // Rebirth
							cm.sendYesNo("Rebirthing allows you to keep your current stats but your level will return to 1 and your job will be a beginner.\r\n#bSo do you want to rebirth?\r\n#rYou can also rebirth by typing '@rebirth'.");
							newJob = 9999;
						} else {
							if(level < 8)
								cm.sendOk("You must be atleast level 8 to advance to the first job.");
							else if(level < 30)
								cm.sendOk("You must be atleast level 30 to advance to the second job.");
							else if(level < 70)
								cm.sendOk("You must be atleast level 70 to advance to the third job.");
							else if(level < 120)
								cm.sendOk("You must be atleast level 120 to advance to the forth job.");
							else if(level < 200)
								cm.sendOk("You must be atleast level 200 to rebirth.");
							cm.dispose();
						}
				} else if(selection == 1){
					method = 1;
					text = "You currently have #r" + cm.getStoredAp() + "#k Ability Points (AP) Stored.\r\nWhat would you like to do?\r\n#L0#Withdraw#l\r\n#L1#Deposit#l\r\n#L2#Add Directly to Stat#l";
					cm.sendSimple(text);
				} else {
					method = 2;
					text = "Sorry this is not available yet.";
	//                text = "Select a skill to return to your keyboard:\r\n";
	//                var skills = cm.getPlayer().getSavedSkills();
	//                for(var i = 0; i < skills.length; i++){
	//                    if(!isBeginnerSkill(skills[i]))
	//                        text += "#L" + skills[i] + "##s" + skills[i] + "##q" + skills[i] + "##l\r\n";
	//                }
					cm.sendOk(text);
					cm.dispose();
				}
            }
        } else if(status == 2){
        	if(cm.getPlayer().getMapId() == 10000) {
				cm.changeJobById(newJob);
				text = "Congratulations you are now a #b" + cm.getJobName(newJob) + "#k!";
				cm.sendOk(text);
				cm.dispose();
        	} else if(cm.getPlayer().getMapId() == 20000) {
        		if(selection == 1 || selection == 2) {
        			cm.sendSimple("Ding ding ding! We have a winner!\r\n\r\nThere is only one kind of currency for BossStory. However depending on whether you install the full version of BossStory or not you will see coin bags or pills.\r\n\r\n" +
        				"#eQuestion 2:#n\r\n" +
        				"Which of the following Secondary Skill's allows for you to scroll items better?\r\n\r\n#b" +
        				"#L0#Enchantment#l\r\n" +
        				"#L1#Magneto#l\r\n" +
        				"#L2#Skinning#l\r\n" +
        				"#L3#Farming#l\r\n");
        		} else {
        			cm.sendOk("Incorrect! Really... on the first question!\r\n\r\n#ePlease try again.");
        			cm.dispose();
        		}
        	} else {
				if(method == 0){
					if(selection != -1){
						newJob = selection;
						text = "Are you sure you want to become a #b" + cm.getJobName(newJob) + "#k?";
						cm.sendYesNo(text);
					} else {
						if(newJob != 9999) {
							cm.changeJobById(newJob);
							text = "Congratulations you are now a #b" + cm.getJobName(newJob) + "#k!";
							cm.sendOk(text);
							cm.dispose();
						} else {
							//text = "Pick a skill to keep:\r\n";
							//var skills = cm.getPlayer().getSkillsByJob(cm.getJobId(), true);
							//for(var i = 0; i < skills.length; i++){
							//	if(!isBeginnerSkill(skills[i]))
							//		text += "#L" + skills[i] + "##s" + skills[i] + "##q" + skills[i] + "##l\r\n";
							//}
							text = "Congratulations you have rebirthed!"
							cm.sendOk(text);
							cm.getPlayer().rebirth();
							cm.dispose();
						}
					}
				} else if(method == 1){
					apmode = selection;
					if(selection == 0){
						if(cm.getStoredAp() > 0){
							text = "You have #r" + cm.getStoredAp() + "#k ability point(s) (AP) stored.\r\nYou have #r" + cm.getAp() + "#k already available.\r\nTo withdraw all your stored ability points (AP) enter '#rall#k' into the box.\r\n\r\nEnter how much you want to withdraw:";
							cm.sendGetText(text);
						} else {
							cm.sendOk("You don't have any ability points (AP) stored.");
							cm.dispose();
						}
					} else if(selection == 1){
						if(cm.getAp() > 0){
							text = "You have #r" + cm.getAp() + "#k ability point(s) (AP) available.\r\nTo deposit all your ability points (AP) enter '#rall#k' into the box.\r\n\r\nEnter how much you want to deposit:";
							cm.sendGetText(text);
						} else {
							cm.sendOk("You don't have any available ability points. (AP)");
							cm.dispose();
						}
					} else if(selection == 2){
						text = "You have #r" + cm.getStoredAp() + "#k ability points (AP) stored.\r\nSTR: #r" + cm.getStr() + "#k\r\nDEX: #r" + cm.getDex() + "#k\r\nINT: #r" + cm.getInt() + "#k\r\nLUK: #r" + cm.getLuk() + "#k\r\nWhat stat would you like to add to?\r\n#L0#Strength (STR)#l\r\n#L1#Dexterity (DEX)#l\r\n#L2#Intelligence (INT)#l\r\n#L3#Luck (LUK)#l";
						cm.sendSimple(text);
					}
				} else if(method == 2) {
					cm.getPlayer().changeKeybinding(42, 1, selection);
					cm.getPlayer().sendKeymap();
					cm.dispose();
				}
            }
        } else if(status == 3){
        	if(cm.getPlayer().getMapId() == 20000) {
        		if(selection == 0) {
        			cm.sendSimple("Fabulous! Ehum... I mean cool stuff.\r\n\r\nThe enchantment skill is just one of many secondary skills that have been created to give you a unique experience in BossStory. Enchantment allows for you to scroll items more efficiently.\r\n\r\n" +
        				"#eQuestion 3:#n\r\n" +
        				"What command do you use to access a library of help from within BossStory?\r\n\r\n#b" +
        				"#L0#@gm#l\r\n" +
        				"#L1#@checkstats#l\r\n" +
        				"#L2#@freemoney#l\r\n" +
        				"#L3#@help#l\r\n");
        		} else if(selection == 1) {
        			cm.sendOk("Sorry but no.\r\n\r\nThe magneto skill allows for you pick up multiple items at a distance.\r\n\r\n#ePlease try again.");
        			cm.dispose();
        		} else if(selection == 2) {
        			cm.sendOk("Sorry but no.\r\n\r\nThe skinning skill allows for you to get more of the specific monster piece's from monsters you kill with a melee attack. These monster piece's can be traded to other players who need them.\r\n\r\n#ePlease try again.");
        			cm.dispose();
        		} else if(selection == 3) {
        			cm.sendOk("Sorry but no.\r\n\r\nThe farming skill allows for you to grow crops and harvest them. These crops can be traded to other players who need them.\r\n\r\n#ePlease try again.");
        			cm.dispose();
        		}
        	} else {
				if(method == 0){
					if(newJob != 9999){
						cm.changeJobById(newJob);
						text = "Congratulations you are now a #b" + cm.getJobName(newJob) + "#k!";
						cm.sendOk(text);
					} else {
						cm.getPlayer().addSavedSkill(selection);
						cm.getPlayer().doReborn();
					}
					cm.dispose();
				} else if(method == 1){
					if(apmode == 0){
						amount = cm.getTextNumber();
						if(amount > 0 && maxStat >= amount || amount == -1){
							if(cm.getStoredAp() >= amount &&  maxStat >= amount + cm.getAp()){
								if(amount == -1){
									if(cm.getStoredAp() >= maxStat)
										amount = maxStat - cm.getAp();
									else if(cm.getAp() + cm.getStoredAp() <= maxStat)
										amount = cm.getStoredAp();
									else if(cm.getAp() + cm.getStoredAp() > maxStat)
										amount = cm.getStoredAp() - ((cm.getAp() + cm.getStoredAp()) - maxStat);
								}
								cm.addAp(amount);
								cm.subtractStoredAp(amount);
								text = "You have successfully withdrew #r" + amount + "#k ability points (AP).";
								cm.sendOk(text);
							} else {
								text = "You do not have enough room to withdraw that much ability points (AP). You can have a max of #r"+maxStat+"#k.";
								cm.sendOk(text);
							}
						} else {
							text = "#rYou entered an incorrect number.#k 1-"+maxStat;
							cm.sendOk(text);
						}
						cm.dispose();
					} else if(apmode == 1){
						amount = cm.getTextNumber();
						if(amount > 0 && maxStat >= amount || amount == -1){
							if(cm.getAp() >= amount){
								if(amount == -1)
									amount = cm.getAp();
								cm.subtractAp(amount);
								cm.addStoredAp(amount);
								text = "You have successfully deposited #r" + amount + "#k ability points (AP).";
								cm.sendOk(text);
							} else {
								cm.sendOk("You do not have enough to deposit that many ability points (AP).");
							}
						} else {
							text = "#rYou entered an incorrect number.#k 1-"+maxStat;
							cm.sendOk(text);
						}
						cm.dispose();
					} else if(apmode == 2){
						if(selection == 0){
							if(cm.getStr() < maxStat){
								text = "You have #r" + cm.getStoredAp() + "#k ability points (AP).\r\nYou have #r" + cm.getStr() + "#k strength (STR).\r\nTo put all your stored ap on a stat enter '#rall#k' into the box.\r\n\r\nHow much do you want to put on strength (STR)?";
								apmode = 1;
								cm.sendGetText(text);
							} else {
								text = "Your strength (STR) is already maxed at #r"+maxStat+"#k.";
								cm.sendOk(text);
								cm.dispose();
							}
						} else if(selection == 1){
							if(cm.getDex() < maxStat){
								text = "You have #r" + cm.getStoredAp() + "#k ability points (AP).\r\nYou have #r" + cm.getDex() + "#k dexterity (DEX).\r\nTo put all your stored ap on a stat enter '#rall#k' into the box.\r\n\r\nHow much do you want to put on dexterity (DEX)?";
								apmode = 2;
								cm.sendGetText(text);
							} else {
								text = "Your dexterity (DEX) is already maxed at #r"+maxStat+"#k.";
								cm.sendOk(text);
								cm.dispose();
							}
						} else if(selection == 2){
							if(cm.getInt() < maxStat){
								text = "You have #r" + cm.getStoredAp() + "#k ability points (AP).\r\nYou have #r" + cm.getInt() + "#k intelligence (INT).\r\nTo put all your stored ap on a stat enter '#rall#k' into the box.\r\n\r\nHow much do you want to put on intelligence (INT)?";
								apmode = 3;
								cm.sendGetText(text);
							} else {
								text = "Your intelligence (INT) is already maxed at #r"+maxStat+"#k.";
								cm.sendOk(text);
								cm.dispose();
							}
						} else if(selection == 3){
							if(cm.getLuk() < maxStat){
								text = "You have #r" + cm.getStoredAp() + "#k ability points (AP).\r\nYou have #r" + cm.getLuk() + "#k luck (LUK).\r\nTo put all your stored ap on a stat enter '#rall#k' into the box.\r\n\r\nHow much do you want to put on luck (LUK)?";
								apmode = 4;
								cm.sendGetText(text);
							} else {
								text = "Your luck (LUK) is already maxed at #r"+maxStat+"#k.";
								cm.sendOk(text);
								cm.dispose();
							}
						}
					}
				}
            }
        } else if(status == 4){
        	if(cm.getPlayer().getMapId() == 20000) {
        		if(selection == 0) {
        			cm.sendOk("Sorry but no.\r\n\r\nThe #b@gm#k command sends a message to all online GM's that you need help.\r\n\r\n#ePlease try again.");
        			cm.dispose();
        		} else if(selection == 1) {
        			cm.sendOk("Sorry but no.\r\n\r\nThe #b@checkstats#k command allows for you to check the stats of yourself or others.\r\n\r\n#ePlease try again.");
        			cm.dispose();
        		} else if(selection == 2) {
        			cm.sendOk("Sorry but no.\r\n\r\nThe #b@freemoney#k command gives you freemoney.\r\n\r\n#ePlease try again.");
        			cm.dispose();
        		} else if(selection == 3) {
        			cm.sendSimple("Your like a pro! Ok one more question and I'll let you go to the next stage of the tutorial.\r\n\r\nThe #b@help#k command opens a library of help that will answer almost any question you have about BossStory.\r\n\r\n" +
        				"#eQuestion 4:#n\r\n" +
        				"When you reach level 200 you can rebirth. Rebirthing allows for you to keep your current stats except for your level and job. Your level gets set to 1 and your job gets set to a beginner.\r\nWhen you reach maximum stats after rebirthing enough times what item can you recieve to continue getting stronger?\r\n\r\n" +
        				"#L0#A Boss Pill#l\r\n" +
        				"#L1#Lots of Money#l\r\n" +
        				"#L2#A Super Skill#l\r\n" +
        				"#L3#Max Stat Item#l\r\n");
        		}
        	} else {
				if(method == 1){
					amount = cm.getTextNumber();
					if(amount > 0 && maxStat >= amount || amount == -1){
						if(cm.getStr() + amount <= maxStat && apmode == 1){
							if(amount == -1 && cm.getStoredAp() >= maxStat)
								amount = maxStat - cm.getStr();
							else if(amount == -1)
								amount = cm.getStoredAp();
							cm.addStr(amount);
							cm.subtractStoredAp(amount);
							text = "You have successfully added #r" + amount + "#k to your strength (STR).";
							cm.sendOk(text);
						} else if(cm.getDex() + amount <= maxStat && apmode == 2){
							if(amount == -1 && cm.getStoredAp() >= maxStat)
								amount = maxStat - cm.getDex();
							else if(amount == -1)
								amount = cm.getStoredAp();
							cm.addDex(amount);
							cm.subtractStoredAp(amount);
							text = "You have successfully added #r" + amount + "#k to your dexterity (DEX).";
							cm.sendOk(text);
						} else if(cm.getInt() + amount <= maxStat && apmode == 3){
							if(amount == -1 && cm.getStoredAp() >= maxStat)
								amount = maxStat - cm.getInt();
							else if(amount == -1)
								amount = cm.getStoredAp();
							cm.addInt(amount);
							cm.subtractStoredAp(amount);
							text = "You have successfully added #r" + amount + "#k to your intelligence (INT).";
							cm.sendOk(text);
						} else if(cm.getLuk() + amount <= maxStat && apmode == 4){
							if(amount == -1 && cm.getStoredAp() >= maxStat)
								amount = maxStat - cm.getLuk();
							else if(amount == -1)
								amount = cm.getStoredAp();
							cm.addLuk(amount);
							cm.subtractStoredAp(amount);
							text = "You have successfully added #r" + amount + "#k to your luck (LUK).";
							cm.sendOk(text);
						} else {
							text = "You can only have a max of #r" + amount + "#k on each stat.";
							cm.sendOk(text);
						}
					} else {
						text = "#rYou entered an incorrect number. #k1-"+maxStat;
						cm.sendOk(text);
					}
					cm.dispose();
				}
            }
        } else if(status == 5) {
        		if(selection == 0) {
        			cm.sendOk("Nope.\r\n\r\n#ePlease try again.");
        		} else if(selection == 1) {
        			cm.sendOk("Nope.\r\n\r\n#ePlease try again.");
        		} else if(selection == 2) {
        			cm.sendOk("Nope.\r\n\r\n#ePlease try again.");
        		} else if(selection == 3) {
        			cm.sendOk("Congratulations! You have beaten the second stage of the tutorial. Take this item and proceed to the portal on the right.");
        			cm.gainItem(2022076, 1);
        		}
            cm.dispose();
        }
    }
}

function isBeginnerSkill(skill){
    var ret = false;
    switch(skill){
        case 8:
            ret = true;
            break;
        case 1000:
            ret = true;
            break;
        case 1001:
            ret = true;
            break;
        case 1002:
            ret = true;
            break;
        case 1003:
            ret = true;
            break;
        case 1004:
            ret = true;
            break;
        case 1005:
            ret = true;
            break;
    }
    return ret;
}

function playerHasTutItems()
{
	return cm.haveItem(4001015)
		&& cm.haveItem(4031674)
		&& cm.haveItem(4001077)
		&& cm.itemQuantity(2022076) == 10;
}