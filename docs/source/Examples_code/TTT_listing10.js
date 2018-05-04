bp.registerBThread("Sides",function() {
	while (true) {
		bp.sync({request:[O(0,1),O(1,0),O(1,2),O(2,1)]},10);
	}
});
