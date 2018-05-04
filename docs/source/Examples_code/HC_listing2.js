bp.registerBThread("add-hot", function(){
	bp.sync({request:HOT});
	bp.sync({request:HOT});
	bp.sync({request:HOT});
});

bp.registerBThread("add-cold", function(){
	bp.sync({request:COLD});
	bp.sync({request:COLD});
	bp.sync({request:COLD});
});
