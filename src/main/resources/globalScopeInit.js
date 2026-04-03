/* global bp */
// Wrap bp.log methods to:
// 1. Add a dummy {} at the end to prevent the single-array-param Java varargs issue
// 2. JSON.stringify params lazily (only when the log level is active)
(function() {
    var _javaLog = bp.log;

    function _wrapArgs(args) {
        var result = [args[0]];
        for (var i = 1; i < args.length; i++) {
            var p = args[i];
            if (p !== null && p !== undefined && typeof p === 'object') {
                try { p = JSON.stringify(p); } catch(e) {}
            }
            result.push(p);
        }
        result.push({});
        return result;
    }

    bp.log = {
        setLevel:             function(lvl) { _javaLog.setLevel(lvl); },
        setLoggerPrintStream: function(ps)  { _javaLog.setLoggerPrintStream(ps); },
        getLevel:             function()    { return _javaLog.getLevel(); },
        isFineEnabled:        function()    { return _javaLog.isFineEnabled(); },
        isInfoEnabled:        function()    { return _javaLog.isInfoEnabled(); },
        isWarnEnabled:        function()    { return _javaLog.isWarnEnabled(); },
        isErrorEnabled:       function()    { return _javaLog.isErrorEnabled(); },

        fine:  function() { if (_javaLog.isFineEnabled())  _javaLog.fine.apply( _javaLog, _wrapArgs(arguments)); },
        info:  function() { if (_javaLog.isInfoEnabled())  _javaLog.info.apply( _javaLog, _wrapArgs(arguments)); },
        warn:  function() { if (_javaLog.isWarnEnabled())  _javaLog.warn.apply( _javaLog, _wrapArgs(arguments)); },
        error: function() { if (_javaLog.isErrorEnabled()) _javaLog.error.apply(_javaLog, _wrapArgs(arguments)); }
    };
})();
