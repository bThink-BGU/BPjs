function addWall(col, row) {
	bp.registerBThread(function(){
		bsync({block: enterEvent(col, row)});
	});
}
