function addSpaceCell( col, row ) {
	bp.registerBThread("cell(c:"+col+" r:"+row+")",
		function() {
			while ( true ) {
				bp.sync({waitFor: adjacentCellEntries(col, row)});
				bp.sync({request: enterEvent(col, row), waitFor: anyEntrance});
			}
		}
	);
}
