bp.registerBThread(function(){
	bsync({request:COLD});
	bsync({request:COLD});
	bsync({request:COLD});
	bsync({request:HOT});
	bsync({request:HOT});
	bsync({request:HOT});
});
