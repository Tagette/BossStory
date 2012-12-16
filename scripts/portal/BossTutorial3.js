function enter(pi) {
	var total = pi.getPlayer().getTotalMeso();
	if (total >= 4000000000 && total < 11000000000) { // 5 - 11 pills
	    pi.warp(40000);
	} else {
		pi.message("You are not done here.");
	}
	return true;
}