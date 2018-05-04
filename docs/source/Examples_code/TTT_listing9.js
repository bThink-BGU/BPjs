bp.registerBThread("Corners",function() {
	while (true)
		bp.sync({request:[O(0,0),O(0,2),O(2,0),O(2,2)]},20);
});
