function addSpaceCell( col, row ) {
	bp.registerBThread("cell(c:"+col+" r:"+row+")",
		function() {
			while ( true ) {
				bsync({waitFor: adjacentCellEntries(col, row)});
				bsync({request: enterEvent(col, row), waitFor: anyEntrance});
			}
		}
	);
}
