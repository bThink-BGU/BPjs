bp.registerBThread("EnforceTurns", function() {
	while (true) {
		bp.sync({waitFor:[X(0,0),X(0,1),...,X(2,2)],
			block:[O(0,0),O(0,1),...,O(2,2)]});

		bp.sync({waitFor:[O(0,0),O(0,1),...,O(2,2)],
			block:[X(0,0),X(0,1),...,X(2,2)]});
	}
});

