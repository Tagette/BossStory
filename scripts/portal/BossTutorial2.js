function enter(pi) {
	// Has second job
	if (pi.getPlayer().getTotalMeso() >= 1000000000) {
	    pi.warp(30000);
	} else {
		pi.message("Please talk to cody.");
	}
	return true;
}