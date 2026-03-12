/* global bp */

const makeEvent = name => bp.Event(name);
const ONLY_ARROW_2 = bp.EventSet("only-arrow-2", e => e.name == "arrow-2");

bp.registerBThread("arrow-requester", () => {
    bp.sync({request: makeEvent("arrow-1")});
    bp.sync({request: makeEvent("arrow-2")});
    bp.sync({request: makeEvent("arrow-3")});
});

bp.registerBThread("arrow-waiter", () => {
    bp.sync({waitFor: makeEvent("arrow-1")});
    bp.sync({waitFor: ONLY_ARROW_2});
    bp.sync({waitFor: makeEvent("arrow-3")});
});

const registerFinisher = () => bp.registerBThread("arrow-finisher", () => {
    bp.sync({waitFor: makeEvent("arrow-3")});
    bp.sync({request: makeEvent("done")});
});

registerFinisher();
