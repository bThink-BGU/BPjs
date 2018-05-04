bp.registerBThread(function(){
	bp.sync({request:COLD});
	bp.sync({request:COLD});
	bp.sync({request:COLD});
	bp.sync({request:HOT});
	bp.sync({request:HOT});
	bp.sync({request:HOT});
});
