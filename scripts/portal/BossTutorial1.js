function enter(pi) {
	var level = pi.getPlayer().getLevel();
	var job = pi.getPlayer().getJob().getId();
	// Has second job
	if (job % 100 != 0) {
	    pi.warp(20000);
	} else {
		pi.message("Please talk to cody.");
	}
	return true;
}