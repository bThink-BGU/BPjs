/* global bp */

// Wrap bp.log to:
// 1. Fix the single-array-param Java varargs issue: when the sole argument is an
//    object/array, stringify it in JS before passing to Java so Rhino never has a
//    chance to spread it across varargs parameters.
// 2. Evaluate arguments lazily (only when the log level is active).
(function() {
    var _javaLog = bp.log;

    function _mkLogFn(levelName) {
        return function() {
            if (!_javaLog.isLevelEnabled(levelName)) return;
            if ( arguments.length === 0 ) {
                _javaLog[levelName.toLowerCase()](""); // edge case - user called logging with no arguments.
                
            } else {
                let params = Array.from(arguments);
                let msg = params.shift();
                _javaLog[levelName.toLowerCase()](msg, params);
            }
        };
    }

    bp.log = {
        setLevel:             function(lvl) { _javaLog.setLevel(lvl); },
        getLevel:             function()    { return _javaLog.getLevel(); },
        isLevelEnabled:       function(lvl) { return _javaLog.isLevelEnabled(lvl); },

        fine:  _mkLogFn("Fine"),
        info:  _mkLogFn("Info"),
        warn:  _mkLogFn("Warn"),
        error: _mkLogFn("Error")
    };
})();
