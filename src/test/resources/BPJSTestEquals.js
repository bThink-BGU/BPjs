/* global bp, bsync */

bp.log.info('BPJSTestEquals');


bp.registerBThread("BThread 1", function() {
	while (true) {
		bp.sync({wait : bp.Event("X")});
		bp.sync({wait : bp.Event("X")});
	}
});


//bp.registerBThread("BThread 1", function() {
//	while (true) {
//		for( var i=0; i<5; i++)
//			bsync({ request : bp.Event("X")	}, "X "+i);
//		
//		bsync({ request : bp.Event("O")	}, "O");
//	}
//});


