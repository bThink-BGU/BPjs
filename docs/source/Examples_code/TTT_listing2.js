bp.registerBThread("SqrTkn("+row+","+col+")", function(){
		while (true) {
			bp.sync({waitFor:[X(row,col), O(row,col)]});
			bp.sync({block:[X(row,col), O(row,col)]});
	}
});
