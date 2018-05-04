bp.registerBThread("onlyOnce", function(){
	var block = [];
	while (true) {
		var evt = bsync({waitFor: anyEntrance, block: block});
		block.push(evt);
	}
});
