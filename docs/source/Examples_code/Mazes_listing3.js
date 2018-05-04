function addStartCell(col, row) {
	bp.registerBThread("startCell", function() {
		bp.sync({request: enterEvent(col,row)});
	});
}
