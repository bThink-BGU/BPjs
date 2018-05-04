bp.registerBThread("Center",function() {
	while (true)
		bp.sync({request:[O(1,1)]},35);
});
