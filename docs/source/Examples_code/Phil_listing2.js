function addStick(i) {
	var j = (i%PHILOSOPHER_COUNT)+1;

	bp.registerBThread("Stick"+i, function () {
		var pickMe = bp.EventSet("pick"+i, function(e) {
			return (e.name === "Pick"+i+"R"
				|| e.name === "Pick"+j+"L");
		});
		var releaseMe = [bp.Event("Rel"+i+"R"),
					bp.Event("Rel"+j+"L")];

		while (true) {
			var e = bp.sync({waitFor: pickMe,
					block: releaseMe});

			var wt = (e.name === "Pick"+i+"R") ?
					"Rel"+i+"R" : "Rel"+j+"L";
			bp.sync({waitFor: bp.Event(wt),
				block: releaseMe});
		}
	});
}
