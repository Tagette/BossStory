var status = 0;
var text = "";
var method = 0;
var equip = null;

var dstr = -1;
var ddex = -1;
var dint = -1;
var dluk = -1;
var dmAtk = -1;
var dwAtk = -1;
var dmDef = -1;
var dwDef = -1;
var dhp = -1;
var dmp = -1;
var dacc = -1;
var davo = -1;
var dhands = -1;
var djump = -1;
var dspeed = -1;
var dmSlot = -1;

var id = 0;
var str = -1;
var dex = -1;
var _int = -1;
var luk = -1;
var mAtk = -1;
var wAtk = -1;
var mDef = -1;
var wDef = -1;
var hp = -1;
var mp = -1;
var acc = -1;
var avo = -1;
var hands = -1;
var jump = -1;
var speed = -1;
var mSlot = -1;
var owner = "";

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1)
		cm.dispose();
	else {
		if (status >= 0 && mode == 0) {
			cm.sendOk("Come back again.");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			if(cm.getPlayer().gmLevel() > 1){
				text = "Welcome #h #, I am the max stat item creator.\r\nWhat would you like to do?\r\n"+
					"#L0#Change the stats of an equip in my inventory#l\r\n"+
					"#L1#Create a new custom stat equip#l";
				cm.sendSimple(text);
			} else {
				cm.sendOk("I am a GM Level 2+ npc only.");
				cm.dispose();
			}
		} else if (status == 1) {
			if(selection == 0){
				method = 2;
				text = "Which item would you like to use?\r\n"+cm.EquipList();
				cm.sendSimple(text);
			} else {
				text = "What would you like to do?\r\n"+
					"#L0#Enter the id of the equip#l\r\n"+
					"#L1#Search for the item by name#l";
				cm.sendSimple(text);
			}
		} else if (status == 2) {
			if(method == 2){
				id = selection;
				text = "Are you sure you want to change the stats on the weapon below?\r\n#v"+ id +"##t"+ id +"#";
				cm.sendYesNo(text);
			} else if(selection == 0) {
				method = 0;
				text = "Enter the id of the equip below:";
				cm.sendGetText(text);
			} else {
				method = 1;
				text = "Enter the name of the item you want to search below:";
				cm.sendGetText(text);
				//cm.sendOk("Disabled");
				//cm.dispose();
			}
		} else if (status == 3) {
			if(method == 2){
				text = "Click next to continue.";
				cm.sendNext(text);
			} else if(method == 0) {
				id = cm.getText();
				text = "Click next to continue.";
				cm.sendNext(text);
			} else if(method == 1) {
				text = cm.searchItem(cm.getText());
				cm.sendSimple(text);
				if(text == "No Item's Found"){
					cm.dispose();
				}
			}
		} else if (status == 4) {
			if(method == 1){
				id = selection;
			}
			equip = cm.getEquipById(id);
			
			if(equip == null){
				text = "The item with id "+id+" is not an equip.";
				cm.sendOk(text);
				cm.dispose();
			}
			dstr = cm.getItemStatValue(equip, 0);
			ddex = cm.getItemStatValue(equip, 1);
			dint = cm.getItemStatValue(equip, 2);
			dluk = cm.getItemStatValue(equip, 3);
			dmAtk = cm.getItemStatValue(equip, 4);
			dwAtk = cm.getItemStatValue(equip, 5);
			dmDef = cm.getItemStatValue(equip, 6);
			dwDef = cm.getItemStatValue(equip, 7);
			dhp = cm.getItemStatValue(equip, 8);
			dmp = cm.getItemStatValue(equip, 9);
			dacc = cm.getItemStatValue(equip, 10);
			davo = cm.getItemStatValue(equip, 11);
			dhands = cm.getItemStatValue(equip, 12);
			djump = cm.getItemStatValue(equip, 13);
			dspeed = cm.getItemStatValue(equip, 14);
			dmSlot = cm.getItemStatValue(equip, 15);
			
			text = "#rCURRENT STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #r"+dstr+"#k    DEX:  #r"+ddex+"#k"+
				"\r\nINT:  #r"+dint+"#k    LUK:  #r"+dluk+"#k"+
				"\r\nWATK: #r"+dwAtk+"#k    MATK: #r"+dmAtk+"#k"+
				"\r\nWDEF: #r"+dwDef+"#k    MDEF: #r"+dmDef+"#k"+
				"\r\nHP:   #r"+dhp+"#k    MP:   #r"+dmp+"#k"+
				"\r\nAccuracy: #r"+dacc+"#k"+
				"\r\nAvoidability: #r"+davo+"#k"+
				"\r\nHands: #r"+dhands+"#k"+
				"\r\nJump: #r"+djump+"#k"+
				"\r\nSpeed: #r"+dspeed+"#k"+
				"\r\nUpgrade Slots: #r"+dmSlot+"#k"+
				"\r\n"+
				"\r\n#r*All stats are default*#k"+
				"\r\n"+
				"\r\nEnter what you would like for the strength(-1 to keep default):\r\n#r32767 is max#k";
			cm.sendGetText(text);
		} else if (status == 5) {
			str = cm.getText();
			if(str < 0)
				str = dstr;
			if(str > 32767)
				str = 32767;
			
			text = "#rCURRENT STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #g"+str+"#k    DEX:  #r"+ddex+"#k"+
				"\r\nINT:  #r"+dint+"#k    LUK:  #r"+dluk+"#k"+
				"\r\nWATK: #r"+dwAtk+"#k    MATK: #r"+dmAtk+"#k"+
				"\r\nWDEF: #r"+dwDef+"#k    MDEF: #r"+dmDef+"#k"+
				"\r\nHP:   #r"+dhp+"#k    MP:   #r"+dmp+"#k"+
				"\r\nAccuracy: #r"+dacc+"#k"+
				"\r\nAvoidability: #r"+davo+"#k"+
				"\r\nHands: #r"+dhands+"#k"+
				"\r\nJump: #r"+djump+"#k"+
				"\r\nSpeed: #r"+dspeed+"#k"+
				"\r\nUpgrade Slots: #r"+dmSlot+"#k"+
				"\r\n"+
				"\r\nEnter what you would like for the dexterity(-1 to keep default):\r\n#r32767 is max#k";
			cm.sendGetText(text);
		} else if (status == 6) {
			dex = cm.getText();
			if(dex < 0)
				dex = ddex;
			if(dex > 32767)
				dex = 32767;
			
			text = "#rCURRENT STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #g"+str+"#k    DEX:  #g"+dex+"#k"+
				"\r\nINT:  #r"+dint+"#k    LUK:  #r"+dluk+"#k"+
				"\r\nWATK: #r"+dwAtk+"#k    MATK: #r"+dmAtk+"#k"+
				"\r\nWDEF: #r"+dwDef+"#k    MDEF: #r"+dmDef+"#k"+
				"\r\nHP:   #r"+dhp+"#k    MP:   #r"+dmp+"#k"+
				"\r\nAccuracy: #r"+dacc+"#k"+
				"\r\nAvoidability: #r"+davo+"#k"+
				"\r\nHands: #r"+dhands+"#k"+
				"\r\nJump: #r"+djump+"#k"+
				"\r\nSpeed: #r"+dspeed+"#k"+
				"\r\nUpgrade Slots: #r"+dmSlot+"#k"+
				"\r\n"+
				"\r\nEnter what you would like for the intelligence(-1 to keep default):\r\n#r32767 is max#k";
			cm.sendGetText(text);
		} else if (status == 7) {
			_int = cm.getText();
			if(_int < 0)
				_int = dint;
			if(_int > 32767)
				_int = 32767;
			
			text = "#rCURRENT STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #g"+str+"#k    DEX:  #g"+dex+"#k"+
				"\r\nINT:  #g"+_int+"#k    LUK:  #r"+dluk+"#k"+
				"\r\nWATK: #r"+dwAtk+"#k    MATK: #r"+dmAtk+"#k"+
				"\r\nWDEF: #r"+dwDef+"#k    MDEF: #r"+dmDef+"#k"+
				"\r\nHP:   #r"+dhp+"#k    MP:   #r"+dmp+"#k"+
				"\r\nAccuracy: #r"+dacc+"#k"+
				"\r\nAvoidability: #r"+davo+"#k"+
				"\r\nHands: #r"+dhands+"#k"+
				"\r\nJump: #r"+djump+"#k"+
				"\r\nSpeed: #r"+dspeed+"#k"+
				"\r\nUpgrade Slots: #r"+dmSlot+"#k"+
				"\r\n"+
				"\r\nEnter what you would like for the luk(-1 to keep default):\r\n#r32767 is max#k";
			cm.sendGetText(text);
		} else if (status == 8) {
			luk = cm.getText();
			if(luk < 0)
				luk = dluk;
			if(luk > 32767)
				luk = 32767;
			
			text = "#rCURRENT STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #g"+str+"#k    DEX:  #g"+dex+"#k"+
				"\r\nINT:  #g"+_int+"#k    LUK:  #g"+luk+"#k"+
				"\r\nWATK: #r"+dwAtk+"#k    MATK: #r"+dmAtk+"#k"+
				"\r\nWDEF: #r"+dwDef+"#k    MDEF: #r"+dmDef+"#k"+
				"\r\nHP:   #r"+dhp+"#k    MP:   #r"+dmp+"#k"+
				"\r\nAccuracy: #r"+dacc+"#k"+
				"\r\nAvoidability: #r"+davo+"#k"+
				"\r\nHands: #r"+dhands+"#k"+
				"\r\nJump: #r"+djump+"#k"+
				"\r\nSpeed: #r"+dspeed+"#k"+
				"\r\nUpgrade Slots: #r"+dmSlot+"#k"+
				"\r\n"+
				"\r\nEnter what you would like for the WATK(-1 to keep default):\r\n#r32767 is max#k";
			cm.sendGetText(text);
		} else if (status == 9) {
			wAtk = cm.getText();
			if(wAtk < 0)
				wAtk = dwAtk;
			if(wAtk > 32767)
				wAtk = 32767;
			
			text = "#rCURRENT STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #g"+str+"#k    DEX:  #g"+dex+"#k"+
				"\r\nINT:  #g"+_int+"#k    LUK:  #g"+luk+"#k"+
				"\r\nWATK: #g"+wAtk+"#k    MATK: #r"+dmAtk+"#k"+
				"\r\nWDEF: #r"+dwDef+"#k    MDEF: #r"+dmDef+"#k"+
				"\r\nHP:   #r"+dhp+"#k    MP:   #r"+dmp+"#k"+
				"\r\nAccuracy: #r"+dacc+"#k"+
				"\r\nAvoidability: #r"+davo+"#k"+
				"\r\nHands: #r"+dhands+"#k"+
				"\r\nJump: #r"+djump+"#k"+
				"\r\nSpeed: #r"+dspeed+"#k"+
				"\r\nUpgrade Slots: #r"+dmSlot+"#k"+
				"\r\n"+
				"\r\nEnter what you would like for the MATK(-1 to keep default):\r\n#r32767 is max#k";
			cm.sendGetText(text);
		} else if (status == 10) {
			mAtk = cm.getText();
			if(mAtk < 0)
				mAtk = dmAtk;
			if(mAtk > 32767)
				mAtk = 32767;
			
			text = "#rCURRENT STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #g"+str+"#k    DEX:  #g"+dex+"#k"+
				"\r\nINT:  #g"+_int+"#k    LUK:  #g"+luk+"#k"+
				"\r\nWATK: #g"+wAtk+"#k    MATK: #g"+mAtk+"#k"+
				"\r\nWDEF: #r"+dwDef+"#k    MDEF: #r"+dmDef+"#k"+
				"\r\nHP:   #r"+dhp+"#k    MP:   #r"+dmp+"#k"+
				"\r\nAccuracy: #r"+dacc+"#k"+
				"\r\nAvoidability: #r"+davo+"#k"+
				"\r\nHands: #r"+dhands+"#k"+
				"\r\nJump: #r"+djump+"#k"+
				"\r\nSpeed: #r"+dspeed+"#k"+
				"\r\nUpgrade Slots: #r"+dmSlot+"#k"+
				"\r\n"+
				"\r\nEnter what you would like for the WDEF(-1 to keep default):\r\n#r32767 is max#k";
			cm.sendGetText(text);
		} else if (status == 11) {
			wDef = cm.getText();
			if(wDef < 0)
				wDef = dwDef;
			if(wDef > 32767)
				wDef = 32767;
			
			text = "#rCURRENT STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #g"+str+"#k    DEX:  #g"+dex+"#k"+
				"\r\nINT:  #g"+_int+"#k    LUK:  #g"+luk+"#k"+
				"\r\nWATK: #g"+wAtk+"#k    MATK: #g"+mAtk+"#k"+
				"\r\nWDEF: #g"+wDef+"#k    MDEF: #r"+dmDef+"#k"+
				"\r\nHP:   #r"+dhp+"#k    MP:   #r"+dmp+"#k"+
				"\r\nAccuracy: #r"+dacc+"#k"+
				"\r\nAvoidability: #r"+davo+"#k"+
				"\r\nHands: #r"+dhands+"#k"+
				"\r\nJump: #r"+djump+"#k"+
				"\r\nSpeed: #r"+dspeed+"#k"+
				"\r\nUpgrade Slots: #r"+dmSlot+"#k"+
				"\r\n"+
				"\r\nEnter what you would like for the MDEF(-1 to keep default):\r\n#r32767 is max#k";
			cm.sendGetText(text);
		} else if (status == 12) {
			mDef = cm.getText();
			if(mDef < 0)
				mDef = dmDef;
			if(mDef > 32767)
				mDef = 32767;
			
			text = "#rCURRENT STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #g"+str+"#k    DEX:  #g"+dex+"#k"+
				"\r\nINT:  #g"+_int+"#k    LUK:  #g"+luk+"#k"+
				"\r\nWATK: #g"+wAtk+"#k    MATK: #g"+mAtk+"#k"+
				"\r\nWDEF: #g"+wDef+"#k    MDEF: #g"+mDef+"#k"+
				"\r\nHP:   #r"+dhp+"#k    MP:   #r"+dmp+"#k"+
				"\r\nAccuracy: #r"+dacc+"#k"+
				"\r\nAvoidability: #r"+davo+"#k"+
				"\r\nHands: #r"+dhands+"#k"+
				"\r\nJump: #r"+djump+"#k"+
				"\r\nSpeed: #r"+dspeed+"#k"+
				"\r\nUpgrade Slots: #r"+dmSlot+"#k"+
				"\r\n"+
				"\r\nEnter what you would like for the HP(-1 to keep default):\r\n#r30000 is max#k";
			cm.sendGetText(text);
		} else if (status == 13) {
			hp = cm.getText();
			if(hp < 0)
				hp = dhp;
			if(hp > 30000)
				hp = 30000;
			
			text = "#rCURRENT STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #g"+str+"#k    DEX:  #g"+dex+"#k"+
				"\r\nINT:  #g"+_int+"#k    LUK:  #g"+luk+"#k"+
				"\r\nWATK: #g"+wAtk+"#k    MATK: #g"+mAtk+"#k"+
				"\r\nWDEF: #g"+wDef+"#k    MDEF: #g"+mDef+"#k"+
				"\r\nHP:   #g"+hp+"#k    MP:   #r"+dmp+"#k"+
				"\r\nAccuracy: #r"+dacc+"#k"+
				"\r\nAvoidability: #r"+davo+"#k"+
				"\r\nHands: #r"+dhands+"#k"+
				"\r\nJump: #r"+djump+"#k"+
				"\r\nSpeed: #r"+dspeed+"#k"+
				"\r\nUpgrade Slots: #r"+dmSlot+"#k"+
				"\r\n"+
				"\r\nEnter what you would like for the MP(-1 to keep default):\r\n#r30000 is max#k";
			cm.sendGetText(text);
		} else if (status == 14) {
			mp = cm.getText();
			if(mp < 0)
				mp = dmp;
			if(mp > 30000)
				mp = 30000;
			
			text = "#rCURRENT STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #g"+str+"#k    DEX:  #g"+dex+"#k"+
				"\r\nINT:  #g"+_int+"#k    LUK:  #g"+luk+"#k"+
				"\r\nWATK: #g"+wAtk+"#k    MATK: #g"+mAtk+"#k"+
				"\r\nWDEF: #g"+wDef+"#k    MDEF: #g"+mDef+"#k"+
				"\r\nHP:   #g"+hp+"#k    MP:   #g"+mp+"#k"+
				"\r\nAccuracy: #r"+dacc+"#k"+
				"\r\nAvoidability: #r"+davo+"#k"+
				"\r\nHands: #r"+dhands+"#k"+
				"\r\nJump: #r"+djump+"#k"+
				"\r\nSpeed: #r"+dspeed+"#k"+
				"\r\nUpgrade Slots: #r"+dmSlot+"#k"+
				"\r\n"+
				"\r\nEnter what you would like for the Accuracy(-1 to keep default):\r\n#r32767 is max#k";
			cm.sendGetText(text);
		} else if (status == 15) {
			acc = cm.getText();
			if(acc < 0)
				acc = dacc;
			if(acc > 32767)
				acc = 32767;
			
			text = "#rCURRENT STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #g"+str+"#k    DEX:  #g"+dex+"#k"+
				"\r\nINT:  #g"+_int+"#k    LUK:  #g"+luk+"#k"+
				"\r\nWATK: #g"+wAtk+"#k    MATK: #g"+mAtk+"#k"+
				"\r\nWDEF: #g"+wDef+"#k    MDEF: #g"+mDef+"#k"+
				"\r\nHP:   #g"+hp+"#k    MP:   #g"+mp+"#k"+
				"\r\nAccuracy: #g"+acc+"#k"+
				"\r\nAvoidability: #r"+davo+"#k"+
				"\r\nHands: #r"+dhands+"#k"+
				"\r\nJump: #r"+djump+"#k"+
				"\r\nSpeed: #r"+dspeed+"#k"+
				"\r\nUpgrade Slots: #r"+dmSlot+"#k"+
				"\r\n"+
				"\r\nEnter what you would like for the Avoidability(-1 to keep default):\r\n#r32767 is max#k";
			cm.sendGetText(text);
		} else if (status == 16) {
			avo = cm.getText();
			if(avo < 0)
				avo = davo;
			if(avo > 32767)
				avo = 32767;
			
			text = "#rCURRENT STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #g"+str+"#k    DEX:  #g"+dex+"#k"+
				"\r\nINT:  #g"+_int+"#k    LUK:  #g"+luk+"#k"+
				"\r\nWATK: #g"+wAtk+"#k    MATK: #g"+mAtk+"#k"+
				"\r\nWDEF: #g"+wDef+"#k    MDEF: #g"+mDef+"#k"+
				"\r\nHP:   #g"+hp+"#k    MP:   #g"+mp+"#k"+
				"\r\nAccuracy: #g"+acc+"#k"+
				"\r\nAvoidability: #g"+avo+"#k"+
				"\r\nHands: #r"+dhands+"#k"+
				"\r\nJump: #r"+djump+"#k"+
				"\r\nSpeed: #r"+dspeed+"#k"+
				"\r\nUpgrade Slots: #r"+dmSlot+"#k"+
				"\r\n"+
				"\r\nEnter what you would like for the Hands(-1 to keep default):\r\n#r30000 is max#k";
			cm.sendGetText(text);
		} else if (status == 17) {
			hands = cm.getText();
			if(hands < 0)
				hands = dhands;
			if(hands > 30000)
				hands = 30000;
			
			text = "#rCURRENT STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #g"+str+"#k    DEX:  #g"+dex+"#k"+
				"\r\nINT:  #g"+_int+"#k    LUK:  #g"+luk+"#k"+
				"\r\nWATK: #g"+wAtk+"#k    MATK: #g"+mAtk+"#k"+
				"\r\nWDEF: #g"+wDef+"#k    MDEF: #g"+mDef+"#k"+
				"\r\nHP:   #g"+hp+"#k    MP:   #g"+mp+"#k"+
				"\r\nAccuracy: #g"+acc+"#k"+
				"\r\nAvoidability: #g"+avo+"#k"+
				"\r\nHands: #g"+hands+"#k"+
				"\r\nJump: #r"+djump+"#k"+
				"\r\nSpeed: #r"+dspeed+"#k"+
				"\r\nUpgrade Slots: #r"+dmSlot+"#k"+
				"\r\n"+
				"\r\nEnter what you would like for the Jump(-1 to keep default):\r\n#r100 is max#k";
			cm.sendGetText(text);
		} else if (status == 18) {
			jump = cm.getText();
			if(jump < 0)
				jump = djump;
			if(jump > 100)
				jump = 100;
			
			text = "#rCURRENT STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #g"+str+"#k    DEX:  #g"+dex+"#k"+
				"\r\nINT:  #g"+_int+"#k    LUK:  #g"+luk+"#k"+
				"\r\nWATK: #g"+wAtk+"#k    MATK: #g"+mAtk+"#k"+
				"\r\nWDEF: #g"+wDef+"#k    MDEF: #g"+mDef+"#k"+
				"\r\nHP:   #g"+hp+"#k    MP:   #g"+mp+"#k"+
				"\r\nAccuracy: #g"+acc+"#k"+
				"\r\nAvoidability: #g"+avo+"#k"+
				"\r\nHands: #g"+hands+"#k"+
				"\r\nJump: #g"+jump+"#k"+
				"\r\nSpeed: #r"+dspeed+"#k"+
				"\r\nUpgrade Slots: #r"+dmSlot+"#k"+
				"\r\n"+
				"\r\nEnter what you would like for the Speed(-1 to keep default):\r\n#r100 is max#k";
			cm.sendGetText(text);
		} else if (status == 19) {
			speed = cm.getText();
			if(speed < 0)
				speed = dspeed;
			if(speed > 100)
				speed = 100;
			
			text = "#rCURRENT STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #g"+str+"#k    DEX:  #g"+dex+"#k"+
				"\r\nINT:  #g"+_int+"#k    LUK:  #g"+luk+"#k"+
				"\r\nWATK: #g"+wAtk+"#k    MATK: #g"+mAtk+"#k"+
				"\r\nWDEF: #g"+wDef+"#k    MDEF: #g"+mDef+"#k"+
				"\r\nHP:   #g"+hp+"#k    MP:   #g"+mp+"#k"+
				"\r\nAccuracy: #g"+acc+"#k"+
				"\r\nAvoidability: #g"+avo+"#k"+
				"\r\nHands: #g"+hands+"#k"+
				"\r\nJump: #g"+jump+"#k"+
				"\r\nSpeed: #g"+speed+"#k"+
				"\r\nUpgrade Slots: #r"+dmSlot+"#k"+
				"\r\n"+
				"\r\nEnter what you would like for the Upgrade Slots(-1 to keep default):\r\n#r100 is max#k";
			cm.sendGetText(text);
		} else if (status == 20) {
			mSlot = cm.getText();
			if(mSlot < 0)
				mSlot = dmSlot;
			if(mSlot > 100)
				mSlot = 100;
			
			text = "#rFINAL STATS:#k\r\n"+
				"#v"+id+"##b#t"+id+"##k"+
				"\r\nSTR:  #g"+str+"#k    DEX:  #g"+dex+"#k"+
				"\r\nINT:  #g"+_int+"#k    LUK:  #g"+luk+"#k"+
				"\r\nWATK: #g"+wAtk+"#k    MATK: #g"+mAtk+"#k"+
				"\r\nWDEF: #g"+wDef+"#k    MDEF: #g"+mDef+"#k"+
				"\r\nHP:   #g"+hp+"#k    MP:   #g"+mp+"#k"+
				"\r\nAccuracy: #g"+acc+"#k"+
				"\r\nAvoidability: #g"+avo+"#k"+
				"\r\nHands: #g"+hands+"#k"+
				"\r\nJump: #g"+jump+"#k"+
				"\r\nSpeed: #g"+speed+"#k"+
				"\r\nUpgrade Slots: #g"+mSlot+"#k"+
				"\r\n"+
				"\r\nWould you like to create this equip?";
			cm.sendYesNo(text);
		} else if (status == 21) {
			if(equip != null){
				cm.giveCustomStatItem(equip, str, dex, _int, luk, mAtk, wAtk, mDef, wDef, hp, mp, acc, avo, hands, jump, speed, mSlot, cm.getName());
				cm.sendOk("There you go it has been created.");
			} else {
				text = "The item with id "+id+" is not an equip.";
				cm.sendOk(text);
			}
			cm.dispose();
		} else {
			cm.dispose();
		}
	}
}
