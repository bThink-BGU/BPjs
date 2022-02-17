/* global bp */

bp.log.info("Starting the test");
bp.log.info("about to perform an illegal thing");
bp.sync({
    request:bp.Event("boom")
});
bp.log.info("We should not get here, really.");
