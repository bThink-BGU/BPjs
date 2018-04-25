/* global bp */
/*
 * Demonstrating the new bp.sync usage.
 */
var sampleEvent = bp.Event("sampleEvent");
bp.registerBThread("helloer", function () {
    bp.sync({request: sampleEvent});
    bp.sync({request: sampleEvent, block: sampleEvent});
});

