/* global bp */

bp.store.put("name", "Posh");
bp.store.put("name", "Josh");

if ( ! bp.store.has("name") ) {
    throw "'has' not working";
}

bp.store.put("person", "Agenta Falskog");
bp.store.put("question", "Whaa?");

if ( ! (bp.store.size()===3) ) {
    throw "'size' should be 3 but it's " + bp.store.size();
}

bp.store.remove("question");

if ( bp.store.has("question") ) {
    throw "remove not working";
}

if ( ! (bp.store.size()===2) ) {
    throw "'size' should be 2 but it's " + bp.store.size();
}

if ( ! (bp.store.keys().size()===2) ) {
    throw "'keys.size' should be 2 but it's " + bp.store.size();
}

bp.registerBThread("storageReader", function(){
    bp.sync({request:bp.Event(bp.store.get("name"))});
    bp.sync({request:bp.Event(bp.store.get("person"))});
});