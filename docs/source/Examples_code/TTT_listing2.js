bp.registerBThread("SqrTkn("+row+","+col+")", function(){
		while (true) {
			bsync({waitFor:[X(row,col), O(row,col)]});
			bsync({block:[X(row,col), O(row,col)]});
	}
});
