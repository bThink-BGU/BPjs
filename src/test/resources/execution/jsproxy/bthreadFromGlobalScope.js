/* global bp */

bp.log.info("Starting the test");
bp.log.info("about to perform an illegal thing");
const t =bp.thread;
bp.log.info("We should not get here, really.");
