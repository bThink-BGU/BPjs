var testEvent = bp.Event("testEvent");

bp.registerBThread("first", function () {
    var arr1 = [null, null, testEvent];
    bp.sync({waitFor: arr1});
});