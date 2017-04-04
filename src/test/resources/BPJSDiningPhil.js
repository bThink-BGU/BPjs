/*Works Good*/
P1R =  bp.Event("Pick1R");	//Phil1 pickes right
P1L =  bp.Event("Pick1L");	//Phil1 pickes left
P2R =  bp.Event("Pick2R");	//Phil2 pickes right
P2L =  bp.Event("Pick2L");	//Phil2 pickes left

R1R =  bp.Event("Rel1R");	//Phil1 release right
R1L =  bp.Event("Rel1L");	//Phil1 release left
R2R =  bp.Event("Rel2R");	//Phil2 release right
R2L =  bp.Event("Rel2L");	//Phil2 release left

bp.registerBThread( "Phil1", function() {
	while(true) {
		bsync( {request: P1R});	//requests to pick his right stick
		bsync( {request: P1L});	//requests to pick his left stick
		
		bsync( {request: R1L});	//requests to release his left stick
		bsync( {request: R1R});	//requests to release his right stick
	}
});

bp.registerBThread( "Phil2", function() {
	while(true) {
		bsync( {request: P2L});	//requests to pick his left stick
		bsync( {request: P2R});	//requests to pick his right stick
		
		bsync( {request: R2R});	//requests to release his right stick
		bsync( {request: R2L});	//requests to release his left stick
	}
});

//Force the stick between p1 and p2 to be one
bp.registerBThread( "Stick between P1 and P2", function() {   
	while(true) {
		var e = bsync( {waitFor: [P1R, P2L], block: [R1R,R2L] });	//waits for pick up
		var wt = (e == P1R ? R1R : R2L);	
		bsync( {waitFor: wt, block: [P1R,P2L] });	//waits for release by the same Phil
	}
});

//Force the stick between p2 and p1 to be one
bp.registerBThread( "Stick between P2 and P1", function() {   
	while(true) {
		var e = bsync( {waitFor: [P1L, P2R], block: [R1L,R2R] });	//waits for pick up
		var wt = (e == P1L ? R1L : R2R);
		bsync( {waitFor: wt, block: [P1L,P2R] });	//waits for release by the same Phil
	}
});

