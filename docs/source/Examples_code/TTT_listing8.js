bp.registerBThread("Center",function() {
	while (true)
		bsync({request:[O(1,1)]},35);
});
