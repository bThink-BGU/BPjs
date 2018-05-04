function addStartCell(col, row) {
	bp.registerBThread("startCell", function() {
		bsync({request: enterEvent(col,row)});
	});
}
