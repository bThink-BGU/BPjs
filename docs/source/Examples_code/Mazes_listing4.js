function addWall(col, row) {
	bp.registerBThread(function(){
		bp.sync({block: enterEvent(col, row)});
	});
}
