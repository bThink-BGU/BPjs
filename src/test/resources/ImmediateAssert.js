/* global bp */
// This b-thread goes forward until it's done.\n" +
bp.registerBThread("forward", function () {
bp.ASSERT(false, "failRightAWay!");
    bp.sync({request: bp.Event("A")});
});
bp.registerBThread("assertor", function () {
    var e = bp.sync({waitFor: bp.Event("B")});
    if (e.name == "B") {
        bp.ASSERT(false, "B happened");
    }
});